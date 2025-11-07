package me.jetby.treexclans.addon;

import lombok.Getter;
import me.jetby.treexclans.addon.service.ServiceManagerImpl;
import me.jetby.treexclans.api.addons.AddonContext;
import me.jetby.treexclans.api.addons.AddonManager;
import me.jetby.treexclans.api.addons.JavaAddon;
import me.jetby.treexclans.api.addons.annotations.ClanAddon;
import me.jetby.treexclans.api.addons.annotations.Dependency;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles dynamic loading, enabling, disabling and unloading of TreexClans addons.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Scan and load addon JARs from /addons directory</li>
 *     <li>Detect classes annotated with {@link ClanAddon}</li>
 *     <li>Initialize and enable addons in dependency order</li>
 *     <li>Unload addons gracefully on shutdown</li>
 * </ul>
 *
 * <p><b>Lifecycle:</b> Load → Initialize → Enable → Disable → Unload</p>
 */
public final class AddonManagerImpl implements AddonManager {

    private final JavaPlugin plugin; //TODO api for TreexClans
    private final File addonsFolder;
    private final Logger logger; // Используем стандартный Logger для детального логирования

    @Getter
    private final Map<String, JavaAddon> loadedAddons = new LinkedHashMap<>(); // LinkedHashMap для порядка загрузки
    private final Map<String, URLClassLoader> classLoaders = new HashMap<>(); // Ключ: имя JAR
    private final Map<String, File> jarFiles = new HashMap<>(); // Ключ: ID аддона → JAR файл (для перезагрузки)

    private final boolean debugMode;

    public AddonManagerImpl(@NotNull JavaPlugin plugin) {
        this(plugin, false);
    }

    public AddonManagerImpl(@NotNull JavaPlugin plugin, boolean debugMode) {
        this.plugin = plugin;
        this.debugMode = debugMode;
        this.logger = plugin.getLogger();
        this.addonsFolder = new File(plugin.getDataFolder(), "addons");

        if (!addonsFolder.exists() && addonsFolder.mkdirs()) {
            logInfo("Created addons folder: " + addonsFolder.getAbsolutePath());
        }
    }

    @Override
    public void loadAddons() {
        File[] jars = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            logInfo("No addons found in " + addonsFolder.getAbsolutePath());
            return;
        }

        logInfo("Found " + jars.length + " addon(s) to load.");
        List<Throwable> errors = new ArrayList<>();

        for (File jarFile : jars) {
            try {
                loadAddon(jarFile);
            } catch (Throwable e) {
                String msg = "Failed to load addon " + jarFile.getName() + ": " + e.getClass().getSimpleName() + " — " + e.getMessage();
                logError(msg, e);
                errors.add(e);
            }
        }

        if (!errors.isEmpty()) {
            logWarn("Loaded with " + errors.size() + " error(s).");
        }

        enableAll();
        logInfo(loadedAddons.size() + " addon(s) loaded successfully.");
    }


    /**
     * Загружает один JAR-аддон.
     * Сканирует классы, находит @TreexAddonInfo, инициализирует и добавляет в loadedAddons (но не включает!).
     * Если ID уже загружен — выбрасывает IllegalStateException.
     *
     * @param jarFile Путь к JAR-файлу.
     * @throws IllegalStateException Если аддон с таким ID уже загружен.
     * @throws Exception             Если ошибка сканирования/загрузки.
     * @see #enableAddon(String)
     */
    @Override
    public void loadAddon(@NotNull File jarFile) throws Exception {
        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Invalid JAR file: " + jarFile.getAbsolutePath());
        }

        logDebug("Loading addon from " + jarFile.getName());
        URLClassLoader loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, plugin.getClass().getClassLoader());
        classLoaders.put(jarFile.getName(), loader);

        List<Class<?>> classes = scanClassesInJar(jarFile, loader);
        String loadedId = null;

        for (Class<?> clazz : classes) {
            ClanAddon meta = clazz.getAnnotation(ClanAddon.class);
            if (meta == null) continue;
            if (!JavaAddon.class.isAssignableFrom(clazz)) {
                logWarn("Class " + clazz.getName() + " is annotated with @Addon but doesn't implement TreexAddon.");
                continue;
            }
            if (loadedAddons.containsKey(meta.id())) {
                throw new IllegalStateException("Addon with ID '" + meta.id() + "' is already loaded.");
            }

            JavaAddon addon = (JavaAddon) clazz.getDeclaredConstructor().newInstance();
            addon.initialize(new AddonContext(
                    new ServiceManagerImpl(this, addonsFolder, plugin, meta),
                    logger
            ));

            loadedAddons.put(meta.id(), addon);
            jarFiles.put(meta.id(), jarFile);
            loadedId = meta.id();

            logInfo("Loaded addon: " + meta.id());
        }

        if (loadedId == null) {
            logWarn("No @Addon classes found in " + jarFile.getName());
            classLoaders.remove(jarFile.getName());
        }

    }

    @Override
    public void enableAll() {
        List<JavaAddon> ordered = sortByDependencies();
        logDebug("Enabling " + ordered.size() + " addon(s) in dependency order.");
        ordered.forEach(a -> enableAddon(a.getInfo().id()));
    }

    @Override
    public boolean enableAddon(@NotNull String addonId) {
        JavaAddon addon = loadedAddons.get(addonId);
        if (addon == null) {
            logWarn("Addon not found: " + addonId);
            return false;
        }

        ClanAddon info = addon.getInfo();
        if (!checkDependencies(info)) {
            logWarn("Skipping " + info.id() + " due to missing dependencies.");
            return false;
        }

        try {
            addon.onEnable();
            logInfo("Enabled addon: " + info.id());
            return true;
        } catch (Throwable e) {
            logError("Error enabling " + info.id() + ": " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void unloadAll() {
        List<JavaAddon> reversed = new ArrayList<>(loadedAddons.values());
        Collections.reverse(reversed);
        reversed.forEach(a -> unloadAddon(a.getInfo().id()));

        loadedAddons.clear();
        jarFiles.clear();
        classLoaders.values().forEach(loader -> {
            try {
                loader.close();
            } catch (IOException ignored) {}
        });
        classLoaders.clear();

        logInfo("All addons unloaded.");
    }

    @Override
    public boolean disable(@NotNull JavaAddon addon) {
        return unloadAddon(addon.getInfo().id());
    }

    @Override
    public boolean unloadAddon(@NotNull String addonId) {
        JavaAddon addon = loadedAddons.get(addonId);
        if (addon == null) {
            logWarn("Addon not found: " + addonId);
            return false;
        }

//        Set<String> dependents = findDependents(addonId);
//        if (!dependents.isEmpty()) {
//            logWarn("Addon " + addonId + " has dependents: " + dependents + ". Unloading anyway.");
//            dependents.forEach(this::unloadAddon);
//        }

        try {
            addon.onDisable();
            logInfo("Disabled addon: " + addonId);
        } catch (Throwable e) {
            logError("Error disabling " + addonId + ": " + e.getMessage(), e);
        }

        loadedAddons.remove(addonId);
        File jarFile = jarFiles.remove(addonId);
        if (jarFile != null) {
            URLClassLoader loader = classLoaders.remove(jarFile.getName());
            if (loader != null) try { loader.close(); } catch (IOException ignored) {}
        }

        return true;
    }

    @Nullable
    @Override
    public JavaAddon getAddon(@NotNull String addonId) {
        return loadedAddons.get(addonId);
    }

    @Override
    public boolean isLoaded(@NotNull String addonId) {
        return loadedAddons.containsKey(addonId);
    }

    @NotNull
    @Override
    public List<String> getAddonIds() {
        return List.copyOf(loadedAddons.keySet());
    }

    private List<Class<?>> scanClassesInJar(@NotNull File jarFile, @NotNull URLClassLoader loader) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream()
                    .filter(e -> !e.isDirectory() && e.getName().endsWith(".class"))
                    .map(e -> e.getName().replace('/', '.').replace(".class", ""))
                    .forEach(name -> {
                        try {
                            classes.add(loader.loadClass(name));
                        } catch (Throwable ignored) {
                            logDebug("Failed to load class: " + name);
                        }
                    });
        }
        return classes;
    }

    private boolean checkDependencies(@NotNull ClanAddon info) {
        boolean ok = true;
        for (Dependency dep : info.depends()) {
            if (!isLoaded(dep.id())) {
                logWarn("Missing dependency: " + dep.id() + " for " + info.id());
                ok = false;
            }
        }
        return ok;
    }

    private List<JavaAddon> sortByDependencies() {
        Map<String, Set<String>> graph = buildDependencyGraph();
        Map<String, Integer> indegree = new HashMap<>();
        graph.keySet().forEach(id -> indegree.put(id, 0));
        graph.values().forEach(deps -> deps.forEach(dep -> indegree.merge(dep, 1, Integer::sum)));

        Deque<String> queue = new ArrayDeque<>();
        indegree.forEach((id, deg) -> { if (deg == 0) queue.add(id); });

        List<JavaAddon> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            JavaAddon addon = loadedAddons.get(id);
            if (addon != null) order.add(addon);
            for (String dep : graph.getOrDefault(id, Set.of())) {
                indegree.put(dep, indegree.get(dep) - 1);
                if (indegree.get(dep) == 0) queue.add(dep);
            }
        }
        return order;
    }

    private Map<String, Set<String>> buildDependencyGraph() {
        Map<String, Set<String>> graph = new HashMap<>();
        for (JavaAddon a : loadedAddons.values()) {
            graph.put(a.getInfo().id(), new LinkedHashSet<>());
        }
        for (JavaAddon a : loadedAddons.values()) {
            ClanAddon info = a.getInfo();
            String id = info.id();
            for (Dependency d : info.depends()) graph.get(id).add(d.id());
            for (Dependency d : info.softDepends()) graph.get(id).add(d.id());
            for (String after : info.loadAfter()) graph.get(id).add(after);
            for (String before : info.loadBefore()) graph.get(before).add(id);
        }
        return graph;
    }

    private Set<String> findDependents(@NotNull String givenId) {
        Set<String> dependents = new HashSet<>();
        for (JavaAddon a : loadedAddons.values()) {
            ClanAddon info = a.getInfo();
            if (info.id().equals(givenId)) continue;
            for (Dependency d : info.depends()) if (d.id().equals(givenId)) dependents.add(info.id());
            for (Dependency d : info.softDepends()) if (d.id().equals(givenId)) dependents.add(info.id());
            for (String after : info.loadAfter()) if (after.equals(givenId)) dependents.add(info.id());
        }
        return dependents;
    }

    private void logInfo(String msg) { if (debugMode) logger.info("[TreexAddon] " + msg); }
    private void logWarn(String msg) { if (debugMode) logger.warning("[TreexAddon] " + msg); }
    private void logError(String msg, Throwable e) { if (debugMode) logger.log(Level.SEVERE, "[TreexAddon] " + msg, e); }
    private void logDebug(String msg) { if (debugMode) logger.fine("[TreexAddon] " + msg); }
}
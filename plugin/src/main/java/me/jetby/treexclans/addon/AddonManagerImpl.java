package me.jetby.treexclans.addon;

import lombok.Getter;
import me.jetby.treexclans.addon.service.ServiceManagerImpl;
import me.jetby.treexclans.api.addons.AddonContext;
import me.jetby.treexclans.api.addons.AddonManager;
import me.jetby.treexclans.api.addons.JavaAddon;
import me.jetby.treexclans.api.addons.annotations.ClanAddon;
import me.jetby.treexclans.api.addons.annotations.Dependency;
import me.jetby.treexclans.api.addons.exception.*;
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

    private final JavaPlugin plugin;
    private final File addonsFolder;
    private final Logger logger;

    private final Map<String, JavaAddon> loadedAddons = new LinkedHashMap<>();
    private final Map<String, URLClassLoader> classLoaders = new HashMap<>();
    private final Map<String, File> jarFiles = new HashMap<>();

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

    public void loadAddons() {
        File[] jars = addonsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            logInfo("No addons found in " + addonsFolder.getAbsolutePath());
            return;
        }

        logInfo("Found " + jars.length + " addon(s) to load.");
        int success = 0;

        for (File jarFile : jars) {
            try {
                JavaAddon addon = loadAddon(jarFile);
                if (addon != null) {
                    success++;
                }
            } catch (AddonException e) {
                logWarn("Failed to load addon " + jarFile.getName() + ": " + e.getMessage());
                logDebug("Cause: " + e.getClass().getSimpleName());
            } catch (Throwable e) {
                logError("Unexpected error loading " + jarFile.getName(), e);
            }
        }

        logInfo(success + " addon(s) loaded successfully.");
        enableAll();
    }


    @Nullable
    @Override
    public JavaAddon loadAddon(@NotNull File jarFile)
            throws AddonLoadException, DuplicateAddonIdException, MissingDependencyException, AddonNotFoundException {

        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
            throw new AddonLoadException("Invalid JAR file: " + jarFile.getAbsolutePath(), null);
        }

        logDebug("Loading addon from " + jarFile.getName());
        URLClassLoader loader;

        try {
            loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, plugin.getClass().getClassLoader());
            classLoaders.put(jarFile.getName(), loader);
        } catch (IOException e) {
            throw new AddonLoadException("Failed to open JAR file " + jarFile.getName(), e);
        }

        List<Class<?>> classes = scanClassesInJar(jarFile, loader);
        JavaAddon addon = null;

        for (Class<?> clazz : classes) {
            ClanAddon meta = clazz.getAnnotation(ClanAddon.class);
            if (meta == null) continue;

            if (!JavaAddon.class.isAssignableFrom(clazz)) {
                logWarn("Class " + clazz.getName() + " is annotated with @ClanAddon but does not extend JavaAddon.");
                continue;
            }

            if (loadedAddons.containsKey(meta.id())) {
                throw new DuplicateAddonIdException("Addon with ID '" + meta.id() + "' is already loaded.");
            }

            try {
                addon = (JavaAddon) clazz.getDeclaredConstructor().newInstance();
                addon.initialize(new AddonContext(
                        new ServiceManagerImpl(this, addonsFolder, plugin, meta),
                        logger
                ));
                loadedAddons.put(meta.id(), addon);
                jarFiles.put(meta.id(), jarFile);
                logInfo("Loaded addon: " + meta.id());
                break;
            } catch (Throwable e) {
                throw new AddonLoadException("Failed to instantiate addon class " + clazz.getName(), e);
            }
        }

        if (addon == null) {
            classLoaders.remove(jarFile.getName());
            throw new AddonNotFoundException("No valid @ClanAddon class found in " + jarFile.getName());
        }

        return addon;
    }

    public void enableAll() {
        List<JavaAddon> ordered = sortByDependencies();
        logDebug("Enabling " + ordered.size() + " addon(s) in dependency order.");
        ordered.forEach(a -> {
            try {
                enableAddon(a);
            } catch (AddonEnableException e) {
                logWarn("Failed to enable " + a.getInfo().id() + ": " + e.getMessage());
            }
        });
    }

    @Override
    public boolean enableAddon(@NotNull JavaAddon addon) throws AddonEnableException {
        ClanAddon info = addon.getInfo();

        if (!checkDependencies(info)) {
            throw new AddonEnableException("Missing dependencies for addon " + info.id(), null);
        }

        try {
            invokeLifecycle(addon, "enable");
            logInfo("Enabled addon: " + info.id());
            return true;
        } catch (Throwable e) {
            throw new AddonEnableException("Error while enabling addon " + info.id(), e);
        }
    }

    @Override
    public void disableAddons() {
        List<JavaAddon> reversed = new ArrayList<>(loadedAddons.values());
        Collections.reverse(reversed);
        reversed.forEach(this::disableAddon);

        loadedAddons.clear();
        jarFiles.clear();
        closeAllClassLoaders();
        logInfo("All addons unloaded.");
    }

    @Override
    public boolean disableAddon(@NotNull JavaAddon addon) {
        String id = addon.getInfo().id();
        try {
            invokeLifecycle(addon, "disable");
            logInfo("Disabled addon: " + id);
        } catch (Throwable e) {
            logError("Error disabling addon " + id, e);
        }

        loadedAddons.remove(id);
        File jarFile = jarFiles.remove(id);
        if (jarFile != null) {
            URLClassLoader loader = classLoaders.remove(jarFile.getName());
            if (loader != null) try { loader.close(); } catch (IOException ignored) {}
        }
        return true;
    }

    private void invokeLifecycle(JavaAddon addon, String methodName) throws Throwable {
        var method = addon.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(addon);
    }

    private void closeAllClassLoaders() {
        classLoaders.values().forEach(loader -> {
            try {
                loader.close();
            } catch (IOException ignored) {}
        });
        classLoaders.clear();
    }

    private List<Class<?>> scanClassesInJar(@NotNull File jarFile, @NotNull URLClassLoader loader) throws AddonLoadException {
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
        } catch (IOException e) {
            throw new AddonLoadException("Failed to read JAR: " + jarFile.getName(), e);
        }
        return classes;
    }

    private boolean checkDependencies(@NotNull ClanAddon info) {
        boolean ok = true;
        for (Dependency dep : info.depends()) {
            if (!isAddonEnabled(dep.id())) {
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
            for (String before : info.loadBefore()) graph.computeIfAbsent(before, k -> new LinkedHashSet<>()).add(id);
        }
        return graph;
    }

    @Nullable
    @Override
    public JavaAddon getAddon(@NotNull String addonId) {
        return loadedAddons.get(addonId);
    }

    @Override
    public boolean isAddonEnabled(@NotNull String addonId) {
        return loadedAddons.containsKey(addonId);
    }

    @Override
    public boolean isAddonEnabled(@NotNull JavaAddon addon) {
        return loadedAddons.containsValue(addon);
    }

    @NotNull
    @Override
    public List<JavaAddon> getAddons() {
        return List.copyOf(loadedAddons.values());
    }

    private void logInfo(String msg) { logger.info("[TreexAddon] " + msg); }
    private void logWarn(String msg) { logger.warning("[TreexAddon] " + msg); }
    private void logError(String msg, Throwable e) { logger.log(Level.SEVERE, "[TreexAddon] " + msg, e); }
    private void logDebug(String msg) { if (debugMode) logger.info("[TreexAddon:DEBUG] " + msg); }
}
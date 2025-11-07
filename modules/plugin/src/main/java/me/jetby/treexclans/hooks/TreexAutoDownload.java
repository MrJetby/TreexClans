package me.jetby.treexclans.hooks;

import me.jetby.treex.Treex;
import me.jetby.treex.events.TreexOnPluginDisable;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class TreexAutoDownload implements Listener {

    private final JavaPlugin plugin;

    @EventHandler
    public void onDisable(TreexOnPluginDisable e) {
        Bukkit.getPluginManager().disablePlugin(plugin);
    }

    public TreexAutoDownload(JavaPlugin plugin) throws IOException {
        this.plugin = plugin;
        Plugin treex = Bukkit.getPluginManager().getPlugin("Treex");
        if (treex == null || !treex.isEnabled()) {
            downloadAndLoad(getRaw("https://raw.githubusercontent.com/MrJetby/Treex/refs/heads/master/DOWNLOAD_LINK"));
        }
        Treex.init(plugin);

        Bukkit.getPluginManager().registerEvents(this, plugin);

    }

    private String getRaw(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder builder = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            in.close();
            return builder.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void downloadAndLoad(String link) {
        try {
            File file = getFile(link);

            Plugin pl = Bukkit.getPluginManager().loadPlugin(file);
            if (pl != null) {
                pl.onLoad();
                Bukkit.getPluginManager().enablePlugin(pl);
            } else {
                plugin.getLogger().warning("Ошибка загрузки плагина!");
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static @NotNull File getFile(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);

        File pluginDir = new File("plugins");
        if (!pluginDir.exists()) pluginDir.mkdirs();

        String fileName = new File(url.getPath()).getName();
        if (!fileName.endsWith(".jar")) fileName += ".jar";

        File file = new File(pluginDir, fileName);

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return file;
    }

}

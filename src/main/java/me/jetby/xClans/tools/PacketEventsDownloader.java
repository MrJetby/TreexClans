package me.jetby.xClans.tools;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class PacketEventsDownloader{

    private final JavaPlugin plugin;


    public PacketEventsDownloader(JavaPlugin plugin) {
        this.plugin = plugin;

        if (Bukkit.getPluginManager().getPlugin("packetevents") == null || !Bukkit.getPluginManager().getPlugin("packetevents").isEnabled()) {
            downloadAndLoad("https://api.spiget.org/v2/resources/80279/download");
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

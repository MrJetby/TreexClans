package me.jetby.treexclans.tools;


import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.configurations.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormatTime {

    private final Config config;
    private final Map<String, List<String>> cachedFormats;

    public FormatTime(TreexClans plugin) {
        this.config = plugin.getCfg();
        this.cachedFormats = new HashMap<>();

        FileConfiguration configuration = plugin.getLang().getConfig();

        ConfigurationSection formattedTime = configuration.getConfigurationSection("formattedTime");

        cachedFormats.put("weeks", formattedTime.getStringList("weeks"));
        cachedFormats.put("days", formattedTime.getStringList("days"));
        cachedFormats.put("hours", formattedTime.getStringList("hours"));
        cachedFormats.put("minutes", formattedTime.getStringList("minutes"));
        cachedFormats.put("seconds", formattedTime.getStringList("seconds"));
    }

    public String stringFormat(int totalSeconds) {
        int weeks = totalSeconds / (7 * 24 * 3600);
        int days = (totalSeconds % (7 * 24 * 3600)) / (24 * 3600);
        int hours = (totalSeconds % (24 * 3600)) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        Map<String, String> timeUnits = new HashMap<>(6, 100);

        timeUnits.put("%weeks%", formatUnit(weeks, cachedFormats.get("weeks")));
        timeUnits.put("%days%", formatUnit(days, cachedFormats.get("days")));
        timeUnits.put("%hours%", formatUnit(hours, cachedFormats.get("hours")));
        timeUnits.put("%minutes%", formatUnit(minutes, cachedFormats.get("minutes")));
        timeUnits.put("%seconds%", formatUnit(seconds, cachedFormats.get("seconds")));

        String format = config.getFormattedTimeFormat();

        for (Map.Entry<String, String> entry : timeUnits.entrySet()) {
            format = format.replace(entry.getKey(), entry.getValue());
        }
        format = format.trim();
        if (format.isEmpty()) {
            return "0";
        }

        return format;
    }

    public String stringFormat(long totalSeconds) {
        int weeks = ((int) totalSeconds / 1000) / (7 * 24 * 3600);
        int days = (((int) totalSeconds / 1000) % (7 * 24 * 3600)) / (24 * 3600);
        int hours = (((int) totalSeconds / 1000) % (24 * 3600)) / 3600;
        int minutes = (((int) totalSeconds / 1000) % 3600) / 60;
        int seconds = ((int) totalSeconds / 1000) % 60;

        Map<String, String> timeUnits = new HashMap<>(6, 100);

        timeUnits.put("%weeks%", formatUnit(weeks, cachedFormats.get("weeks")));
        timeUnits.put("%days%", formatUnit(days, cachedFormats.get("days")));
        timeUnits.put("%hours%", formatUnit(hours, cachedFormats.get("hours")));
        timeUnits.put("%minutes%", formatUnit(minutes, cachedFormats.get("minutes")));
        timeUnits.put("%seconds%", formatUnit(seconds, cachedFormats.get("seconds")));

        String format = config.getFormattedTimeFormat();

        for (Map.Entry<String, String> entry : timeUnits.entrySet()) {
            format = format.replace(entry.getKey(), entry.getValue());
        }
        format = format.trim();
        if (format.isEmpty()) {
            return "0";
        }

        return format;
    }

    private String formatUnit(int value, List<String> forms) {
        if (value == 0 || forms == null || forms.size() < 3) {
            return "";
        }

        value = Math.abs(value);
        int remainder10 = value % 10;
        int remainder100 = value % 100;

        String result;

        if (remainder10 == 1 && remainder100 != 11) {
            result = value + " " + forms.get(0);
        } else if (remainder10 >= 2 && remainder10 <= 4 && (remainder100 < 10 || remainder100 >= 20)) {
            result = value + " " + forms.get(1);
        } else {
            result = value + " " + forms.get(2);
        }
        return result;
    }
}
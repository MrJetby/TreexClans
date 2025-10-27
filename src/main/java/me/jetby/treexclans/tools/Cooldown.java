package me.jetby.treexclans.tools;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Cooldown {

    private final TreexClans plugin = TreexClans.getInstance();

    private final Map<String, Integer> cooldowns = new HashMap<>();

    public boolean isOnCooldown(String key) {
        return cooldowns.containsKey(key);
    }
    public void setCooldown(String key, int seconds) {
        cooldowns.put(key, seconds);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task -> {
            var cd = cooldowns.get(key);
            if (cd==null) {
                task.cancel();
                return;
            }
            int timeLeft = cd - 1;
            if (timeLeft <= 0) {
                cooldowns.remove(key);
                task.cancel();
            } else {
                cooldowns.put(key, timeLeft);
            }
        }, 20L, 20L);
    }
    public void removeCooldown(String key) {
        cooldowns.remove(key);
    }
    public int getCooldown(String key) {
        return cooldowns.getOrDefault(key, 0) / 20;
    }
}

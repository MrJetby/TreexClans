package me.jetby.treexclans.tools;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple cooldown system.
 * <p>
 * The idea is to get the cooldown info only when you call the method.
 *
 * @author MrJetby
 **/

@UtilityClass
public class Cooldown {
    private final Map<String, Cooldowns> cooldowns = new HashMap<>();

    public boolean isOnCooldown(String key) {
        var cd = cooldowns.get(key);
        if (cd == null) return false;

        long elapsed = (System.currentTimeMillis() - cd.timestamp()) / 1000;
        if (elapsed >= cd.seconds()) {
            cooldowns.remove(key);
            return false;
        }

        return true;
    }


    public void setCooldown(String key, int seconds) {
        cooldowns.put(key, new Cooldowns(seconds, System.currentTimeMillis()));
    }

    public void removeCooldown(String key) {
        cooldowns.remove(key);
    }

    private record Cooldowns(int seconds, long timestamp) {
    }
}

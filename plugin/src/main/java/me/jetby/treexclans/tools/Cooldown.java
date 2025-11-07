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
        if (cd != null) {
            int sec = cd.seconds();
            int goal = ((int) (System.currentTimeMillis() - cd.timestamp()) / 1000);
            if (sec >= goal) {
                cooldowns.remove(key);
                return true;
            }
        }
        return false;
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

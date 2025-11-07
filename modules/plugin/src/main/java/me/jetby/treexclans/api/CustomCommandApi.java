package me.jetby.treexclans.api;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.jetby.treexclans.commands.Subcommand;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class CustomCommandApi {

    @Getter
    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public void register(String name, Subcommand subcommand) {
        String lowerName = name.toLowerCase();
        subcommands.put(lowerName, subcommand);
    }

    public void unregister(String name) {
        subcommands.remove(name);
    }

    public enum CommandType {
        CLAN,
        ADMIN
    }

}

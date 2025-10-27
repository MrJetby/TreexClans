package me.jetby.treexclans.configurations;

import lombok.Getter;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;


public class Lang {

    @Getter
    private FileConfiguration config;

    public Lang(TreexClans plugin, String lang) {
        File langFolder = new File(plugin.getDataFolder(), "messages");


        File[] files = langFolder.listFiles();

        String[] defaults = {"ru.yml", "en.yml", "es.yml", "de.yml", "zh.yml", "uk.yml"};

        for (String name : defaults) {
            File target = new File(langFolder, name);

            if (!target.exists()) {
                plugin.saveResource("messages/" + name, false);
                FileConfiguration configuration = YamlConfiguration.loadConfiguration(target);
                String foundedLang = configuration.getString("lang");
                if (foundedLang == null) continue;
                if (!foundedLang.equalsIgnoreCase(lang)) continue;
                this.config = configuration;
                break;
            }

        }

        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            String foundedLang = configuration.getString("lang");
            if (foundedLang == null) continue;
            if (!foundedLang.equalsIgnoreCase(lang)) continue;
            this.config = configuration;
            break;
        }
    }
    public void sendMessage(Player player, Clan clan, String path) {
        ActionContext ctx = new ActionContext(player);
        ctx.put("clan", clan);

        List<String> actions = config.getStringList(path).stream()
                .map(str -> str.replace("{prefix}", config.getString("prefix", "")))
                .toList();

        ActionExecutor.execute(ctx, ActionRegistry.transform(actions));
    }

    public void sendMessage(Player player, Clan clan, String path, ReplaceString replace) {
        ActionContext ctx = new ActionContext(player);
        ctx.put("clan", clan);

        List<String> actions = config.getStringList(path).stream()
                .map(str -> str
                        .replace("{prefix}", config.getString("prefix", ""))
                        .replace(replace.target(), replace.replacement()))
                .toList();

        ActionExecutor.execute(ctx, ActionRegistry.transform(actions));
    }

    public void sendMessage(Player player, Clan clan, String path, List<ReplaceString> replaceStrings) {
        ActionContext ctx = new ActionContext(player);
        ctx.put("clan", clan);

        List<String> actions = config.getStringList(path).stream()
                .map(str -> {
                    String replaced = str.replace("{prefix}", config.getString("prefix", ""));
                    for (ReplaceString replace : replaceStrings) {
                        replaced = replaced.replace(replace.target(), replace.replacement());
                    }
                    return replaced;
                })
                .toList();

        ActionExecutor.execute(ctx, ActionRegistry.transform(actions));
    }

    public List<String> getMessageList(String path) {
        return config.getStringList(path);
    }

    public record ReplaceString(String target, String replacement){}
}

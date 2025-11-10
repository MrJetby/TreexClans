package me.jetby.treexclans.configurations;

import lombok.Getter;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Colorize;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.tools.FileLoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;


public class Messages {

    @Getter
    private final FileConfiguration config = FileLoader.getFileConfiguration("messages.yml");

    public void sendMessage(Player player, Clan clan, String path) {
        ActionContext ctx = new ActionContext(player);
        ctx.put("clan", clan);
        String prefix = config.getString("prefix", "");

        List<String> actions = config.getStringList(path).stream()
                .map(str -> str.replace("{prefix}", prefix))
                .toList();

        ActionExecutor.execute(ctx, ActionRegistry.transform(actions));
    }

    public void sendMessage(Player player, Clan clan, String path, ReplaceString... replaceStrings) {
        ActionContext ctx = new ActionContext(player);
        ctx.put("clan", clan);

        String prefix = config.getString("prefix", "");
        List<String> actions = config.getStringList(path).stream()
                .map(str -> {
                    String replaced = str.replace("{prefix}", prefix);
                    for (ReplaceString replace : replaceStrings) {
                        replaced = replaced.replace(replace.target(), replace.replacement());
                    }
                    return replaced;
                })
                .toList();

        ActionExecutor.execute(ctx, ActionRegistry.transform(actions));
    }

    public String getMessage(String path) {
        return Colorize.text(config.getString(path, path));
    }

    public List<String> getMessageList(String path) {
        return config.getStringList(path);
    }

    public record ReplaceString(String target, String replacement) {
    }
}

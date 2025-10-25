package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.tools.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Invite implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) {
                sender.sendMessage("§cYou are not in a clan.");
                return true;
            }
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (!clan.getMember(player.getUniqueId()).getRank().rankPermissions().invite()) {
                player.sendMessage("You are not allowed to do that! Please ask your clan leader to give you permission");
                return true;
            }
            Player target = player.getServer().getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return true;
            }

            if (plugin.getClanManager().isInClan(target.getUniqueId())) {
                player.sendMessage("§cThat player is already in a clan.");
                return true;
            }
            if (Cooldown.isOnCooldown("invite_"+target.getUniqueId()+"_"+clan.getId())) {
                player.sendMessage("§cYou must wait before inviting that player again.");
                return true;
            } else {
                Cooldown.setCooldown("invite_"+target.getUniqueId()+"_"+clan.getId(), 60);
                target.sendMessage("§aYou have been invited to join " + clan.getId() + " by " + player.getName() + ". Use /clan accept " + clan.getId() + " to join.");
            }
            return true;
        }

        return true;
    }


    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length>0) {
            return new ArrayList<>((Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())));
        }
        return List.of();
    }
}

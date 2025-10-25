package me.jetby.xClans.commands.clan.args;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Member;
import me.jetby.xClans.tools.Cooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Accept implements Subcommand {
    private final TreexClans plugin = TreexClans.getInstance();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        if (sender instanceof Player player) {
            if (plugin.getClanManager().isInClan(player.getUniqueId())) {
                sender.sendMessage("§cYou are already in a clan.");
                return true;
            }
            if (!plugin.getClanManager().clanExists(args[0])) {
                sender.sendMessage("§cThat clan does not exist.");
                return true;
            }
            if (!Cooldown.isOnCooldown("invite_"+player.getUniqueId()+"_"+args[0])) {
                sender.sendMessage("§cYou have no pending clan invites.");
                return true;
            } else {
                Cooldown.removeCooldown("invite_"+player.getUniqueId()+"_"+args[0]);
                Clan clan = plugin.getClanManager().getClan(args[0]);
                Member member = new Member(player.getUniqueId(), plugin.getCfg().getDefaultRank(), System.currentTimeMillis(), System.currentTimeMillis() ,false);
                clan.addMember(member);
                sender.sendMessage("§aYou have joined the clan!");
            }
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabCompleter(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }
}

package me.jetby.treexclans;

import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.api.events.OnClanCreate;
import me.jetby.treexclans.api.events.OnClanDelete;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.api.service.clan.level.Level;
import me.jetby.treexclans.clan.ClanImpl;
import me.jetby.treexclans.configurations.Lang;
import me.jetby.treexclans.gui.requirements.Requirements;
import me.jetby.treexclans.gui.requirements.SimpleRequirement;
import me.jetby.treexclans.clan.MemberImpl;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Concrete implementation of {@link ClanManager} for TreexClans.
 * <p>
 * External plugins should use the {@link ClanManager} API interface,
 * this class is internal wiring between plugin config and API.
 */
public final class ClanManagerImpl implements Listener, ClanManager {

    private final TreexClans plugin;

    private final Lifecycle lifecycle = new LifecycleImpl();
    private final Validation validation = new ValidationImpl();
    private final Chat chat = new ChatImpl();
    private final Economy economy = new EconomyImpl();
    private final Colors colors = new ColorsImpl();
    private final Lookup lookup = new LookupImpl();

    public ClanManagerImpl(@NotNull TreexClans plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public @NotNull Lifecycle lifecycle() {
        return lifecycle;
    }

    @Override
    public @NotNull Validation validation() {
        return validation;
    }

    @Override
    public @NotNull Chat chat() {
        return chat;
    }

    @Override
    public @NotNull Economy economy() {
        return economy;
    }

    @Override
    public @NotNull Colors colors() {
        return colors;
    }

    @Override
    public @NotNull Lookup lookup() {
        return lookup;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Clan clan = lookup.getClanByMember(uuid);
            if (clan == null) return;
            Member member = clan.getMember(uuid);
            if (member instanceof MemberImpl impl) {
                impl.setLastOnline(System.currentTimeMillis());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Clan> clans() {
        // cfg.getClans() is assumed to be Map<String, clan>
        return plugin.getCfg().getClans();
    }

    private boolean exists(@NotNull String name) {
        return clans().containsKey(name);
    }

    private final class LifecycleImpl implements ClanManager.Lifecycle {

        @Override
        public boolean createClan(@NotNull String name, @NotNull Clan clan) {
            if (exists(name)) return false;
            clans().put(name, clan);
            Bukkit.getPluginManager().callEvent(new OnClanCreate(clan, null));
            return true;
        }

        @Override
        public boolean createClan(@NotNull String name, @NotNull Player leaderPlayer) {
            if (exists(name)) return false;

            MemberImpl leader = new MemberImpl(
                    leaderPlayer.getUniqueId(),
                    plugin.getCfg().getLeaderRank(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    false,
                    false,
                    0,
                    0,
                    new HashMap<>(),
                    0,
                    0
            );

            Level baseLevel = plugin.getCfg().getLevels().getOrDefault(
                    1,
                    new Level("1", 0, 1, 0, 1, new ArrayList<>(), new ArrayList<>())
            );

            var clan = new ClanImpl(
                    name,
                    null,
                    leader,
                    new HashSet<>(),
                    plugin.getCfg().getDefaultRanks(),
                    baseLevel,
                    0.0,
                    null,
                    0,
                    false,
                    new HashMap<>(),
                    new HashMap<>(),
                    new ArrayList<>(),
                    ""
            );

            clans().put(name, clan);
            Bukkit.getPluginManager().callEvent(new OnClanCreate(clan, leaderPlayer));
            return true;
        }

        @Override
        public void deleteClan(@NotNull Clan clan, @Nullable Player initiator) {
            //        for (Member member : clan.getMembers()) {
//            Player player = Bukkit.getPlayer(member.getUuid());
//            if (player != null) {
//                player.sendMessage("Your clan was disbanded by clan leader");
//            }
//        }
            clans().remove(clan.getId());
            Bukkit.getPluginManager().callEvent(new OnClanDelete(clan, initiator));
        }

        @Override
        public boolean deleteClan(@NotNull String name) {
            var clan = clans().get(name);
            if (clan == null) {
                return false;
            }

            // notify members
            for (var member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage("Your clan was disbanded by clan leader");
                }
            }

            clans().remove(clan.getId());
            Bukkit.getPluginManager().callEvent(new OnClanDelete(clan, null));
            return true;
        }

        @Override
        public boolean clanExists(@NotNull String name) {
            return exists(name);
        }
    }

    private final class ValidationImpl implements ClanManager.Validation {

        @Override
        public boolean isAllowedName(@NotNull Player player, @NotNull String clanName) {
            int min = plugin.getCfg().getMinTagLength();
            int max = plugin.getCfg().getMaxTagLength();

            if (clanName.length() < min) {
                plugin.getLang().sendMessage(player, null, "clan-tag-too-short",
                        new Lang.ReplaceString("{min_length}", String.valueOf(min)));
                return false;
            }

            if (clanName.length() > max) {
                plugin.getLang().sendMessage(player, null, "clan-tag-too-long",
                        new Lang.ReplaceString("{max_length}", String.valueOf(max)));
                return false;
            }

            if (plugin.getCfg().getBlockedTags().contains(clanName.toLowerCase())) {
                plugin.getLang().sendMessage(player, null, "clan-tag-blocked");
                return false;
            }

            if (!isAllowedRegex(clanName, plugin.getCfg().getRegex())) {
                plugin.getLang().sendMessage(player, null, "disallowed-tag-regex");
                return false;
            }

            // Requirements (money, perms, etc.)
            for (SimpleRequirement requirement : plugin.getCfg().getRequirements()) {
                if (!Requirements.check(player, requirement)) {
                    ActionContext ctx = new ActionContext(player);
                    List<String> deny = requirement.denyActions().stream()
                            .map(line -> Papi.setPapi(player, line))
                            .map(line -> line.replace("{name}", clanName))
                            .toList();
                    ActionExecutor.execute(ctx, ActionRegistry.transform(deny));
                    return false;
                } else {
                    List<String> ok = requirement.actions().stream()
                            .map(line -> line.replace("{name}", clanName))
                            .toList();
                    ActionExecutor.execute(new ActionContext(player), ActionRegistry.transform(ok));
                }
            }

            return true;
        }

        @Override
        public boolean isAllowedPrefix(@NotNull Player player, @NotNull String prefix) {
            String cleaned = removeIgnoredSymbols(prefix, plugin.getCfg().getLengthIgnoredSymbols());
            int min = plugin.getCfg().getPrefixMinLength();
            int max = plugin.getCfg().getPrefixMaxLength();

            if (cleaned.length() < min) {
                plugin.getLang().sendMessage(player, null, "clan-prefix-too-short",
                        new Lang.ReplaceString("{min_length}", String.valueOf(min)));
                return false;
            }

            if (cleaned.length() > max) {
                plugin.getLang().sendMessage(player, null, "clan-prefix-too-long",
                        new Lang.ReplaceString("{max_length}", String.valueOf(max)));
                return false;
            }

            if (plugin.getCfg().getBlockedTags().contains(prefix.toLowerCase())) {
                plugin.getLang().sendMessage(player, null, "clan-tag-blocked");
                return false;
            }

            if (!isAllowedRegex(prefix, plugin.getCfg().getPrefixRegex())) {
                plugin.getLang().sendMessage(player, null, "disallowed-prefix-regex");
                return false;
            }

            return true;
        }

        @Override
        public boolean isAllowedRegex(@NotNull String text, @NotNull String regex) {
            return text.matches(regex);
        }

        private String removeIgnoredSymbols(String input, List<String> ignored) {
            String result = input;
            for (String token : ignored) {
                if (token != null && !token.isEmpty()) {
                    result = result.replace(token, "");
                }
            }
            return result;
        }
    }

    private final class ChatImpl implements ClanManager.Chat {

        @Override
        public void sendMessage(@NotNull Clan clan, @NotNull String message) {

            for (var member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage(message);
                }
            }

            var leader = clan.getLeader();
            Player leaderPlayer = Bukkit.getPlayer(leader.getUuid());
            if (leaderPlayer != null) {
                leaderPlayer.sendMessage(message);
            }
        }

        @Override
        public void sendChat(@NotNull Clan clan, @NotNull Player sender, @NotNull String message) {
            String format = plugin.getCfg().getChatFormat()
                    .replace("{player}", sender.getName())
                    .replace("{message}", message);

            String colored = Colorize.text(format);

            for (var member : clan.getMembers()) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    player.sendMessage(colored);
                }
            }

            var leader = clan.getLeader();
            Player leaderPlayer = Bukkit.getPlayer(leader.getUuid());
            if (leaderPlayer != null) {
                leaderPlayer.sendMessage(colored);
            }
        }
    }

    private static final class EconomyImpl implements ClanManager.Economy {

        @Override
        public void addBalance(double amount, @NotNull Clan clan) {
            clan.setBalance(clan.getBalance() + amount);
        }

        @Override
        public void takeBalance(double amount, @NotNull Clan clan) {
            clan.setBalance(clan.getBalance() - amount);
        }

        @Override
        public double getBalance(@NotNull Clan clan) {
            return clan.getBalance();
        }
    }

    private static final class ColorsImpl implements ClanManager.Colors {

        @Override
        public void setColor(@NotNull Clan clan, @NotNull Member member, @NotNull Color color) {

            Map<UUID, Color> colors = member.getGlowColors();
            var scope = new HashSet<>(clan.getMembers());
            if (!clan.getLeader().equals(member)) {
                scope.add(clan.getLeader());
            }

            for (var target : scope) {
                if (!target.equals(member)) {
                    colors.put(target.getUuid(), color);
                }
            }

            member.setGlowColors(colors);
        }

        @Override
        public void setColor(@NotNull Member member, @NotNull Set<Member> members, @NotNull Color color) {
            Map<UUID, Color> colors = member.getGlowColors();

            for (Member raw : members) {
                if (!raw.equals(member)) {
                    colors.put(raw.getUuid(), color);
                }
            }

            member.setGlowColors(colors);
        }

        @Override
        public void setColor(@NotNull Member member, @NotNull Member target, @NotNull Color color) {
            member.getGlowColors().put(target.getUuid(), color);
        }
    }

    private final class LookupImpl implements ClanManager.Lookup {

        @Override
        public boolean isInClan(@NotNull UUID uuid) {
            return clans().values().stream()
                    .anyMatch(clan ->
                            (clan.getLeader() != null && clan.getLeader().getUuid().equals(uuid)) ||
                                    clan.getMembers().stream().anyMatch(m -> m.getUuid().equals(uuid))
                    );
        }

        @Override @Deprecated(since = "Это так не работает если не ошибаюсь")
        public boolean isInClan(@NotNull String uuidString) {
            try {
                return isInClan(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public @Nullable Clan getClan(@NotNull String name) {
            return clans().get(name);
        }

        @Override
        public @Nullable Clan getClanByMember(@NotNull UUID uuid) {
            return clans().values().stream()
                    .filter(clan ->
                            (clan.getLeader() != null && clan.getLeader().getUuid().equals(uuid)) ||
                                    clan.getMembers().stream().anyMatch(m -> m.getUuid().equals(uuid))
                    )
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public @Nullable Clan getClanByMember(@NotNull String uuidString) {
            try {
                return getClanByMember(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public @Nullable Clan getClanByMember(@NotNull Member member) {
            return clans().values().stream()
                    .filter(clan ->
                            (clan.getLeader() != null && clan.getLeader().equals(member)) ||
                                    clan.getMembers().contains(member)
                    )
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public @NotNull String getLastOnlineFormatted(@NotNull UUID uuid) {
            if (!isInClan(uuid)) {
                return "-1";
            }

            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            ClanImpl clan = (ClanImpl) getClanByMember(uuid);
            if (clan == null) {
                return "-1";
            }

            var member = clan.getMember(uuid);
            if (member == null) {
                return "-1";
            }

            if (offline.isOnline()) {
                member.setLastOnline(System.currentTimeMillis());
                return "В сети";
            }

            long diff = System.currentTimeMillis() - member.getLastOnline();
            return plugin.getFormatTime().stringFormat(diff);
        }

        @Override
        public @NotNull String getLastOnlineFormatted(@NotNull Member member) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(member.getUuid());

            if (offline.isOnline()) {
                member.setLastOnline(System.currentTimeMillis());
                return "В сети";
            }

            long diff = System.currentTimeMillis() - member.getLastOnline();
            return plugin.getFormatTime().stringFormat(diff);
        }
    }
}

package me.jetby.treexclans.storage;

import me.jetby.treex.bukkit.LocationHandler;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.clan.ClanImpl;
import me.jetby.treexclans.clan.MemberImpl;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.tools.FileLoader;
import me.jetby.treexclans.tools.ItemSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class YAML implements Storage {

    private final TreexClans plugin;
    private final File file;
    private final FileConfiguration configuration;

    public YAML(TreexClans plugin) {
        this.plugin = plugin;
        this.configuration = FileLoader.getFileConfiguration("storage.yml");
        this.file = FileLoader.getFile("storage.yml");
    }

    @Override
    public void load() {

        for (String clanId : configuration.getKeys(false)) {
            if (clanId.equals("clan-id")) continue;
            ConfigurationSection clan = configuration.getConfigurationSection(clanId);
            if (clan == null) continue;

            String prefix = clan.getString("prefix");
            Set<Member> memberImplSet = new HashSet<>();
            double balance = clan.getDouble("balance", 0.0);
            String level = clan.getString("level", "1");
            int clanExp = clan.getInt("exp", 0);
            boolean pvp = clan.getBoolean("pvp", false);
            String slogan = clan.getString("slogan", "");


            String leaderUUID = clan.getString("leader.uuid");
            if (leaderUUID == null || leaderUUID.isEmpty()) {
                plugin.getLogger().warning("Clan " + clanId + " has no leader UUID in storage.yml!");
                continue;
            }

            Map<String, Rank> ranks = new HashMap<>();
            ConfigurationSection ranksSection = clan.getConfigurationSection("ranks");
            if (ranksSection != null) {

                for (String key : ranksSection.getKeys(false)) {
                    String displayName = ranksSection.getString(key + ".display-name");
                    ConfigurationSection permission = ranksSection.getConfigurationSection(key + ".permissions");
                    if (permission == null) continue;
                    Set<RankPerms> perms = new HashSet<>();
                    for (String perm : permission.getKeys(false)) {
                        switch (perm.toLowerCase()) {
                            case "invite" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.INVITE);
                            }
                            case "kick" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.KICK);
                            }
                            case "base" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.BASE);
                            }
                            case "setbase" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETBASE);
                            }
                            case "setrank" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETRANK);
                            }
                            case "deposit" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.DEPOSIT);
                            }
                            case "withdraw" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.WITHDRAW);
                            }
                            case "pvp" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.PVP);
                            }
                            case "setslogan" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETSLOGAN);
                            }
                            case "setprefix" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETPREFIX);
                            }
                        }


                    }
                    ranks.put(key.toLowerCase(), new Rank(key.toLowerCase(), displayName, perms));
                }
            } else {
                ranks.putAll(plugin.getCfg().getDefaultRanks());
            }
            ConfigurationSection leaderSection = clan.getConfigurationSection("leader");
            if (leaderSection == null) continue;
            MemberImpl leader = getMember(leaderUUID, leaderSection, ranks);

            ConfigurationSection members = clan.getConfigurationSection("members");
            if (members != null) {
                for (String key : members.getKeys(false)) {
                    ConfigurationSection member = members.getConfigurationSection(key);
                    if (member == null) continue;
                    memberImplSet.add(getMember(key, member, ranks));
                }
            }

            Location base = LocationHandler.deserialize(clan.getString("base-location"));

            List<ItemStack> chestItems = new ArrayList<>();
            List<String> itemsStr = clan.getStringList("chest");
            for (String base64 : itemsStr) {
                try {
                    chestItems.add(ItemSerializer.itemFromBase64(base64));
                } catch (Exception e) {
                    LOGGER.warn("Не удалось загрузить предмет из " + clanId + ": " + e.getMessage());
                }
            }


            Map<UUID, Map<String, Integer>> questsInProgress = new HashMap<>();
            ConfigurationSection progress = clan.getConfigurationSection("quests-progress");

            if (progress != null) {
                for (String questId : progress.getKeys(false)) {
                    ConfigurationSection playersInProgress = progress.getConfigurationSection(questId);
                    if (playersInProgress == null) continue;

                    for (String id : playersInProgress.getKeys(false)) {
                        UUID uuid = UUID.fromString(id);
                        int value = playersInProgress.getInt(id, 0);

                        Map<String, Integer> playerMap = questsInProgress.getOrDefault(uuid, new HashMap<>());
                        playerMap.put(questId, value);
                        questsInProgress.put(uuid, playerMap);
                    }
                }
            }

            Map<UUID, List<String>> completedQuests = new HashMap<>();
            ConfigurationSection quests = clan.getConfigurationSection("quests-completed");
            if (quests != null) {
                for (String uid : quests.getKeys(false)) {
                    try {
                        completedQuests.put(UUID.fromString(uid), quests.getStringList(uid));
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }


            plugin.getCfg().getClans().put(clanId, new ClanImpl(clanId, prefix, leader, memberImplSet, ranks,
                    plugin.getCfg().getLevels().get(Integer.parseInt(level)),
                    balance, base, clanExp, pvp, questsInProgress, completedQuests, chestItems, slogan));
        }
    }

    @Override
    public void save() {

        try {
            for (String key : configuration.getKeys(false)) {
                configuration.set(key, null);
            }
            for (String clanId : plugin.getCfg().getClans().keySet()) {
                var clanImpl = plugin.getCfg().getClans().get(clanId);

                for (String key : clanImpl.getRanks().keySet()) {
                    Rank rank = clanImpl.getRanks().get(key);
                    configuration.set(clanId + ".ranks." + rank.id() + ".display-name", rank.name());
                    Set<RankPerms> perms = rank.perms();
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.ALWAYS", true);
                    for (RankPerms perm : perms) {
                        configuration.set(clanId + ".ranks." + rank.id() + ".permissions." + perm.name(), true);
                    }
                }

                configuration.set(clanId + ".slogan", clanImpl.getSlogan());
                configuration.set(clanId + ".balance", clanImpl.getBalance());
                configuration.set(clanId + ".level", clanImpl.getLevel().id());
                configuration.set(clanId + ".exp", clanImpl.getExp());
                configuration.set(clanId + ".pvp", clanImpl.isPvp());

                for (UUID uuid : clanImpl.getQuestsProgress().keySet()) {
                    Map<String, Integer> map = clanImpl.getQuestsProgress().get(uuid);
                    if (map != null) {
                        for (String key : map.keySet()) {
                            configuration.set(clanId + ".quests-progress." + key + "." + uuid.toString(), map.get(key));
                        }

                    }
                }

                for (Map.Entry<UUID, List<String>> entry : clanImpl.getCompletedQuest().entrySet()) {
                    configuration.set(clanId + ".quests-completed." + entry.getKey().toString(), entry.getValue());
                }


                var leader = clanImpl.getLeader();
                configuration.set(clanImpl.getId() + ".leader.uuid", leader.getUuid().toString());
                setMember(leader, clanImpl, "leader");

                for (var memberImpl : clanImpl.getMembers()) {
                    setMember(memberImpl, clanImpl, "members." + memberImpl.getUuid());
                }

                configuration.set(clanId + ".chest",
                        clanImpl.getChest().stream()
                                .map(ItemSerializer::itemToBase64)
                                .toList());


                Location location = clanImpl.getBase();
                if (location != null) {
                    configuration.set(clanId + ".base-location", LocationHandler.serialize(clanImpl.getBase()));
                } else {
                    configuration.set(clanId + ".base-location", null);
                }
            }
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MemberImpl getMember(String key, ConfigurationSection member, Map<String, Rank> ranks) {
        if (member == null) return null;
        UUID uuid = UUID.fromString(key);
        Rank rank = ranks.get(member.getString("rank"));
        long joinedAt = member.getLong("joined-at");
        long lastOnline = member.getLong("last-online");
        boolean glow = member.getBoolean("clan-glow", false);
        int coin = member.getInt("coin", 0);
        int exp = member.getInt("exp", 0);
        Map<UUID, Color> colors = new HashMap<>();
        for (String str : member.getStringList("glow-colors")) {
            String[] args = str.split(";");
            if (args.length < 4) continue;
            UUID id = UUID.fromString(args[0]);
            int r = Integer.parseInt(args[1]);
            int g = Integer.parseInt(args[2]);
            int b = Integer.parseInt(args[3]);
            Color color = Color.fromRGB(r, g, b);
            colors.put(id, color);
        }

        return new MemberImpl(uuid, rank, joinedAt, lastOnline, glow, false, coin, exp, colors,
                member.getInt("kills", 0),
                member.getInt("deaths", 0)
        );
    }

    private void setMember(Member memberImpl, Clan clanImpl, String path) {
        configuration.set(clanImpl.getId() + "." + path + ".rank", memberImpl.getRank().id());
        configuration.set(clanImpl.getId() + "." + path + ".joined-at", memberImpl.getJoinedAt());
        configuration.set(clanImpl.getId() + "." + path + ".last-online", memberImpl.getLastOnline());
        configuration.set(clanImpl.getId() + "." + path + ".clan-glow", memberImpl.isClanGlow());

        configuration.set(clanImpl.getId() + "." + path + ".coin", memberImpl.getCoin());
        configuration.set(clanImpl.getId() + "." + path + ".exp", memberImpl.getExp());
        configuration.set(clanImpl.getId() + "." + path + ".kills", memberImpl.getKills());
        configuration.set(clanImpl.getId() + "." + path + ".deaths", memberImpl.getDeaths());
        List<String> colors = new ArrayList<>();
        for (Map.Entry<UUID, Color> entry : memberImpl.getGlowColors().entrySet()) {
            UUID id = entry.getKey();
            Color color = entry.getValue();
            colors.add(id + ";" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue());
        }

        configuration.set(clanImpl.getId() + "." + path + ".glow-colors", colors);
    }
}

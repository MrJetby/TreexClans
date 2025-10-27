package me.jetby.treexclans.storage;

import me.jetby.treex.bukkit.LocationHandler;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Level;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.clan.rank.Rank;
import me.jetby.treexclans.clan.rank.RankPermissions;
import me.jetby.treexclans.tools.FileLoader;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
            Set<Member> memberSet = new HashSet<>();
            double balance = clan.getDouble("balance", 0.0);
            String level = clan.getString("level", "1");
            int clanExp = clan.getInt("exp", 0);
            boolean pvp = clan.getBoolean("pvp", false);


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
                    ConfigurationSection perm = ranksSection.getConfigurationSection(key + ".permissions");
                    if (perm == null) continue;
                    RankPermissions rankPermissions = new RankPermissions(
                            perm.getBoolean("invite", false),
                            perm.getBoolean("kick", false),
                            perm.getBoolean("base", false),
                            perm.getBoolean("setbase", false),
                            perm.getBoolean("setrank", false),
                            perm.getBoolean("deposit", false),
                            perm.getBoolean("withdraw", false),
                            perm.getBoolean("pvp", false)
                    );
                    ranks.put(key, new Rank(key, displayName, rankPermissions));
                }
            } else {
                ranks.putAll(plugin.getCfg().getDefaultRanks());
            }
            ConfigurationSection leaderSection = clan.getConfigurationSection("leader");
            if (leaderSection == null) continue;
            Member leader = getMember(leaderUUID, leaderSection, ranks);

            ConfigurationSection members = clan.getConfigurationSection("members");
            if (members != null) {
                for (String key : members.getKeys(false)) {
                    ConfigurationSection member = members.getConfigurationSection(key);
                    if (member == null) continue;
                    memberSet.add(getMember(key, member, ranks));
                }
            }

            Location base = LocationHandler.deserialize(clan.getString("base-location"));

            Map<String, Integer> questsProgress = new HashMap<>();
            ConfigurationSection progress = clan.getConfigurationSection("quests-progress");
            if (progress != null) {
                for (String key : progress.getKeys(false)) {
                    if (plugin.getQuestsLoader().getQuests().get(key)==null) continue;
                    questsProgress.put(key, progress.getInt(key, 0));
                }
            }
            List<String> completedQuests = new ArrayList<>(clan.getStringList("quests-completed"));


            plugin.getCfg().getClans().put(clanId, new Clan(clanId, prefix, leader, memberSet, ranks, new ArrayList<>(),
                    new Level(Integer.parseInt(level)), balance, base, clanExp, pvp, questsProgress, completedQuests));
        }
    }

    @Override
    public void save() {

        try {
            for (String key : configuration.getKeys(false)) {
                configuration.set(key, null);
            }
            for (String clanId : plugin.getCfg().getClans().keySet()) {
                Clan clan = plugin.getCfg().getClans().get(clanId);

                for (String key : clan.getRanks().keySet()) {
                    Rank rank = clan.getRanks().get(key);
                    configuration.set(clanId + ".ranks." + rank.id() + ".display-name", rank.name());
                    RankPermissions perm = rank.rankPermissions();
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.invite", perm.invite());
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.kick", perm.kick());
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.base", perm.base());
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.setbase", perm.setbase());
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.setrank", perm.setrank());
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.deposit", perm.deposit());
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.withdraw", perm.withdraw());
                    configuration.set(clanId + ".ranks." + rank.id() + ".permissions.pvp", perm.pvp());
                }

                configuration.set(clanId + ".balance", clan.getBalance());
                configuration.set(clanId + ".level", clan.getLevel().id());
                configuration.set(clanId + ".exp", clan.getExp());
                configuration.set(clanId + ".pvp", clan.isPvp());

                for (Map.Entry<String, Integer> entry : clan.getQuestsProgress().entrySet()) {
                    configuration.set(clanId + ".quests-progress." + entry.getKey(), entry.getValue());
                }
                configuration.set(clanId + ".quests-completed", clan.getCompletedQuest());

                Member leader = clan.getLeader();
                configuration.set(clan.getId() + ".leader.uuid", leader.getUuid().toString());
                setMember(leader, clan, "leader");

                for (Member member : clan.getMembers()) {
                    setMember(member, clan, "members." + member.getUuid());
                }

                Location location = clan.getBase();
                if (location != null) {
                    configuration.set(clanId + ".base-location", LocationHandler.serialize(clan.getBase()));
                } else {
                    configuration.set(clanId + ".base-location", null);
                }
            }
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Member getMember(String key, ConfigurationSection member, Map<String, Rank> ranks) {
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

        return new Member(uuid, rank, joinedAt, lastOnline, glow, false, coin, exp, colors,
                member.getInt("kills", 0),
                member.getInt("deaths", 0),
                member.getInt("war-wins", 0),
                member.getInt("war-participated", 0),
                member.getInt("war-loses", 0));
    }

    private void setMember(Member member, Clan clan, String path) {
        configuration.set(clan.getId() + "." + path + ".rank", member.getRank().id());
        configuration.set(clan.getId() + "." + path + ".joined-at", member.getJoinedAt());
        configuration.set(clan.getId() + "." + path + ".last-online", member.getLastOnline());
        configuration.set(clan.getId() + "." + path + ".clan-glow", member.isClanGlow());

        configuration.set(clan.getId() + "." + path + ".kills", member.getKills());
        configuration.set(clan.getId() + "." + path + ".deaths", member.getDeaths());
        configuration.set(clan.getId() + "." + path + ".war-wins", member.getWarWins());
        configuration.set(clan.getId() + "." + path + ".war-participated", member.getWarParticipated());
        configuration.set(clan.getId() + "." + path + ".war-loses", member.getWarLoses());
        List<String> colors = new ArrayList<>();
        for (Map.Entry<UUID, Color> entry : member.getGlowColors().entrySet()) {
            UUID id = entry.getKey();
            Color color = entry.getValue();
            colors.add(id + ";" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue());
        }

        configuration.set(clan.getId() + "." + path + ".glow-colors", colors);
    }
}

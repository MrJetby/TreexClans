package me.jetby.xClans.storage;

import me.jetby.treex.bukkit.LocationHandler;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.clan.Clan;
import me.jetby.xClans.clan.Level;
import me.jetby.xClans.clan.Member;
import me.jetby.xClans.clan.rank.Rank;
import me.jetby.xClans.clan.rank.RankPermissions;
import me.jetby.xClans.tools.FileLoader;
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

            UUID leader_uuid = UUID.fromString(leaderUUID);
            Rank leader_rank = plugin.getCfg().getDefaultRanks().get(clan.getString("leader.rank"));
            long leader_joinedAt = clan.getLong("leader.joined-at");
            long leader_lastOnline = clan.getLong("leader.last-online");
            boolean leader_glow = clan.getBoolean("leader.clan-glow", false);
            int leader_coin = clan.getInt("leader.coin", 0);
            int leader_exp = clan.getInt("leader.exp", 0);
            Map<UUID, Color> leader_colors = new HashMap<>();
            for (String str : clan.getStringList("leader.glow-colors")) {
                String[] args = str.split(";");
                UUID id = UUID.fromString(args[0]);
                Color color = Color.fromBGR(Integer.parseInt(args[1]));
                leader_colors.put(id, color);
            }

            Member leader = new Member(
                    leader_uuid,
                    leader_rank,
                    leader_joinedAt,
                    leader_lastOnline,
                    leader_glow,
                    false,
                    leader_coin,
                    leader_exp,
                    leader_colors,
                    clan.getInt("leader.kills", 0),
                    clan.getInt("leader.deaths", 0),
                    clan.getInt("leader.war-wins", 0),
                    clan.getInt("leader.war-participated", 0),
                    clan.getInt("leader.war-loses", 0)
            );

            ConfigurationSection members = clan.getConfigurationSection("members");
            if (members != null) {
                for (String key : members.getKeys(false)) {
                    ConfigurationSection member = members.getConfigurationSection(key);
                    if (member == null) continue;
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
                        UUID id = UUID.fromString(args[0]);
                        Color color = Color.fromBGR(Integer.parseInt(args[1]));
                        colors.put(id, color);
                    }
                    memberSet.add(new Member(uuid, rank, joinedAt, lastOnline, glow, false, coin, exp, colors,
                            member.getInt("kills", 0),
                            member.getInt("deaths", 0),
                            member.getInt("war-wins", 0),
                            member.getInt("war-participated", 0),
                            member.getInt("war-loses", 0)));
                }
            }

            Location base = LocationHandler.deserialize(clan.getString("base-location"));

            plugin.getCfg().getClans().put(clanId, new Clan(clanId, prefix, leader, memberSet, ranks, new ArrayList<>(),
                    new Level(Integer.parseInt(level)), balance, base, clanExp, pvp));
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

                Member leader = clan.getLeader();
                configuration.set(clanId + ".leader.uuid", leader.getUuid().toString());
                configuration.set(clanId + ".leader.rank", leader.getRank().id());
                configuration.set(clanId + ".leader.joined-at", leader.getJoinedAt());
                configuration.set(clanId + ".leader.last-online", leader.getLastOnline());
                configuration.set(clanId + ".leader.clan-glow", leader.isClanGlow());

                configuration.set(clanId + ".leader.kills", leader.getKills());
                configuration.set(clanId + ".leader.deaths", leader.getDeaths());
                configuration.set(clanId + ".leader.war-wins", leader.getWarWins());
                configuration.set(clanId + ".leader.war-participated", leader.getWarParticipated());
                configuration.set(clanId + ".leader.war-loses", leader.getWarLoses());

                for (Member member : clan.getMembers()) {

                    configuration.set(clanId + ".members." + member.getUuid() + ".rank", member.getRank().id());
                    configuration.set(clanId + ".members." + member.getUuid() + ".joined-at", member.getJoinedAt());
                    configuration.set(clanId + ".members." + member.getUuid() + ".last-online", member.getLastOnline());
                    configuration.set(clanId + ".members." + member.getUuid() + ".clan-glow", member.isClanGlow());

                    configuration.set(clanId + ".members." + member.getUuid() + ".kills", leader.getKills());
                    configuration.set(clanId + ".members." + member.getUuid() + ".deaths", leader.getDeaths());
                    configuration.set(clanId + ".members." + member.getUuid() + ".war-wins", leader.getWarWins());
                    configuration.set(clanId + ".members." + member.getUuid() + ".war-participated", leader.getWarParticipated());
                    configuration.set(clanId + ".members." + member.getUuid() + ".war-loses", leader.getWarLoses());
                    List<String> colors = new ArrayList<>();
                    for (UUID key : member.getGlowColors().keySet()) {
                        Color color = member.getGlowColors().get(key);
                        colors.add(key + ";" + color.serialize());
                    }
                    configuration.set(clanId + ".members." + member.getUuid() + ".glow-colors", colors);

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
}

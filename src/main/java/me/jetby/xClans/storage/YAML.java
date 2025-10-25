package me.jetby.xClans.storage;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.configurations.ClansLoader;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Level;
import me.jetby.xClans.records.Member;
import me.jetby.xClans.records.rank.Rank;
import me.jetby.xClans.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class YAML implements Storage {

    private final TreexClans plugin;
    private final File file;
    private final FileConfiguration configuration;
    private final ClansLoader clansLoader;

    public YAML(TreexClans plugin) {
        this.plugin = plugin;
        this.configuration = FileLoader.getFileConfiguration("storage.yml");
        this.file = FileLoader.getFile("storage.yml");
        this.clansLoader = plugin.getClansLoader();
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


            String leaderUUID = clan.getString("leader.uuid");
            if (leaderUUID == null || leaderUUID.isEmpty()) {
                plugin.getLogger().warning("Clan " + clanId + " has no leader UUID in storage.yml!");
                continue;
            }
            UUID uuid = UUID.fromString(leaderUUID);

            Rank rank = plugin.getCfg().getDefaultRanks().get(clan.getString("leader.rank"));
            long joinedAt = clan.getLong("leader.joined-at");
            long lastOnline = clan.getLong("leader.last-online");
            boolean glow = clan.getBoolean("leader.clan-glow", false);

            Member leader = new Member(uuid, rank, joinedAt, lastOnline, glow);

            ConfigurationSection members = clan.getConfigurationSection("members");
            if (members != null) {

                for (String key : members.getKeys(false)) {
                    ConfigurationSection member = members.getConfigurationSection(key);
                    if (member == null) continue;
                    UUID uuid1 = UUID.fromString(key);
                    Rank rank1 = plugin.getCfg().getDefaultRanks().get(member.getString("rank"));
                    long joinedAt1 = member.getLong("joined-at");
                    long lastOnline1 = member.getLong("last-online");
                    boolean glow1 = member.getBoolean("clan-glow", false);
                    memberSet.add(new Member(uuid1, rank1, joinedAt1, lastOnline1, glow1));

                }
            }


            plugin.getClansLoader().getClans().put(clanId, new Clan(clanId, prefix, leader, memberSet, plugin.getCfg().getDefaultRanks(), null,
                    new Level(Integer.parseInt(level)), balance));
        }
    }

    @Override
    public void save() {

        try {
            for (String clanId : clansLoader.getClans().keySet()) {
                Clan clan = clansLoader.getClans().get(clanId);

                configuration.set(clanId + ".balance", clan.getBalance());
                configuration.set(clanId + ".level", clan.getLevel().id());

                Member leader = clan.getLeader();
                configuration.set(clanId + ".leader.uuid", leader.getUuid().toString());
                configuration.set(clanId + ".leader.rank", leader.getRank().id());
                configuration.set(clanId + ".leader.joined-at", leader.getJoinedAt());
                configuration.set(clanId + ".leader.last-online", leader.getLastOnline());
                configuration.set(clanId + ".leader.clan-glow", leader.isClanGlow());

                for (Member member : clan.getMembers()) {

                    configuration.set(clanId + ".members." + member.getUuid() + ".rank", member.getRank().id());
                    configuration.set(clanId + ".members." + member.getUuid() + ".joined-at", member.getJoinedAt());
                    configuration.set(clanId + ".members." + member.getUuid() + ".last-online", member.getLastOnline());
                    configuration.set(clanId + ".members." + member.getUuid() + ".clan-glow", member.isClanGlow());
                }
            }
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

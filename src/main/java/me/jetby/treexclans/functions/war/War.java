package me.jetby.treexclans.functions.war;

import lombok.Getter;
import lombok.Setter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class War {
    private final TreexClans plugin;
    private final Set<Clan> clans = new HashSet<>();
    
    public War(TreexClans plugin, Clan... clans) {
        this.plugin = plugin;
        this.clans.addAll(List.of(clans));
    }
}

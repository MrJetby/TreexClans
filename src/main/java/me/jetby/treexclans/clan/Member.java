package me.jetby.treexclans.clan;


import lombok.*;
import me.jetby.treexclans.clan.rank.Rank;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter @Setter
public class Member {
    private UUID uuid;
    private Rank rank;
    private long joinedAt;
    private long lastOnline;
    private boolean clanGlow;
    private boolean chat;
    private int coin;
    private int exp;
    private Map<UUID, Color> glowColors;
    private int kills;
    private int deaths;
    private int warWins;
    private int warParticipated;
    private int warLoses;

    public void addCoin(int a) {
        setCoin(getCoin()-a);
    }
    public void takeCoin(int a) {
        setCoin(getCoin()-a);
    }
}

package me.jetby.xClans.records;


import lombok.*;
import me.jetby.xClans.records.rank.Rank;

import java.util.UUID;

@AllArgsConstructor
@Getter @Setter
public class Member {
    private UUID uuid;
    private Rank rank;
    private long joinedAt;
    private long lastOnline;
    private  boolean clanGlow;

}

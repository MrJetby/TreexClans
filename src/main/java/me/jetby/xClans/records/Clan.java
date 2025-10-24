package me.jetby.xClans.records;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.jetby.xClans.records.rank.Rank;

import java.util.Set;

@RequiredArgsConstructor
@Getter @Setter
public class Clan {
    private final String id;
    private final String prefix;
    private final Member leader;
    private final Set<Member> members;
    private final Set<Rank> ranks;
    private final Chest chest;
    private final Level level;
    private final double balance;

    public void addMember(Member member) {
        this.members.add(member);
    }
    public void removeMember(Member member) {
        this.members.remove(member);
    }
}

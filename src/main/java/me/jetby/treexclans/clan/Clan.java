package me.jetby.treexclans.clan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treexclans.clan.rank.Rank;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Getter @Setter
public class Clan {
    private final String id;
    private final String prefix;
    private final Member leader;
    private final Set<Member> members;
    private final Map<String, Rank> ranks;
    private final Level level;
    private double balance;
    private Location base;
    private int exp;
    private boolean pvp;
    private Map<String, Integer> questsProgress;
    private List<String> completedQuest;
    private List<ItemStack> chest;

    public void addMember(Member member) {
        this.members.add(member);
    }
    public Member getMember(UUID uuid) {
        if (leader.getUuid().equals(uuid)) {
            return leader;
        }
        return members.stream()
                .filter(member -> member.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }
    public void removeMember(Member member) {
        this.members.remove(member);
    }
}

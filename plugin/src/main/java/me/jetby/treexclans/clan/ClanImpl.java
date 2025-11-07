package me.jetby.treexclans.clan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.level.Level;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
@Getter
@Setter
public class ClanImpl implements Clan {
    private final String id;
    private String prefix;
    private final Member leader;
    private final Set<Member> members;
    private final Map<String, Rank> ranks;
    private Level level;
    private double balance;
    private Location base;
    private int exp;
    private boolean pvp;
    private Map<UUID, Map<String, Integer>> questsProgress;
    private Map<UUID, List<String>> completedQuest;
    private List<ItemStack> chest;
    private String slogan;

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

    public @NotNull Set<Member> getMembersWithLeader() {
        Set<Member> list = new HashSet<>(members);
        list.add(leader);
        return list;
    }

    public void removeMember(Member memberImpl) {
        this.members.remove(memberImpl);
    }


    public void addExp(int amount, @NotNull Member memberImpl, Map<Integer, Level> levels) {
        int remaining = amount;

        while (remaining > 0) {
            int toNext = level.minExp() - getExp();

            if (remaining >= toNext) {
                setExp(0);
                remaining -= toNext;

                Level nextLevel = levels.get(Integer.parseInt(level.id()) + 1);
                if (nextLevel == null) break;

                for (Member m : getMembersWithLeader()) {
                    ActionContext ctx = new ActionContext(Bukkit.getPlayer(m.getUuid()));
                    ctx.put("clan", this);
                    ctx.put("member", m);
                    ActionExecutor.execute(ctx, ActionRegistry.transform(nextLevel.levelUpActions()));
                }

                setLevel(nextLevel);
            } else {
                setExp(getExp() + remaining);
                remaining = 0;
            }
        }

        memberImpl.setExp(memberImpl.getExp() + amount);
    }

    public void addExp(int amount, Map<Integer, Level> levels) {
        int remaining = amount;

        while (remaining > 0) {
            int toNext = level.minExp() - getExp();

            if (remaining >= toNext) {
                setExp(0);
                remaining -= toNext;

                Level nextLevel = levels.get(Integer.parseInt(level.id()) + 1);
                if (nextLevel == null) break;

                for (Member m : getMembersWithLeader()) {
                    ActionContext ctx = new ActionContext(Bukkit.getPlayer(m.getUuid()));
                    ctx.put("clan", this);
                    ctx.put("member", m);
                    ActionExecutor.execute(ctx, ActionRegistry.transform(nextLevel.levelUpActions()));
                }

                setLevel(nextLevel);
            } else {
                setExp(getExp() + remaining);
                remaining = 0;
            }
        }
    }


    public int getExpToNextLevel() {
        return level.minExp() - exp;
    }

    public void takeExp(int a, @NotNull Member memberImpl) {
        setExp(getExp() - a);
        memberImpl.setExp(memberImpl.getExp() - a);
    }

    public void takeExp(int a) {
        setExp(getExp() - a);
    }

}

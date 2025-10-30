package me.jetby.treexclans.clan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treexclans.clan.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@AllArgsConstructor
@Getter @Setter
public class Clan {
    private final String id;
    private final String prefix;
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
    public Set<Member> getMembersWithLeader() {
        Set<Member> list = new HashSet<>(members);
        list.add(leader);
        return list;
    }
    public void removeMember(Member member) {
        this.members.remove(member);
    }


    public void addExp(int a, @NotNull Member member, Map<Integer, Level> levels) {
        if (level.minExp()<=getExp()+a) {
            setExp(0);
            Level nextLevel = levels.get(Integer.parseInt(level.id())+1);
            for (Member m : getMembersWithLeader()) {
                ActionContext ctx = new ActionContext(Bukkit.getPlayer(m.getUuid()));
                ctx.put("clan", this);
                ctx.put("member", m);
                ActionExecutor.execute(ctx, ActionRegistry.transform(nextLevel.levelUpActions()));
            }
            setLevel(levels.get(Integer.parseInt(nextLevel.id())));

        } else {
            setExp(getExp()+a);
        }
        member.setExp(member.getExp()+a);
    }
    public void addExp(int a, Map<Integer, Level> levels) {
        if (level.minExp()<=getExp()+a) {
            setExp(0);
            try {
                Level nextLevel = levels.get(Integer.parseInt(level.id())+1);
                for (Member m : getMembersWithLeader()) {
                    ActionContext ctx = new ActionContext(Bukkit.getPlayer(m.getUuid()));
                    ctx.put("clan", this);
                    ctx.put("member", m);
                    ActionExecutor.execute(ctx, ActionRegistry.transform(nextLevel.levelUpActions()));
                }
                setLevel(levels.get(Integer.parseInt(nextLevel.id())));
            } catch (NumberFormatException ignored) {}
        } else {
            setExp(getExp()+a);
        }
    }

    public int getExpToNextLevel() {
        return level.minExp()-exp;
    }

    public void takeExp(int a, @NotNull Member member) {
        setExp(getExp()-a);
        member.setExp(member.getExp()-a);
    }
    public void takeExp(int a) {
        setExp(getExp()-a);
    }

}

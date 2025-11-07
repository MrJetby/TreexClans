package me.jetby.treexclans.api.service.clan;

import me.jetby.treexclans.api.service.clan.level.Level;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a clan in TreexClans.
 * <p>
 * Provides access to its leader, members, ranks, level, economy,
 * base location, quest progress, and inventory storage.
 */
public interface Clan {

    /** @return unique clan ID (usually its name). */
    @NotNull String getId();

    /** @return clan tag/prefix displayed before names. */
    @Nullable String getPrefix();

    void setPrefix(@Nullable String prefix);

    /** @return clan leader. */
    @NotNull Member getLeader();

    /** @return all members excluding the leader. */
    @NotNull Set<Member> getMembers();

    /**
     * @return all members including the leader.
     */
    Set<Member> getMembersWithLeader();

    /** Adds a new member to the clan. */
    void addMember(@NotNull Member member);

    /** Removes a member from the clan. */
    void removeMember(@NotNull Member member);

    /** @return map of clan ranks. */
    @NotNull Map<String, Rank> getRanks();

    /** @return current clan level. */
    @NotNull Level getLevel();

    void setLevel(@NotNull Level level);

    /** @return total clan balance. */
    double getBalance();

    void setBalance(double balance);

    /** @return current clan experience. */
    int getExp();

    void setExp(int exp);

    /** @return amount of exp left to next level. */
    int getExpToNextLevel();

    /** @return clan base location, or null if unset. */
    @Nullable Location getBase();

    void setBase(@Nullable Location base);

    /** @return whether PvP is enabled for this clan. */
    boolean isPvp();

    void setPvp(boolean pvp);

    /** @return current slogan or description of the clan. */
    @Nullable String getSlogan();

    void setSlogan(@Nullable String slogan);

    /** @return per-member quest progress. */
    @NotNull Map<UUID, Map<String, Integer>> getQuestsProgress();

    /** @return per-member completed quests. */
    @NotNull Map<UUID, List<String>> getCompletedQuest();

    /** @return the shared clan chest inventory. */
    @NotNull List<ItemStack> getChest();

    void setChest(@NotNull List<ItemStack> items);

    /** Adds experience to clan and handles level-ups. */
    void addExp(int amount, @NotNull Member member, @NotNull Map<Integer, Level> levels);

    void addExp(int amount, @NotNull Map<Integer, Level> levels);

    /** Removes experience points. */
    void takeExp(int amount, @NotNull Member member);

    void takeExp(int amount);

    /** Finds a member by their UUID. */
    @Nullable Member getMember(@NotNull UUID uuid);
}

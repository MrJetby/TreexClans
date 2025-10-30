package me.jetby.treexclans.functions.quests;

import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;


public record QuestManager(TreexClans plugin) {

    public boolean isQuestCompleted(@NotNull Member member, @NotNull Quest quest) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (clan == null) return false;

        List<String> completedQuests = clan.getCompletedQuest().get(member.getUuid());
        if (completedQuests == null) return false;

        return completedQuests.contains(quest.id());
    }

    public boolean isQuestPassable(@NotNull Member member, @NotNull Quest quest) {
        if (!plugin.getCfg().isGradualQuest()) return true;

        for (Set<Quest> categoryQuests : plugin.getQuestsLoader().getCategories().values()) {
            boolean found = false;
            for (Quest q : categoryQuests) {
                if (q.id().equals(quest.id())) {
                    found = true;
                    break;
                }
                if (!isQuestCompleted(member, q)) {
                    return false;
                }
            }
            if (found) return true;
        }
        return false;
    }

    public int getProgress(@NotNull Member member, @NotNull Quest quest) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (clan == null) return 0;

        Map<String, Integer> progress = clan.getQuestsProgress().get(member.getUuid());
        if (progress == null) return 0;

        return progress.getOrDefault(quest.id(), 0);
    }

    public void addProgressViaChecks(@NotNull Player player, @NotNull Member member,
                                     @NotNull QuestType type, @Nullable String property, int progress) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (clan == null) return;

        for (Quest quest : plugin.getQuestsLoader().getQuests().values()) {
            if (isQuestCompleted(member, quest)) continue;
            if (!isQuestPassable(member, quest)) continue;
            if (quest.disabledWorlds().contains(player.getWorld().getName())) continue;
            if (quest.questType() != type) continue;

            if (property != null && !property.equals(quest.questProperty())) continue;

            if (quest.progressType() == QuestProgressType.GLOBAL) {
                addGlobalQuestProgress(player, member, clan, quest, progress);
            } else {
                addIndividualQuestProgress(player, member, clan, quest, progress);
            }
            break;
        }
    }

    public void addProgressViaChecks(@NotNull Player player, @NotNull Member member,
                                     @NotNull Quest quest, @Nullable String property, int progress) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (clan == null) return;

        if (isQuestCompleted(member, quest)) return;
        if (quest.disabledWorlds().contains(player.getWorld().getName())) return;
        if (property != null && !property.equals(quest.questProperty())) return;

        if (quest.progressType() == QuestProgressType.GLOBAL) {
            addGlobalQuestProgress(player, member, clan, quest, progress);
        } else {
            addIndividualQuestProgress(player, member, clan, quest, progress);
        }
    }

    private void addIndividualQuestProgress(@NotNull Player player, @NotNull Member member,
                                            @NotNull Clan clan, @NotNull Quest quest, int progress) {
        int current = getProgress(member, quest);
        int newProgress = current + progress;

        Map<String, Integer> map = clan.getQuestsProgress()
                .computeIfAbsent(member.getUuid(), k -> new HashMap<>());
        map.put(quest.id(), newProgress);

        if (newProgress >= quest.target()) {
            finishIndividualQuest(player, member, clan, quest);
        }
    }

    private void addGlobalQuestProgress(@NotNull Player player, @NotNull Member member,
                                        @NotNull Clan clan, @NotNull Quest quest, int progress) {
        int current = getProgress(member, quest);
        int newProgress = current + progress;

        for (Member m : clan.getMembersWithLeader()) {
            Map<String, Integer> map = clan.getQuestsProgress()
                    .computeIfAbsent(m.getUuid(), k -> new HashMap<>());
            map.put(quest.id(), newProgress);
        }

        if (newProgress >= quest.target()) {
            finishGlobalQuest(player, member, clan, quest);
        }
    }

    private void finishIndividualQuest(@NotNull Player player, @NotNull Member member,
                                       @NotNull Clan clan, @NotNull Quest quest) {
        List<String> completed = clan.getCompletedQuest()
                .computeIfAbsent(member.getUuid(), k -> new ArrayList<>());

        if (completed.contains(quest.id())) {
            return;
        }

        completed.add(quest.id());

        ActionContext ctx = new ActionContext(player);
        ctx.put("member", member);
        ctx.put("clan", clan);
        List<String> commands = quest.rewards();
        commands = commands.stream()
                .map(s -> s.replace("%name%", quest.name()))
                .map(s -> s.replace("%id%", quest.id()))
                .map(s -> s.replace("%description%", quest.description()))
                .map(s -> s.replace("%target%", String.valueOf(quest.target())))
                .toList();
        ActionExecutor.execute(ctx, ActionRegistry.transform(commands));
    }

    private void finishGlobalQuest(@NotNull Player player, @NotNull Member member,
                                   @NotNull Clan clan, @NotNull Quest quest) {
        boolean alreadyCompleted = false;
        for (Member m : clan.getMembersWithLeader()) {
            List<String> completed = clan.getCompletedQuest()
                    .computeIfAbsent(m.getUuid(), k -> new ArrayList<>());
            if (completed.contains(quest.id())) {
                alreadyCompleted = true;
                break;
            }
        }

        if (alreadyCompleted) {
            return;
        }

        ActionContext globalCtx = new ActionContext(player);
        globalCtx.put("member", member);
        globalCtx.put("clan", clan);
        List<String> globalRewards = quest.globalRewards();
        globalRewards = globalRewards.stream()
                .map(s -> s.replace("%name%", quest.name()))
                .map(s -> s.replace("%id%", quest.id()))
                .map(s -> s.replace("%description%", quest.description()))
                .map(s -> s.replace("%target%", String.valueOf(quest.target())))
                .toList();
        ActionExecutor.execute(globalCtx, ActionRegistry.transform(globalRewards));

        for (Member m : clan.getMembersWithLeader()) {
            List<String> memberCompleted = clan.getCompletedQuest()
                    .computeIfAbsent(m.getUuid(), k -> new ArrayList<>());
            memberCompleted.add(quest.id());

            Player target = Bukkit.getPlayer(m.getUuid());
            if (target != null && target.isOnline()) {
                ActionContext ctx = new ActionContext(target);
                ctx.put("member", m);
                ctx.put("clan", clan);
                List<String> commands = quest.rewards();
                commands = commands.stream()
                        .map(s -> s.replace("%name%", quest.name()))
                        .map(s -> s.replace("%id%", quest.id()))
                        .map(s -> s.replace("%description%", quest.description()))
                        .map(s -> s.replace("%target%", String.valueOf(quest.target())))
                        .toList();
                ActionExecutor.execute(globalCtx, ActionRegistry.transform(commands));
            }
        }
    }

    public void setProgress(@NotNull Member member, @NotNull Quest quest, int progress) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (clan == null) return;

        Map<String, Integer> map = clan.getQuestsProgress()
                .computeIfAbsent(member.getUuid(), k -> new HashMap<>());
        map.put(quest.id(), progress);

        if (progress >= quest.target()) {
            List<String> completed = clan.getCompletedQuest()
                    .computeIfAbsent(member.getUuid(), k -> new ArrayList<>());
            if (!completed.contains(quest.id())) {
                completed.add(quest.id());
            }
        }
    }
}
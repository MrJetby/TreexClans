package me.jetby.treexclans.functions.quests;

import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.clan.MemberImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record QuestManager(TreexClans plugin) {

    public boolean isQuestCompleted(@NotNull Member memberImpl, @NotNull Quest quest) {
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(memberImpl);
        if (clanImpl == null) return false;

        List<String> completedQuests = clanImpl.getCompletedQuest().get(memberImpl.getUuid());
        if (completedQuests == null) return false;

        return completedQuests.contains(quest.id());
    }

    public boolean isQuestPassable(@NotNull Member memberImpl, @NotNull Quest quest) {
        if (!plugin.getCfg().isGradualQuest()) return true;

        for (Set<Quest> categoryQuests : plugin.getQuestsLoader().getCategories().values()) {
            boolean found = false;
            for (Quest q : categoryQuests) {
                if (q.id().equals(quest.id())) {
                    found = true;
                    break;
                }
                if (!isQuestCompleted(memberImpl, q)) {
                    return false;
                }
            }
            if (found) return true;
        }
        return false;
    }

    public int getProgress(@NotNull Member memberImpl, @NotNull Quest quest) {
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(memberImpl);
        if (clanImpl == null) return 0;

        Map<String, Integer> progress = clanImpl.getQuestsProgress().get(memberImpl.getUuid());
        if (progress == null) return 0;

        return progress.getOrDefault(quest.id(), 0);
    }

    public void addProgressViaChecks(@NotNull Player player, @NotNull Member memberImpl,
                                     @NotNull QuestType type, @Nullable String property, int progress) {
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(memberImpl);
        if (clanImpl == null) return;

        for (Quest quest : plugin.getQuestsLoader().getQuests().values()) {
            if (isQuestCompleted(memberImpl, quest)) continue;
            if (!isQuestPassable(memberImpl, quest)) continue;
            if (quest.disabledWorlds().contains(player.getWorld().getName())) continue;
            if (quest.questType() != type) continue;

            if (property != null && !property.equals(quest.questProperty())) continue;

            if (quest.progressType() == QuestProgressType.GLOBAL) {
                addGlobalQuestProgress(player, memberImpl, clanImpl, quest, progress);
            } else {
                addIndividualQuestProgress(player, memberImpl, clanImpl, quest, progress);
            }
            break;
        }
    }

    public void addProgressViaChecks(@NotNull Player player, @NotNull MemberImpl memberImpl,
                                     @NotNull Quest quest, @Nullable String property, int progress) {
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(memberImpl);
        if (clanImpl == null) return;

        if (isQuestCompleted(memberImpl, quest)) return;
        if (quest.disabledWorlds().contains(player.getWorld().getName())) return;
        if (property != null && !property.equals(quest.questProperty())) return;

        if (quest.progressType() == QuestProgressType.GLOBAL) {
            addGlobalQuestProgress(player, memberImpl, clanImpl, quest, progress);
        } else {
            addIndividualQuestProgress(player, memberImpl, clanImpl, quest, progress);
        }
    }

    private void addIndividualQuestProgress(@NotNull Player player, @NotNull Member memberImpl,
                                            @NotNull Clan clanImpl, @NotNull Quest quest, int progress) {
        int current = getProgress(memberImpl, quest);
        int newProgress = current + progress;

        Map<String, Integer> map = clanImpl.getQuestsProgress()
                .computeIfAbsent(memberImpl.getUuid(), k -> new HashMap<>());
        map.put(quest.id(), newProgress);

        if (newProgress >= quest.target()) {
            finishIndividualQuest(player, memberImpl, clanImpl, quest);
        }
    }

    private void addGlobalQuestProgress(@NotNull Player player, @NotNull Member memberImpl,
                                        @NotNull Clan clanImpl, @NotNull Quest quest, int progress) {
        int current = getProgress(memberImpl, quest);
        int newProgress = current + progress;

        for (var m : clanImpl.getMembersWithLeader()) {
            Map<String, Integer> map = clanImpl.getQuestsProgress()
                    .computeIfAbsent(m.getUuid(), k -> new HashMap<>());
            map.put(quest.id(), newProgress);
        }

        if (newProgress >= quest.target()) {
            finishGlobalQuest(player, memberImpl, clanImpl, quest);
        }
    }

    private void finishIndividualQuest(@NotNull Player player, @NotNull Member memberImpl,
                                       @NotNull Clan clanImpl, @NotNull Quest quest) {
        List<String> completed = clanImpl.getCompletedQuest()
                .computeIfAbsent(memberImpl.getUuid(), k -> new ArrayList<>());

        if (completed.contains(quest.id())) {
            return;
        }

        completed.add(quest.id());

        ActionContext ctx = new ActionContext(player);
        ctx.put("member", memberImpl);
        ctx.put("clan", clanImpl);
        List<String> commands = quest.rewards();
        commands = commands.stream()
                .map(s -> s.replace("%name%", quest.name()))
                .map(s -> s.replace("%id%", quest.id()))
                .map(s -> s.replace("%description%", quest.description()))
                .map(s -> s.replace("%target%", String.valueOf(quest.target())))
                .toList();
        ActionExecutor.execute(ctx, ActionRegistry.transform(commands));
    }

    private void finishGlobalQuest(@NotNull Player player, @NotNull Member memberImpl,
                                   @NotNull Clan clanImpl, @NotNull Quest quest) {
        boolean alreadyCompleted = false;
        for (var m : clanImpl.getMembersWithLeader()) {
            List<String> completed = clanImpl.getCompletedQuest()
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
        globalCtx.put("member", memberImpl);
        globalCtx.put("clan", clanImpl);
        List<String> globalRewards = quest.globalRewards();
        globalRewards = globalRewards.stream()
                .map(s -> s.replace("%name%", quest.name()))
                .map(s -> s.replace("%id%", quest.id()))
                .map(s -> s.replace("%description%", quest.description()))
                .map(s -> s.replace("%target%", String.valueOf(quest.target())))
                .toList();
        ActionExecutor.execute(globalCtx, ActionRegistry.transform(globalRewards));

        for (var m : clanImpl.getMembersWithLeader()) {
            List<String> memberCompleted = clanImpl.getCompletedQuest()
                    .computeIfAbsent(m.getUuid(), k -> new ArrayList<>());
            memberCompleted.add(quest.id());

            Player target = Bukkit.getPlayer(m.getUuid());
            if (target != null && target.isOnline()) {
                ActionContext ctx = new ActionContext(target);
                ctx.put("member", m);
                ctx.put("clan", clanImpl);
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

    public void setProgress(@NotNull MemberImpl memberImpl, @NotNull Quest quest, int progress) {
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(memberImpl);
        if (clanImpl == null) return;

        Map<String, Integer> map = clanImpl.getQuestsProgress()
                .computeIfAbsent(memberImpl.getUuid(), k -> new HashMap<>());
        map.put(quest.id(), progress);

        if (progress >= quest.target()) {
            List<String> completed = clanImpl.getCompletedQuest()
                    .computeIfAbsent(memberImpl.getUuid(), k -> new ArrayList<>());
            if (!completed.contains(quest.id())) {
                completed.add(quest.id());
            }
        }
    }
}
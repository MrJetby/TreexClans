package me.jetby.treexclans.functions.quests;

import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public record QuestManager(TreexClans plugin) {


    // TODO: так как сменил Clan -> Member
    //   то теперь куча багов. надо исправить всё

    public boolean isQuestCompleted(@NotNull Member member, @NotNull Quest quest) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        clan.getCompletedQuest().computeIfAbsent(member.getUuid(), k -> new ArrayList<>());
        return  clan.getCompletedQuest().get(member.getUuid()).contains(quest.id());
    }

    public void setProgress(@NotNull Member member, @NotNull Quest quest, int progress) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (!isQuestCompleted(member, quest)) {
            ActionContext ctx = new ActionContext(null);
            ctx.put("clan", clan);
            ctx.put("member", member);
            if (quest.progressType().equals(QuestProgressType.INDIVIDUAL)) {
                clan.getCompletedQuest().get(member.getUuid()).add(quest.id());
                ActionExecutor.execute(ctx, quest.rewards());
            } else if (quest.progressType().equals(QuestProgressType.GLOBAL)) {
                ActionExecutor.execute(ctx, quest.globalRewards());
                for (Member m : clan.getMembersWithLeader()) {
                    clan.getCompletedQuest().get(m.getUuid()).add(quest.id());
                    ActionExecutor.execute(ctx, quest.rewards());
                }
            }
        }
        Map<String, Integer> map = new HashMap<>();
        map.put(quest.id(), progress);
        clan.getQuestsProgress().put(member.getUuid(), map);
    }

    public boolean isQuestPassable(@NotNull Member member, @NotNull Quest quest) {
        if (!plugin.getCfg().isGradualQuest()) return true;
        for (Map.Entry<String, Set<Quest>> entry : plugin.getQuestsLoader().getCategories().entrySet()) {
            Set<Quest> categoryQuests = entry.getValue();
            boolean reachedCurrent = false;
            for (Quest q : categoryQuests) {
                if (q.equals(quest)) {
                    reachedCurrent = true;
                    break;
                }
                if (!isQuestCompleted(member, q)) {
                    return false;
                }
            }
            if (reachedCurrent) {
                return true;
            }
        }
        return false;
    }

    public void addProgressViaChecks(@NotNull Player player, @NotNull Member member, @NotNull QuestType type, @Nullable String property, int progress) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        for (Map.Entry<String, Quest> entry : plugin.getQuestsLoader().getQuests().entrySet()) {
            Quest quest = entry.getValue();
            if (quest.disabledWorlds().contains(player.getWorld().getName())) return;
            if (!isQuestPassable(member, quest)) continue;
            if (isQuestCompleted(member, quest)) continue;
            if (quest.questType() != type) continue;
            if (property != null) {
                if (!property.equals(quest.questProperty())) continue;
            }

            if (getProgress(member, quest) + progress >= quest.target()) {

                if (quest.progressType().equals(QuestProgressType.INDIVIDUAL)) {

                    Map<String, Integer> map = new HashMap<>();
                    map.put(quest.id(), quest.target());
                    clan.getQuestsProgress().put(member.getUuid(), map);

                    var data = clan.getCompletedQuest().get(member.getUuid());
                    if (data==null) {
                        clan.getCompletedQuest().forEach((uuid, strings) -> {
                            if (uuid.equals(member.getUuid())) {
                                strings = new ArrayList<>();
                                strings.add(quest.id());
                            }
                        });
                    }

                    ActionContext ctx = new ActionContext(player);
                    ctx.put("member", member);
                    ctx.put("clan", clan);
                    ActionExecutor.execute(ctx, quest.rewards());
                } else if (quest.progressType().equals(QuestProgressType.GLOBAL)) {

                    Map<String, Integer> map = new HashMap<>();
                    map.put(quest.id(), quest.target());
                    clan.getQuestsProgress().get(member.getUuid()).putAll(map);
                    ActionContext globalCtx = new ActionContext(player);
                    globalCtx.put("member", member);
                    globalCtx.put("clan", clan);
                    ActionExecutor.execute(globalCtx, quest.globalRewards());

                    for (Member m : clan.getMembersWithLeader()) {
                        Player target = Bukkit.getPlayer(m.getUuid());
                        // TODO: Сделать поддержку OfflinePlayer для выдачи кастом валюты оффлайн игрокам
                        if (target==null) continue;
                        ActionContext ctx = new ActionContext(target);
                        ctx.put("member", member);
                        ctx.put("clan", clan);
                        var data = clan.getCompletedQuest().get(m.getUuid());
                        if (data==null) {
                            clan.getCompletedQuest().forEach((uuid, strings) -> {
                                if (uuid.equals(m.getUuid())) {
                                    strings = new ArrayList<>();
                                    strings.add(quest.id());
                                }
                            });
                        }

                        ActionExecutor.execute(ctx, quest.rewards());
                    }
                }
                continue;
            }
            Map<String, Integer> map = new HashMap<>();
            map.put(quest.id(), getProgress(member, quest) + progress);
            clan.getQuestsProgress().put(member.getUuid(), map);
        }


    }

    public void addProgressViaChecks(@NotNull Player player, @NotNull Member member, Quest quest, @Nullable String property, int progress) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (property != null) {
            if (!property.equals(quest.questProperty())) return;
        }
        if (getProgress(member, quest) >= quest.target()) {
            Map<String, Integer> map = new HashMap<>();
            map.put(quest.id(), quest.target());
            clan.getQuestsProgress().put(member.getUuid(), map);
            if (quest.progressType().equals(QuestProgressType.INDIVIDUAL)) {
                clan.getCompletedQuest().get(member.getUuid()).add(quest.id());
                ActionContext ctx = new ActionContext(player);
                ctx.put("member", member);
                ctx.put("clan", clan);
                ActionExecutor.execute(ctx, quest.rewards());
            } else if (quest.progressType().equals(QuestProgressType.GLOBAL)) {

                ActionContext globalCtx = new ActionContext(player);
                globalCtx.put("member", member);
                globalCtx.put("clan", clan);
                ActionExecutor.execute(globalCtx, quest.globalRewards());
                for (Member m : clan.getMembersWithLeader()) {
                    Player target = Bukkit.getPlayer(m.getUuid());
                    // TODO: Сделать поддержку OfflinePlayer для выдачи кастом валюты оффлайн игрокам
                    if (target==null) continue;
                    ActionContext ctx = new ActionContext(target);
                    ctx.put("member", member);
                    ctx.put("clan", clan);
                    clan.getCompletedQuest().get(m.getUuid()).add(quest.id());
                    ActionExecutor.execute(ctx, quest.rewards());
                }
            }
            return;
        }
        Map<String, Integer> map = new HashMap<>();
        map.put(quest.id(), getProgress(member, quest) + progress);
        clan.getQuestsProgress().put(member.getUuid(), map);
    }

    public int getProgress(@NotNull Member member, @NotNull Quest quest) {
        Clan clan = plugin.getClanManager().getClanByMember(member);
        if (clan.getQuestsProgress().get(member.getUuid())==null) return 0;
        return clan.getQuestsProgress().get(member.getUuid()).getOrDefault(quest.id(), 0);
    }

}

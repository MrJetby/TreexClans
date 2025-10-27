package me.jetby.treexclans.functions.quests;

import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;


public record QuestManager(TreexClans plugin) {


    public boolean isQuestCompleted(Clan clan, Quest quest) {
        if (clan == null) return false;
        return clan.getCompletedQuest().contains(quest.id());
    }

    public void setProgress(Clan clan, Quest quest, int progress) {
        if (clan == null) return;
        if (!isQuestCompleted(clan, quest)) {
            ActionContext ctx = new ActionContext(null);
            ctx.put("clan", clan);
            ActionExecutor.execute(ctx, quest.rewards());
            clan.getCompletedQuest().add(quest.id());
        }
        clan.getQuestsProgress().put(quest.id(), progress);
    }

    public Quest getPastQuest(Clan clan, Quest quest) {
        // TODO: get past quest before this
        for (Map.Entry<String, Set<Quest>> entry : plugin.getQuestsLoader().getCategories().entrySet()) {
            if (entry.getValue().stream().filter(q -> !isQuestCompleted(clan, q)).findFirst().equals(quest)) {

            }
        }
        return quest;
    }

    public boolean isQuestPassable(Clan clan, Quest quest) {
        if (!plugin.getCfg().isGradualQuest()) return true;
        for (Map.Entry<String, Set<Quest>> entry : plugin.getQuestsLoader().getCategories().entrySet()) {
            Set<Quest> categoryQuests = entry.getValue();
            boolean reachedCurrent = false;
            for (Quest q : categoryQuests) {
                if (q.equals(quest)) {
                    reachedCurrent = true;
                    break;
                }
                if (!isQuestCompleted(clan, q)) {
                    return false;
                }
            }
            if (reachedCurrent) {
                return true;
            }
        }
        return false;
    }

    public void addProgress(Player player, Clan clan, QuestType type, @Nullable String property, int progress) {
        if (clan == null) return;
        for (Map.Entry<String, Quest> entry : plugin.getQuestsLoader().getQuests().entrySet()) {
            Quest quest = entry.getValue();
            if (!isQuestPassable(clan, quest)) continue;
            if (isQuestCompleted(clan, quest)) continue;
            if (quest.questType() != type) continue;
            if (property != null) {
                if (!property.equals(quest.questProperty())) continue;
            }

            if (getProgress(clan, quest)+progress >= quest.target()) {
                ActionContext ctx = new ActionContext(player);
                ctx.put("clan", clan);
                ActionExecutor.execute(ctx, quest.rewards());
                clan.getQuestsProgress().put(quest.id(), quest.target());
                clan.getCompletedQuest().add(quest.id());
                continue;
            }
            clan.getQuestsProgress().put(quest.id(), getProgress(clan, quest) + progress);
        }


    }

    public void addProgress(Player player, Clan clan, Quest quest, @Nullable String property, int progress) {
        if (clan == null) return;
        if (property != null) {
            if (!property.equals(quest.questProperty())) return;
        }
        if (getProgress(clan, quest) >= quest.target()) {
            ActionContext ctx = new ActionContext(player);
            ctx.put("clan", clan);
            ActionExecutor.execute(ctx, quest.rewards());
            clan.getQuestsProgress().put(quest.id(), quest.target());
            clan.getCompletedQuest().add(quest.id());
            return;
        }
        clan.getQuestsProgress().put(quest.id(), getProgress(clan, quest) + progress);
    }

    public int getProgress(Clan clan, Quest quest) {
        return clan.getQuestsProgress().getOrDefault(quest.id(), 0);
    }

}

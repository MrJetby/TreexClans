package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.ActionEntry;
import me.jetby.treex.actions.ActionTypeRegistry;

import java.util.HashSet;
import java.util.Set;

public class Actions {

    public void registerCustomActions() {
        Set<ActionEntry> actions = new HashSet<>();
        actions.add(new ActionEntry("TREEXCLANS", "CLAN_MESSAGE", new ClanMessageAction()));
        actions.add(new ActionEntry("TREEXCLANS", "MESSAGE_CLAN", new ClanMessageAction()));
        actions.add(new ActionEntry("TREEXCLANS", "TEAM_MESSAGE", new ClanMessageAction()));
        actions.add(new ActionEntry("TREEXCLANS", "TEAM_MSG", new ClanMessageAction()));

        actions.add(new ActionEntry("TREEXCLANS", "OPEN_MENU", new OpenMenuAction()));
        actions.add(new ActionEntry("TREEXCLANS", "OPEN_GUI", new OpenMenuAction()));
        actions.add(new ActionEntry("TREEXCLANS", "MENU", new OpenMenuAction()));

        actions.add(new ActionEntry("TREEXCLANS", "CLAN_EXP_GIVE", new ClanExpGiveAction()));
        actions.add(new ActionEntry("TREEXCLANS", "CLAN_EXP_TAKE", new ClanExpGiveAction()));


        ActionTypeRegistry.register(actions);
    }
}

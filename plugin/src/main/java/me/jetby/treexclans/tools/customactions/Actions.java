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
        actions.add(new ActionEntry("TREEXCLANS", "CLAN_EXP_TAKE", new ClanExpTakeAction()));

        actions.add(new ActionEntry("TREEXCLANS", "MONEY_GIVE", new MoneyGiveAction()));
        actions.add(new ActionEntry("TREEXCLANS", "MONEY_TAKE", new MoneyTakeAction()));

        actions.add(new ActionEntry("TREEXCLANS", "COIN_GIVE", new CoinAddAction()));
        actions.add(new ActionEntry("TREEXCLANS", "COIN_TAKE", new CoinTakeAction()));

        actions.add(new ActionEntry("TREEXCLANS", "BUTTON", new ButtonAction()));

        ActionTypeRegistry.register(actions);
    }
}

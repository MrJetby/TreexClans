package me.jetby.treexclans.tools.customactions;

import me.jetby.treex.actions.ActionTypeRegistry;

public class Actions {

    public void registerCustomActions() {
        ActionTypeRegistry.register("TREEXCLANS", "CLAN_MESSAGE", new ClanMessageAction());
        ActionTypeRegistry.register("TREEXCLANS", "MESSAGE_CLAN", new ClanMessageAction());
        ActionTypeRegistry.register("TREEXCLANS", "TEAM_MESSAGE", new ClanMessageAction());
        ActionTypeRegistry.register("TREEXCLANS", "TEAM_MSG", new ClanMessageAction());

        ActionTypeRegistry.register("TREEXCLANS", "OPEN_MENU", new OpenMenuAction());
        ActionTypeRegistry.register("TREEXCLANS", "OPEN_GUI", new OpenMenuAction());
        ActionTypeRegistry.register("TREEXCLANS", "MENU", new OpenMenuAction());
    }
}

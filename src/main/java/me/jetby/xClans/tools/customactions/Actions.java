package me.jetby.xClans.tools.customactions;

import me.jetby.treex.actions.ActionTypeRegistry;

public class Actions {

    public void registerCustomActions() {
        ActionTypeRegistry.register("TREEXCLANS", "CLAN_MESSAGE", new ClanMessage());
        ActionTypeRegistry.register("TREEXCLANS", "MESSAGE_CLAN", new ClanMessage());
        ActionTypeRegistry.register("TREEXCLANS", "TEAM_MESSAGE", new ClanMessage());
        ActionTypeRegistry.register("TREEXCLANS", "TEAM_MSG", new ClanMessage());
    }
}

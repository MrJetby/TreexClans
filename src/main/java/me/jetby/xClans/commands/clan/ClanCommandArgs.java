package me.jetby.xClans.commands.clan;

import lombok.Getter;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.commands.clan.args.*;

public enum ClanCommandArgs {
    CREATE(new Create()),
    INVITE(new Invite()),
    ACCEPT(new Accept()),
    GLOW(new Glow()),
    INFO(new Info());

    @Getter
    private Subcommand subcommand;

    ClanCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

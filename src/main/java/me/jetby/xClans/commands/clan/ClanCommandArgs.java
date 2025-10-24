package me.jetby.xClans.commands.clan;

import lombok.Getter;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.commands.clan.args.Create;

public enum ClanCommandArgs {
    CREATE(new Create());

    @Getter
    private Subcommand subcommand;

    ClanCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

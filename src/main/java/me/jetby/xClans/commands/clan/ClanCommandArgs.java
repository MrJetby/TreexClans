package me.jetby.xClans.commands.clan;

import lombok.Getter;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.commands.clan.args.Create;
import me.jetby.xClans.commands.clan.args.Info;

public enum ClanCommandArgs {
    CREATE(new Create()),
    INFO(new Info());

    @Getter
    private Subcommand subcommand;

    ClanCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

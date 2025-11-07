package me.jetby.treexclans.commands.admin;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.commands.admin.subcommands.CoinSubcommand;
import me.jetby.treexclans.commands.admin.subcommands.ExpSubcommand;
import me.jetby.treexclans.commands.admin.subcommands.ReloadSubcommand;
import me.jetby.treexclans.commands.admin.subcommands.StorageSubcommand;

public enum AdminCommandArgs {
    COIN(new CoinSubcommand()),
    EXP(new ExpSubcommand()),
    STORAGE(new StorageSubcommand()),
    RELOAD(new ReloadSubcommand(TreexClans.getInstance()));

    @Getter
    private final Subcommand subcommand;

    AdminCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

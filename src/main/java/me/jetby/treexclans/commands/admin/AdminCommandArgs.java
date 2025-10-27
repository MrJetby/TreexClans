package me.jetby.treexclans.commands.admin;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.commands.Subcommand;
import me.jetby.treexclans.commands.admin.subcommands.TestSubcommand;

public enum AdminCommandArgs {
    TEST(new TestSubcommand(TreexClans.getInstance()));

    @Getter
    private Subcommand subcommand;

    AdminCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

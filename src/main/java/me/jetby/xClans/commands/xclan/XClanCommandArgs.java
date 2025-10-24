package me.jetby.xClans.commands.xclan;

import lombok.Getter;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.commands.xclan.args.Test;

public enum XClanCommandArgs {
    TEST(new Test(TreexClans.getInstance()));

    @Getter
    private Subcommand subcommand;

    XClanCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

package me.jetby.xClans.commands.clan;

import lombok.Getter;
import me.jetby.xClans.commands.Subcommand;
import me.jetby.xClans.commands.clan.args.*;

public enum ClanCommandArgs {
    CREATE(new Create()),
    INVITE(new Invite()),
    ACCEPT(new Accept()),
    GLOW(new Glow()),
    KICK(new Kick()),
    DISBAND(new Disband()),
    DEPOSIT(new Deposit()),
    BALANCE(new Balance()),
    INVEST(new Deposit()),
    WITHDRAW(new Withdraw()),
    SETBASE(new SetBase()),
    SETRANK(new SetRank()),
    BASE(new Base()),
    LEAVE(new Leave()),
    CHAT(new Chat()),
    INFO(new Info());

    @Getter
    private Subcommand subcommand;

    ClanCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

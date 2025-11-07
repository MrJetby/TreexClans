package me.jetby.treexclans.commands.clan;

import lombok.Getter;
import me.jetby.treexclans.api.command.Subcommand;
import me.jetby.treexclans.commands.clan.subcommands.*;

public enum ClanCommandArgs {
    CREATE(new CreateSubcommand()),
    INVITE(new InviteSubcommand()),
    ACCEPT(new AcceptSubcommand()),
    GLOW(new GlowSubcommand()),
    KICK(new KickSubcommand()),
    DISBAND(new DisbandSubcommand()),
    DEPOSIT(new DepositSubcommand()),
    BALANCE(new BalanceSubcommand()),
    INVEST(new DepositSubcommand()),
    WITHDRAW(new WithdrawSubcommand()),
    SETBASE(new SetBaseSubcommand()),
    SETRANK(new SetRankSubcommand()),
    BASE(new BaseSubcommand()),
    LEAVE(new LeaveSubcommand()),
    CHAT(new ChatSubcommand()),
    SETSLOGAN(new SetSloganSubcommand()),
    SETPREFIX(new SetPrefixSubcommand()),
    PVP(new PvpSubcommand());

    @Getter
    private final Subcommand subcommand;

    ClanCommandArgs(Subcommand subcommand) {
        this.subcommand = subcommand;
    }
}

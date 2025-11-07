package me.jetby.treexclans.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.gui.GuiApi;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import me.jetby.treexclans.clan.MemberImpl;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import me.jetby.treexclans.gui.core.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class GuiFactory {

    public Gui create(TreexClans plugin,
                      @NotNull Menu menu,
                      @NotNull Player player,
                      @NotNull Clan clanImpl,
                      Object... customObjects) {

        String guiType = menu.type();

        Gui builtInGui = createBuiltInGui(plugin, menu, player, clanImpl, guiType, customObjects);
        if (builtInGui != null) {
            return builtInGui;
        }

        Gui customGui = GuiApi.createGui(guiType, plugin, menu, player, clanImpl, customObjects);
        if (customGui != null) {
            return customGui;
        }

        TreexClans.LOGGER.warn("GUI type '" + guiType + "' not found! Returning DefaultGui instead.");
        return new DefaultGui(plugin, menu, player, clanImpl);
    }

    private Gui createBuiltInGui(TreexClans plugin,
                                 @NotNull Menu menu,
                                 @NotNull Player player,
                                 @NotNull Clan clanImpl,
                                 String guiType,
                                 Object... customObjects) {
        try {
            GuiType type = GuiType.valueOf(guiType);

            return switch (type) {
                case MEMBERS -> new MembersGui(plugin, menu, player, clanImpl);
                case CHOOSE_COLOR -> {
                    if (customObjects != null) {
                        for (Object obj : customObjects) {
                            if (obj instanceof MemberImpl target) {
                                yield new ChooseColorGui(plugin, menu, player, clanImpl, target);
                            }
                        }
                    }
                    yield new ChooseColorGui(plugin, menu, player, clanImpl, null);
                }

                case CHEST -> new ChestGui(plugin, menu, player, clanImpl);

                case QUESTS -> new QuestsGui(plugin, menu, player, clanImpl);

                case RANKS -> new RanksGui(plugin, menu, player, clanImpl);

                case RANK_PERMISSIONS -> {
                    if (customObjects != null) {
                        for (Object obj : customObjects) {
                            if (obj instanceof Rank rank) {
                                yield new RankPermissionsGui(plugin, menu, player, clanImpl, rank);
                            }
                        }
                    }
                    yield null;
                }

                case CHOOSE_PLAYER_COLOR -> new ChoosePlayerColorGui(plugin, menu, player, clanImpl);

                case MENU, DEFAULT -> new DefaultGui(plugin, menu, player, clanImpl);

                case TOP_CLANS -> {
                    if (customObjects != null) {
                        LeaderboardService.TopType topType = null;
                        int num = 1;
                        for (Object obj : customObjects) {
                            if (obj instanceof LeaderboardService.TopType t) topType = t;
                            if (obj instanceof Integer i) num = i;
                        }
                        yield new TopClansGui(plugin, menu, player, clanImpl, topType, num);
                    }
                    yield new TopClansGui(plugin, menu, player, clanImpl, null, 1);
                }
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
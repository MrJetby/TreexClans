package me.jetby.treexclans.gui;

import lombok.Getter;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.gui.*;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import me.jetby.treexclans.clan.MemberImpl;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import me.jetby.treexclans.gui.core.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class GuiFactoryImpl implements GuiFactory {

    private final GuiService guiService;

    public GuiFactoryImpl() {
        this.guiService = new GuiApi();
    }

    /**
     * Creates a GUI instance based on the provided menu type.
     * <p>
     * The resolution order is:
     * <ol>
     *     <li>Built-in core GUIs (handled internally)</li>
     *     <li>Custom GUIs registered in {@link GuiService}</li>
     *     <li>Fallback to {@link DefaultGui}</li>
     * </ol>
     *
     * @param plugin        the plugin instance
     * @param menu          the menu configuration
     * @param player        the player viewing the GUI
     * @param clan          the player's clan
     * @param customObjects additional context parameters (optional)
     * @return a valid {@link Gui} instance, never {@code null}
     */
    public Gui create(JavaPlugin plugin,
                      @NotNull Menu menu,
                      @NotNull Player player,
                      @NotNull Clan clan,
                      Object... customObjects) {

        String guiType = menu.type();

        Gui builtInGui = createBuiltInGui(plugin, menu, player, clan, guiType, customObjects);
        if (builtInGui != null) {
            return builtInGui;
        }

        Gui customGui = guiService.createGui(guiType, plugin, menu, player, clan, customObjects);
        if (customGui != null) {
            return customGui;
        }

        TreexClans.LOGGER.warn("GUI type '" + guiType + "' not found! Returning DefaultGui instead.");
        return new DefaultGui(plugin, menu, player, clan);
    }

    /**
     * Creates one of the predefined built-in GUIs.
     *
     * @param plugin  the plugin instance
     * @param menu    the menu configuration
     * @param player  the player
     * @param clan    the player's clan
     * @param guiType the GUI type (usually from {@link GuiType})
     * @param args    additional parameters (optional)
     * @return a built-in {@link Gui}, or {@code null} if not a built-in type
     */
    private Gui createBuiltInGui(JavaPlugin plugin,
                                 @NotNull Menu menu,
                                 @NotNull Player player,
                                 @NotNull Clan clan,
                                 String guiType,
                                 Object... args) {
        try {
            GuiType type = GuiType.valueOf(guiType);

            return switch (type) {
                case MEMBERS -> new MembersGui(plugin, menu, player, clan);
                case CHOOSE_COLOR -> {
                    if (args != null) {
                        for (Object obj : args) {
                            if (obj instanceof MemberImpl target) {
                                yield new ChooseColorGui(plugin, menu, player, clan, target);
                            }
                        }
                    }
                    yield new ChooseColorGui(plugin, menu, player, clan, null);
                }

                case CHEST -> new ChestGui(plugin, menu, player, clan);

                case QUESTS -> new QuestsGui(plugin, menu, player, clan);

                case RANKS -> new RanksGui(plugin, menu, player, clan);

                case RANK_PERMISSIONS -> {
                    if (args != null) {
                        for (Object obj : args) {
                            if (obj instanceof Rank rank) {
                                yield new RankPermissionsGui(plugin, menu, player, clan, rank);
                            }
                        }
                    }
                    yield null;
                }

                case CHOOSE_PLAYER_COLOR -> new ChoosePlayerColorGui(plugin, menu, player, clan);

                case MENU, DEFAULT -> new DefaultGui(plugin, menu, player, clan);

                case TOP_CLANS -> {
                    if (args != null) {
                        LeaderboardService.TopType topType = null;
                        int num = 1;
                        for (Object obj : args) {
                            if (obj instanceof LeaderboardService.TopType t) topType = t;
                            if (obj instanceof Integer i) num = i;
                        }
                        yield new TopClansGui(plugin, menu, player, clan, topType, num);
                    }
                    yield new TopClansGui(plugin, menu, player, clan, null, 1);
                }
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
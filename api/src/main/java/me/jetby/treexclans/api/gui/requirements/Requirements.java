package me.jetby.treexclans.api.gui.requirements;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.treex.text.Papi;
import org.bukkit.entity.Player;

@UtilityClass
public class Requirements {

    public boolean check(Player player, Requirement req) {
        return checkInternal(player, req.type(), req.permission(), req.input(), req.output());
    }

    private boolean checkInternal(Player player,
                                  String type,
                                  String permission,
                                  String input,
                                  String output) {
        String parsedInput = input;
        String parsedOutput = output;

        if (type.equalsIgnoreCase("string equals") || type.equalsIgnoreCase("!string equals")) {
            parsedInput = setPlaceholders(player, input);
            parsedOutput = setPlaceholders(player, output);
        }

        return switch (type.toLowerCase()) {
            case "has permission" -> player.hasPermission(permission);
            case "!has permission" -> !player.hasPermission(permission);
            case "string equals" -> parsedInput.equalsIgnoreCase(parsedOutput);
            case "!string equals" -> !parsedInput.equalsIgnoreCase(parsedOutput);
            case "javascript", "math" -> evalJavascriptLike(player, input);
            default -> false;
        };
    }

    private String setPlaceholders(Player player, String input) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }

    private boolean evalJavascriptLike(Player player, String input) {
        String[] args = input.split(" ");
        if (args.length < 3) return false;

        args[0] = Papi.setPapi(player, args[0]);
        args[2] = Papi.setPapi(player, args[2]);

        try {
            double x = Double.parseDouble(args[0]);
            double x1 = Double.parseDouble(args[2]);
            return switch (args[1]) {
                case ">" -> x > x1;
                case ">=" -> x >= x1;
                case "==" -> x == x1;
                case "!=" -> x != x1;
                case "<=" -> x <= x1;
                case "<" -> x < x1;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return switch (args[1]) {
                case "==" -> args[0].equals(args[2]);
                case "!=" -> !args[0].equals(args[2]);
                default -> false;
            };
        }
    }
}

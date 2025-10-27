package me.jetby.treexclans.gui.requirements;

import lombok.experimental.UtilityClass;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.gui.Button;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


@UtilityClass
public class Requirements {

    public boolean check(Player player, SimpleRequirement req) {
        return checkInternal(player, req.type(), req.permission(), req.input(), req.output());
    }
    public boolean check(Player player, ViewRequirement req) {
        return checkInternal(player, req.type(), req.permission(), req.input(), req.output());
    }

    public boolean check(Player player, ClickRequirement req) {
        return checkInternal(player, req.type(), req.permission(), req.input(), req.output());
    }

    private boolean checkInternal(Player player,
                                  String type,
                                  String permission,
                                  String input,
                                  String output) {
        return switch (type.toLowerCase()) {
            case "has permission" -> player.hasPermission(permission);
            case "!has permission" -> !player.hasPermission(permission);
            case "string equals" -> input.equalsIgnoreCase(output);
            case "!string equals" -> !input.equalsIgnoreCase(output);
            case "javascript", "math" -> evalJavascriptLike(player, input);
            default -> false;
        };
    }

    private boolean evalJavascriptLike(Player player, String input) {
        String[] args = input.split(" ");
        if (args.length < 3) return false;

        args[0] = setPlaceholders(player, args[0]);
        args[2] = setPlaceholders(player, args[2]);

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


    public void runDenyCommands(Player player, List<String> denyCommands) {
        ActionContext ctx = new ActionContext(player);
        List<String> commands = new ArrayList<>();
        for (String str : denyCommands) {
            commands.add(setPlaceholders(player, str));
        }
        ActionExecutor.execute(ctx, ActionRegistry.transform(commands));
    }
    public void runDenyCommands(Player player, List<String> denyCommands, Button button) {
        ActionContext ctx = new ActionContext(player);
        ctx.put("button", button);
        List<String> commands = new ArrayList<>();
        for (String str : denyCommands) {
            commands.add(setPlaceholders(player, str));
        }
        ActionExecutor.execute(ctx, ActionRegistry.transform(commands));
    }

    private String setPlaceholders(Player player, String string) {
        return Papi.setPapi(player, string
//                .replace("%sell_pay%", df.format(totalPrice))
//                .replace("%sell_score%", df.format(totalScore))
        );
    }
}

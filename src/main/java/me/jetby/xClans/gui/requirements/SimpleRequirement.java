package me.jetby.xClans.gui.requirements;

import java.util.List;

public record SimpleRequirement(

        String type,
        String input,
        String output,
        String permission,
        List<String> actions,
        List<String> denyActions
) {
}

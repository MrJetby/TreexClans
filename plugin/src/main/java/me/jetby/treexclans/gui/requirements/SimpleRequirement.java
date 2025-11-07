package me.jetby.treexclans.gui.requirements;

import java.util.List;

public record SimpleRequirement(
        String type,
        String input,
        String output,
        String permission,
        List<String> actions,
        List<String> denyActions
) implements Requirement { }

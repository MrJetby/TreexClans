package me.jetby.treexclans.api.gui.requirements;

import java.util.List;

public record SimpleRequirement(
        String type,
        String input,
        String output,
        String permission,
        List<String> actions,
        List<String> denyActions
) implements Requirement { }

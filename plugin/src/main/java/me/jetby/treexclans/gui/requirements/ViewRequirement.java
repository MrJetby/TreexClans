package me.jetby.treexclans.gui.requirements;

public record ViewRequirement(
        String type,
        String input,
        String output,
        boolean freeSlot,
        String permission
) implements Requirement { }

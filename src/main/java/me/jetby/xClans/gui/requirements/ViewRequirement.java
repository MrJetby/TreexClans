package me.jetby.xClans.gui.requirements;

public record ViewRequirement(

        String type,
        String input,
        String output,
        boolean freeSlot,
        String permission
) {
}

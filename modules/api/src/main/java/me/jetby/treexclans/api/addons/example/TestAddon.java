package me.jetby.treexclans.api.addons.example;

import me.jetby.treexclans.api.addons.JavaAddon;
import me.jetby.treexclans.api.addons.annotations.Dependency;
import me.jetby.treexclans.api.addons.annotations.ClanAddon;

@ClanAddon(
        id = "test-addon",
        version = "1.0.0",
        authors = {"JetBy"},
        depends = {
                @Dependency(id = "core-api"),
                @Dependency(id = "database")
        },
        softDepends = {
                @Dependency(id = "economy")
        },
        loadBefore = {"promo-system"},
        loadAfter = {"core-api"}
)
public final class TestAddon extends JavaAddon {

    @Override
    public void onEnable() {
        this.getLogger().info("Test enabled!");
        this.getServiceManager().getCommandService().registerCommand("test", new TestCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

package info.n00bs.adblock_2b2t.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import info.n00bs.adblock_2b2t.client.config.FilterCategory;
import info.n00bs.adblock_2b2t.client.config.FilterConfig;
import info.n00bs.adblock_2b2t.client.filter.MessageFilter;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.CompletableFuture;

/**
 * Handles command registration and execution for the AdBlock mod.
 */
public class FilterCommands {

    /**
     * Registers all commands for the AdBlock mod.
     */
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerAdBlockCommand(dispatcher);
        });
    }

    /**
     * Registers the main adblock command and its subcommands.
     * 
     * @param dispatcher The command dispatcher
     */
    private static void registerAdBlockCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("adblock")
                .then(ClientCommandManager.literal("enable")
                    .executes(context -> executeEnable(context, true))
                )
                .then(ClientCommandManager.literal("disable")
                    .executes(context -> executeEnable(context, false))
                )
                .then(ClientCommandManager.literal("status")
                    .executes(FilterCommands::executeStatus)
                )
                .then(ClientCommandManager.literal("refresh")
                    .executes(FilterCommands::executeRefresh)
                )
                .then(ClientCommandManager.literal("remote")
                    .then(ClientCommandManager.literal("enable")
                        .executes(context -> executeRemoteEnable(context, true))
                    )
                    .then(ClientCommandManager.literal("disable")
                        .executes(context -> executeRemoteEnable(context, false))
                    )
                    .then(ClientCommandManager.literal("url")
                        .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                            .executes(FilterCommands::executeSetRemoteUrl)
                        )
                    )
                )
                .then(ClientCommandManager.literal("custom")
                    .then(ClientCommandManager.literal("enable")
                        .executes(context -> executeCustomEnable(context, true))
                    )
                    .then(ClientCommandManager.literal("disable")
                        .executes(context -> executeCustomEnable(context, false))
                    )
                )
                .then(ClientCommandManager.literal("debug")
                    .then(ClientCommandManager.literal("enable")
                        .executes(context -> executeDebugEnable(context, true))
                    )
                    .then(ClientCommandManager.literal("disable")
                        .executes(context -> executeDebugEnable(context, false))
                    )
                )
                .then(ClientCommandManager.literal("autorefresh")
                    .then(ClientCommandManager.literal("enable")
                        .executes(context -> executeAutoRefreshEnable(context, true))
                    )
                    .then(ClientCommandManager.literal("disable")
                        .executes(context -> executeAutoRefreshEnable(context, false))
                    )
                    .then(ClientCommandManager.literal("delay")
                        .then(ClientCommandManager.argument("minutes", IntegerArgumentType.integer(1))
                            .executes(FilterCommands::executeSetAutoRefreshDelay)
                        )
                    )
                )
                .then(ClientCommandManager.literal("help")
                    .executes(FilterCommands::executeHelp)
                )
        );
    }

    /**
     * Executes the enable/disable command.
     * 
     * @param context The command context
     * @param enable Whether to enable or disable the filter
     * @return 1 for success
     */
    private static int executeEnable(CommandContext<FabricClientCommandSource> context, boolean enable) {
        FilterConfig.getInstance().setEnabled(enable);

        if (enable) {
            context.getSource().sendFeedback(Text.literal("AdBlock has been enabled").formatted(Formatting.GREEN));
        } else {
            context.getSource().sendFeedback(Text.literal("AdBlock has been disabled").formatted(Formatting.RED));
        }

        return 1;
    }

    /**
     * Executes the status command.
     * 
     * @param context The command context
     * @return 1 for success
     */
    private static int executeStatus(CommandContext<FabricClientCommandSource> context) {
        FilterConfig config = FilterConfig.getInstance();

        context.getSource().sendFeedback(Text.literal("=== AdBlock Status ===").formatted(Formatting.GOLD));
        context.getSource().sendFeedback(Text.literal("Enabled: " + config.isEnabled())
                .formatted(config.isEnabled() ? Formatting.GREEN : Formatting.RED));

        context.getSource().sendFeedback(Text.literal("Remote filters: " + config.isUseRemoteFilters())
                .formatted(config.isUseRemoteFilters() ? Formatting.GREEN : Formatting.RED));
        context.getSource().sendFeedback(Text.literal("Remote URL: " + config.getRemoteUrl())
                .formatted(Formatting.AQUA));
        context.getSource().sendFeedback(Text.literal("Auto-refresh: " + config.isAutoRefreshEnabled())
                .formatted(config.isAutoRefreshEnabled() ? Formatting.GREEN : Formatting.RED));
        context.getSource().sendFeedback(Text.literal("Auto-refresh delay: " + config.getAutoRefreshDelay() + " minutes")
                .formatted(Formatting.AQUA));

        context.getSource().sendFeedback(Text.literal("Custom filters: " + config.isUseCustomFilters())
                .formatted(config.isUseCustomFilters() ? Formatting.GREEN : Formatting.RED));
        context.getSource().sendFeedback(Text.literal("Debug mode: " + config.isDebugMode())
                .formatted(config.isDebugMode() ? Formatting.GREEN : Formatting.RED));
        return 1;
    }

    /**
     * Executes the refresh command.
     * 
     * @param context The command context
     * @return 1 for success
     */
    private static int executeRefresh(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("Refreshing filters...").formatted(Formatting.YELLOW));

        CompletableFuture<Void> future = MessageFilter.getInstance().refreshFilters();
        future.thenRun(() -> {
            context.getSource().sendFeedback(Text.literal("Filters refreshed successfully").formatted(Formatting.GREEN));
        });

        return 1;
    }

    /**
     * Executes the remote enable/disable command.
     * 
     * @param context The command context
     * @param enable Whether to enable or disable remote filters
     * @return 1 for success
     */
    private static int executeRemoteEnable(CommandContext<FabricClientCommandSource> context, boolean enable) {
        FilterConfig.getInstance().setUseRemoteFilters(enable);

        if (enable) {
            context.getSource().sendFeedback(Text.literal("Remote filters have been enabled").formatted(Formatting.GREEN));
            executeRefresh(context);
        } else {
            context.getSource().sendFeedback(Text.literal("Remote filters have been disabled").formatted(Formatting.RED));
        }

        return 1;
    }

    /**
     * Executes the set remote URL command.
     * 
     * @param context The command context
     * @return 1 for success
     */
    private static int executeSetRemoteUrl(CommandContext<FabricClientCommandSource> context) {
        String url = StringArgumentType.getString(context, "url");
        FilterConfig.getInstance().setRemoteUrl(url);

        context.getSource().sendFeedback(Text.literal("Remote URL set to: " + url).formatted(Formatting.GREEN));

        if (FilterConfig.getInstance().isUseRemoteFilters()) {
            executeRefresh(context);
        } else {
            context.getSource().sendFeedback(Text.literal("Note: Remote filters are currently disabled. Use /adblock remote enable to enable them.")
                    .formatted(Formatting.YELLOW));
        }

        return 1;
    }

    /**
     * Executes the custom enable/disable command.
     * 
     * @param context The command context
     * @param enable Whether to enable or disable custom filters
     * @return 1 for success
     */
    private static int executeCustomEnable(CommandContext<FabricClientCommandSource> context, boolean enable) {
        FilterConfig.getInstance().setUseCustomFilters(enable);

        if (enable) {
            context.getSource().sendFeedback(Text.literal("Custom filters have been enabled").formatted(Formatting.GREEN));
            executeRefresh(context);
        } else {
            context.getSource().sendFeedback(Text.literal("Custom filters have been disabled").formatted(Formatting.RED));
        }

        return 1;
    }

    /**
     * Executes the debug enable/disable command.
     * 
     * @param context The command context
     * @param enable Whether to enable or disable debug mode
     * @return 1 for success
     */
    private static int executeDebugEnable(CommandContext<FabricClientCommandSource> context, boolean enable) {
        FilterConfig.getInstance().setDebugMode(enable);

        if (enable) {
            context.getSource().sendFeedback(Text.literal("Debug mode has been enabled. Blocked messages will be shown with hover info.").formatted(Formatting.GREEN));
        } else {
            context.getSource().sendFeedback(Text.literal("Debug mode has been disabled. Blocked messages will be silently removed.").formatted(Formatting.RED));
        }

        return 1;
    }

    /**
     * Executes the autorefresh enable/disable command.
     * 
     * @param context The command context
     * @param enable Whether to enable or disable autorefresh
     * @return 1 for success
     */
    private static int executeAutoRefreshEnable(CommandContext<FabricClientCommandSource> context, boolean enable) {
        FilterConfig config = FilterConfig.getInstance();
        config.setAutoRefreshEnabled(enable);

        // Update the autorefresh scheduler
        MessageFilter.getInstance().startAutoRefreshIfEnabled();

        if (enable) {
            context.getSource().sendFeedback(Text.literal("Auto-refresh has been enabled. Remote filters will refresh every " 
                    + config.getAutoRefreshDelay() + " minutes.").formatted(Formatting.GREEN));
        } else {
            context.getSource().sendFeedback(Text.literal("Auto-refresh has been disabled.").formatted(Formatting.RED));
        }

        return 1;
    }

    /**
     * Executes the set autorefresh delay command.
     * 
     * @param context The command context
     * @return 1 for success
     */
    private static int executeSetAutoRefreshDelay(CommandContext<FabricClientCommandSource> context) {
        int minutes = IntegerArgumentType.getInteger(context, "minutes");
        FilterConfig config = FilterConfig.getInstance();
        config.setAutoRefreshDelay(minutes);

        // Update the autorefresh scheduler if enabled
        MessageFilter.getInstance().startAutoRefreshIfEnabled();

        context.getSource().sendFeedback(Text.literal("Auto-refresh delay set to " + minutes + " minutes.").formatted(Formatting.GREEN));

        if (!config.isAutoRefreshEnabled()) {
            context.getSource().sendFeedback(Text.literal("Note: Auto-refresh is currently disabled. Use /adblock autorefresh enable to enable it.")
                    .formatted(Formatting.YELLOW));
        }

        return 1;
    }

    /**
     * Executes the help command.
     * 
     * @param context The command context
     * @return 1 for success
     */
    private static int executeHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("=== AdBlock Commands ===").formatted(Formatting.GOLD));
        context.getSource().sendFeedback(Text.literal("/adblock enable").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Enable the AdBlock filter").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock disable").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Disable the AdBlock filter").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock status").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Show current filter status").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock refresh").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Refresh filter lists").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock remote enable|disable").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Enable/disable remote filters").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock remote url <url>").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Set remote filter URL").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock custom enable|disable").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Enable/disable custom filters").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock debug enable|disable").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Enable/disable debug mode (shows blocked messages with hover info)").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock autorefresh enable|disable").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Enable/disable automatic refreshing of remote filters").formatted(Formatting.WHITE)));
        context.getSource().sendFeedback(Text.literal("/adblock autorefresh delay <minutes>").formatted(Formatting.YELLOW)
                .append(Text.literal(" - Set the delay between automatic refreshes (in minutes)").formatted(Formatting.WHITE)));

        return 1;
    }
}

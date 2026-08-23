package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.gui.SettingsGui;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HavocCasinoCommand implements CommandExecutor, TabCompleter {

    private final HavocCasino plugin;

    public HavocCasinoCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        // Player-facing self toggle — available without admin permission.
        if (args.length >= 1 && args[0].equalsIgnoreCase("messages")) {
            return handleMessages(sender, args);
        }

        if (!sender.hasPermission("havoccasino.admin")) {
            Msg.force(sender, "<red>You don't have permission to use this.");
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.casinoConfig().reload();
                plugin.crateManager().load();
                Msg.force(sender, "<green>Configuration and crates reloaded.");
                return true;
            case "jackpot":
                return handleJackpot(sender, args);
            default:
                help(sender);
                return true;
        }
    }

    private boolean handleMessages(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.force(sender, "<red>Only players can change message settings.");
            return true;
        }
        if (!player.hasPermission("havoccasino.messages")) {
            Msg.force(player, "<red>You don't have permission to use this.");
            return true;
        }
        if (args.length >= 2) {
            String value = args[1].toLowerCase();
            if (value.equals("on") || value.equals("enable")) {
                plugin.playerSettings().setMessagesEnabled(player.getUniqueId(), true);
                plugin.playerSettings().save();
                plugin.messages().force(player, "settings.enabled");
                return true;
            }
            if (value.equals("off") || value.equals("disable")) {
                plugin.playerSettings().setMessagesEnabled(player.getUniqueId(), false);
                plugin.playerSettings().save();
                plugin.messages().force(player, "settings.disabled");
                return true;
            }
        }
        new SettingsGui(plugin, player).open();
        return true;
    }

    private boolean handleJackpot(CommandSender sender, String[] args) {
        // /hc jackpot <set|add> <amount>
        if (args.length < 3) {
            Msg.force(sender, "<gray>Usage: <white>/hc jackpot <set|add> <amount>");
            return true;
        }
        Double amount = Numbers.parsePositive(args[2]);
        if (amount == null) {
            Msg.force(sender, "<red>Invalid amount.");
            return true;
        }
        if (args[1].equalsIgnoreCase("set")) {
            plugin.jackpotManager().setPool(amount);
        } else if (args[1].equalsIgnoreCase("add")) {
            plugin.jackpotManager().addToPool(amount);
        } else {
            Msg.force(sender, "<gray>Usage: <white>/hc jackpot <set|add> <amount>");
            return true;
        }
        plugin.jackpotManager().save();
        Msg.force(sender, "<green>Jackpot pool is now <gold>"
                + plugin.currencyService().format(plugin.jackpotManager().pool())
                + "<green>.");
        return true;
    }

    private void help(CommandSender sender) {
        Msg.force(sender, "<gold><bold>HavocCasino</bold> <gray>admin commands:");
        Msg.forceRaw(sender, "<gray>• <white>/hc reload");
        Msg.forceRaw(sender, "<gray>• <white>/hc jackpot <set|add> <amount>");
        Msg.forceRaw(sender, "<gray>• <white>/hc messages <gray>(toggle your messages)");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            if (sender.hasPermission("havoccasino.messages")) {
                subs.add("messages");
            }
            if (sender.hasPermission("havoccasino.admin")) {
                subs.addAll(Arrays.asList("reload", "jackpot"));
            }
            return filter(subs, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("messages")
                && sender.hasPermission("havoccasino.messages")) {
            return filter(Arrays.asList("on", "off"), args[1]);
        }
        if (!sender.hasPermission("havoccasino.admin")) {
            return List.of();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("jackpot")) {
            return filter(Arrays.asList("set", "add"), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                out.add(option);
            }
        }
        return out;
    }
}

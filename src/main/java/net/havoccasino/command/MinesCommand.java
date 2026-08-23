package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.economy.CurrencyService;
import net.havoccasino.gui.MinesGui;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class MinesCommand implements CommandExecutor {

    private final HavocCasino plugin;

    public MinesCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can play mines.");
            return true;
        }
        if (!player.hasPermission("havoccasino.mines")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }
        if (args.length == 0) {
            Msg.send(player, "<gray>Usage: <white>/mines <bet> [mines]");
            return true;
        }

        Double parsed = Numbers.parsePositive(args[0]);
        if (parsed == null) {
            Msg.send(player, "<red>'" + args[0] + "' is not a valid bet.");
            return true;
        }
        double bet = parsed;

        int mines = plugin.casinoConfig().minesDefault();
        if (args.length >= 2) {
            Integer parsedMines = tryInt(args[1]);
            if (parsedMines == null) {
                Msg.send(player, "<red>'" + args[1] + "' is not a valid mine count.");
                return true;
            }
            mines = parsedMines;
        }

        int minMines = Math.max(1, plugin.casinoConfig().minesMin());
        int maxMines = Math.min(MinesGui.TOTAL_TILES - 1, plugin.casinoConfig().minesMax());
        if (mines < minMines || mines > maxMines) {
            Msg.send(player, "<red>Mines must be between <white>" + minMines
                    + " <red>and <white>" + maxMines + "<red>.");
            return true;
        }

        CurrencyService bank = plugin.currencyService();
        double min = plugin.casinoConfig().minBet();
        double max = plugin.casinoConfig().maxBet();
        if (bet < min || bet > max) {
            Msg.send(player, "<red>Bet must be between <white>" + bank.format(min)
                    + " <red>and <white>" + bank.format(max) + "<red>.");
            return true;
        }
        if (!bank.has(player, bet)) {
            Msg.send(player, "<red>You can't afford that bet. Balance: <white>"
                    + bank.format(bank.balance(player)));
            return true;
        }
        if (!bank.withdraw(player, bet)) {
            Msg.send(player, "<red>Transaction failed. Try again.");
            return true;
        }

        new MinesGui(plugin, player, bet, mines, plugin.casinoConfig().minesHouseEdge()).open();
        return true;
    }

    private Integer tryInt(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

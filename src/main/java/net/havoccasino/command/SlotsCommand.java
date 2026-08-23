package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.economy.CurrencyService;
import net.havoccasino.economy.CurrencyType;
import net.havoccasino.game.SlotResult;
import net.havoccasino.gui.SlotGui;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SlotsCommand implements CommandExecutor {

    private final HavocCasino plugin;

    public SlotsCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can play slots.");
            return true;
        }
        if (!player.hasPermission("havoccasino.slots")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }
        if (args.length == 0) {
            Msg.send(player, "<gray>Usage: <white>/slots <bet> [money|rubies]");
            return true;
        }

        Double parsed = Numbers.parsePositive(args[0]);
        if (parsed == null) {
            Msg.send(player, "<red>'" + args[0] + "' is not a valid bet.");
            return true;
        }
        double bet = parsed;

        CurrencyType currency = args.length >= 2
                ? CurrencyType.fromString(args[1], plugin.casinoConfig().defaultCurrency())
                : plugin.casinoConfig().defaultCurrency();

        CurrencyService bank = plugin.currencyService();
        if (!bank.isAvailable(currency)) {
            Msg.send(player, "<red>That currency isn't available on this server.");
            return true;
        }

        double min = plugin.casinoConfig().minBet();
        double max = plugin.casinoConfig().maxBet();
        if (bet < min || bet > max) {
            Msg.send(player, "<red>Bet must be between <white>" + bank.format(currency, min)
                    + " <red>and <white>" + bank.format(currency, max) + "<red>.");
            return true;
        }
        if (!bank.has(player, currency, bet)) {
            Msg.send(player, "<red>You can't afford that bet. Balance: <white>"
                    + bank.format(currency, bank.balance(player, currency)));
            return true;
        }
        if (!bank.withdraw(player, currency, bet)) {
            Msg.send(player, "<red>Transaction failed. Try again.");
            return true;
        }

        SlotResult result = plugin.slotMachine().spin(bet);
        new SlotGui(plugin, player, result, bet, currency).open();
        return true;
    }
}

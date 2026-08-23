package net.havoccasino.command;

import net.havoccasino.HavocCasino;
import net.havoccasino.economy.CurrencyService;
import net.havoccasino.economy.CurrencyType;
import net.havoccasino.game.JackpotManager;
import net.havoccasino.util.Msg;
import net.havoccasino.util.Numbers;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class JackpotCommand implements CommandExecutor {

    private final HavocCasino plugin;

    public JackpotCommand(HavocCasino plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "<red>Only players can play the jackpot.");
            return true;
        }
        if (!player.hasPermission("havoccasino.jackpot")) {
            Msg.send(player, "<red>You don't have permission to use this.");
            return true;
        }

        CurrencyType currency = plugin.casinoConfig().jackpotCurrency();
        CurrencyService bank = plugin.currencyService();
        JackpotManager jackpot = plugin.jackpotManager();

        if (args.length == 0) {
            plugin.messages().send(player, "jackpot.info-pool",
                    "pool", bank.format(currency, jackpot.pool()));
            plugin.messages().send(player, "jackpot.info-howto",
                    "chance", Numbers.trim(plugin.casinoConfig().jackpotWinChance() * 100.0));
            return true;
        }

        Double parsed = Numbers.parsePositive(args[0]);
        if (parsed == null) {
            Msg.send(player, "<red>'" + args[0] + "' is not a valid amount.");
            return true;
        }
        double bet = parsed;

        double minEntry = plugin.casinoConfig().jackpotMinBet();
        if (bet < minEntry) {
            Msg.send(player, "<red>Minimum entry is <white>" + bank.format(currency, minEntry) + "<red>.");
            return true;
        }
        if (!bank.isAvailable(currency)) {
            Msg.send(player, "<red>The jackpot currency isn't available on this server.");
            return true;
        }
        if (!bank.has(player, currency, bet)) {
            Msg.send(player, "<red>You can't afford that. Balance: <white>"
                    + bank.format(currency, bank.balance(player, currency)));
            return true;
        }
        if (!bank.withdraw(player, currency, bet)) {
            Msg.send(player, "<red>Transaction failed. Try again.");
            return true;
        }

        JackpotManager.JackpotOutcome outcome = jackpot.roll(bet);
        jackpot.save();

        if (outcome.won) {
            bank.deposit(player, currency, outcome.amountWon);
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
            String amount = bank.format(currency, outcome.amountWon);
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                plugin.messages().send(online, "jackpot.win-broadcast",
                        "player", player.getName(), "amount", amount);
            }
        } else {
            plugin.messages().send(player, "jackpot.lose",
                    "pool", bank.format(currency, outcome.newPool));
        }
        return true;
    }
}

package net.havoccasino.economy;

import net.havoccasino.config.CasinoConfig;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

/**
 * Single entry point for balance checks and transactions across both
 * currencies. Money flows through Vault; rubies through the RubyStore.
 */
public final class CurrencyService {

    private final VaultHook vault;
    private final RubyStore rubies;
    private final CasinoConfig config;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat rubyFormat = new DecimalFormat("#,##0");

    public CurrencyService(VaultHook vault, RubyStore rubies, CasinoConfig config) {
        this.vault = vault;
        this.rubies = rubies;
        this.config = config;
    }

    public boolean isAvailable(CurrencyType type) {
        if (type == CurrencyType.MONEY) {
            return vault.isEnabled();
        }
        return true;
    }

    public double balance(Player player, CurrencyType type) {
        if (type == CurrencyType.MONEY) {
            return vault.isEnabled() ? vault.economy().getBalance(player) : 0D;
        }
        return rubies.get(player.getUniqueId());
    }

    public boolean has(Player player, CurrencyType type, double amount) {
        if (type == CurrencyType.MONEY) {
            return vault.isEnabled() && vault.economy().has(player, amount);
        }
        return rubies.has(player.getUniqueId(), (long) Math.ceil(amount));
    }

    public boolean withdraw(Player player, CurrencyType type, double amount) {
        if (type == CurrencyType.MONEY) {
            return vault.isEnabled() && vault.economy().withdrawPlayer(player, amount).transactionSuccess();
        }
        return rubies.withdraw(player.getUniqueId(), (long) Math.ceil(amount));
    }

    public void deposit(Player player, CurrencyType type, double amount) {
        if (type == CurrencyType.MONEY) {
            if (vault.isEnabled()) {
                vault.economy().depositPlayer(player, amount);
            }
            return;
        }
        rubies.add(player.getUniqueId(), (long) Math.floor(amount));
    }

    public String format(CurrencyType type, double amount) {
        if (type == CurrencyType.MONEY) {
            return config.moneySymbol() + moneyFormat.format(amount);
        }
        return rubyFormat.format(amount) + " " + config.rubyName();
    }
}

package net.havoccasino.economy;

import net.milkbowl.vault.economy.Economy;
import net.havoccasino.HavocCasino;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Optional bridge to a Vault economy provider. If Vault is absent the plugin
 * still works with rubies only.
 */
public final class VaultHook {

    private final HavocCasino plugin;
    private Economy economy;

    public VaultHook(HavocCasino plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
        }
    }

    public boolean isEnabled() {
        return economy != null;
    }

    public Economy economy() {
        return economy;
    }
}

package net.havoccasino;

import net.havoccasino.command.JackpotCommand;
import net.havoccasino.command.CratesCommand;
import net.havoccasino.command.MinesCommand;
import net.havoccasino.command.HavocCasinoCommand;
import net.havoccasino.command.SlotsCommand;
import net.havoccasino.config.CasinoConfig;
import net.havoccasino.economy.CurrencyService;
import net.havoccasino.economy.VaultHook;
import net.havoccasino.game.CrateManager;
import net.havoccasino.game.JackpotManager;
import net.havoccasino.game.SlotMachine;
import net.havoccasino.gui.GuiListener;
import net.havoccasino.hook.HavocExpansion;
import net.havoccasino.message.Messages;
import net.havoccasino.settings.PlayerSettings;
import net.havoccasino.util.Msg;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * HavocCasino entry point. Wires the currency layer, games and commands together.
 */
public final class HavocCasino extends JavaPlugin {

    private CasinoConfig casinoConfig;
    private VaultHook vaultHook;
    private CurrencyService currencyService;
    private SlotMachine slotMachine;
    private JackpotManager jackpotManager;
    private CrateManager crateManager;
    private PlayerSettings playerSettings;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.casinoConfig = new CasinoConfig(this);
        this.casinoConfig.reload();

        this.playerSettings = new PlayerSettings(this);
        this.playerSettings.load();
        Msg.init(this.casinoConfig, this.playerSettings);

        this.messages = new Messages(this);
        this.messages.load();

        this.vaultHook = new VaultHook(this);
        this.vaultHook.setup();
        if (!this.vaultHook.isEnabled()) {
            getLogger().severe("Vault economy not found. HavocCasino needs Vault plus an "
                    + "economy plugin (e.g. EssentialsX). Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.currencyService = new CurrencyService(this.vaultHook, this.casinoConfig);
        this.slotMachine = new SlotMachine(this.casinoConfig);
        this.jackpotManager = new JackpotManager(this, this.casinoConfig, this.currencyService);
        this.jackpotManager.load();

        this.crateManager = new CrateManager(this);
        this.crateManager.load();

        registerCommand("slots", new SlotsCommand(this));
        registerCommand("mines", new MinesCommand(this));
        registerCommand("jackpot", new JackpotCommand(this));
        CratesCommand cratesCommand = new CratesCommand(this);
        registerCommand("crates", cratesCommand);
        if (getCommand("crates") != null) {
            getCommand("crates").setTabCompleter(cratesCommand);
        }
        HavocCasinoCommand admin = new HavocCasinoCommand(this);
        registerCommand("havoccasino", admin);
        if (getCommand("havoccasino") != null) {
            getCommand("havoccasino").setTabCompleter(admin);
        }

        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                HavocExpansion.register(this);
                getLogger().info("PlaceholderAPI found — placeholders registered.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        getLogger().info("HavocCasino enabled. Using Vault economy.");
    }

    @Override
    public void onDisable() {
        if (jackpotManager != null) {
            jackpotManager.save();
        }
        if (playerSettings != null) {
            playerSettings.save();
        }
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("Command '" + name + "' missing from plugin.yml.");
        }
    }

    public CasinoConfig casinoConfig() {
        return casinoConfig;
    }

    public CurrencyService currencyService() {
        return currencyService;
    }

    public SlotMachine slotMachine() {
        return slotMachine;
    }

    public JackpotManager jackpotManager() {
        return jackpotManager;
    }

    public CrateManager crateManager() {
        return crateManager;
    }

    public PlayerSettings playerSettings() {
        return playerSettings;
    }

    public Messages messages() {
        return messages;
    }
}

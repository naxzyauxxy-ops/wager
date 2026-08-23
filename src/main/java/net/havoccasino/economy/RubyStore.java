package net.havoccasino.economy;

import net.havoccasino.HavocCasino;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores per-player ruby balances in rubies.yml, keyed by UUID.
 */
public final class RubyStore {

    private final HavocCasino plugin;
    private final File file;
    private final ConcurrentHashMap<UUID, Long> balances = new ConcurrentHashMap<>();

    public RubyStore(HavocCasino plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "rubies.yml");
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            try {
                balances.put(UUID.fromString(key), cfg.getLong(key));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed keys.
            }
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        balances.forEach((uuid, amount) -> cfg.set(uuid.toString(), amount));
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save rubies.yml: " + e.getMessage());
        }
    }

    public long get(UUID uuid) {
        return balances.getOrDefault(uuid, 0L);
    }

    public boolean has(UUID uuid, long amount) {
        return get(uuid) >= amount;
    }

    public void set(UUID uuid, long amount) {
        balances.put(uuid, Math.max(0L, amount));
    }

    public void add(UUID uuid, long amount) {
        set(uuid, get(uuid) + amount);
    }

    public boolean withdraw(UUID uuid, long amount) {
        long current = get(uuid);
        if (current < amount) {
            return false;
        }
        set(uuid, current - amount);
        return true;
    }
}

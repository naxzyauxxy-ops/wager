package net.havoccasino.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Routes clicks/drags/closes for HavocCasino GUIs and prevents item removal.
 */
public final class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof SlotHolder) {
            event.setCancelled(true);
            return;
        }
        if (holder instanceof MinesHolder minesHolder) {
            event.setCancelled(true);
            int raw = event.getRawSlot();
            if (raw >= 0 && raw < event.getInventory().getSize() && minesHolder.getGui() != null) {
                minesHolder.getGui().handleClick(raw);
            }
            return;
        }
        if (holder instanceof SettingsHolder settingsHolder) {
            event.setCancelled(true);
            int raw = event.getRawSlot();
            if (raw >= 0 && raw < event.getInventory().getSize() && settingsHolder.getGui() != null) {
                settingsHolder.getGui().handleClick(raw);
            }
            return;
        }
        if (holder instanceof CrateHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof SlotHolder || holder instanceof MinesHolder
                || holder instanceof SettingsHolder || holder instanceof CrateHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof MinesHolder minesHolder
                && minesHolder.getGui() != null) {
            minesHolder.getGui().handleClose();
        }
    }
}

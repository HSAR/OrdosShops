package org.landofordos.ordosshops.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.landofordos.ordosshops.events.TransactionEvent;
import org.landofordos.ordosshops.events.TransactionEvent.TransactionOutcome;

public class TransactionListener implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public static void onTransaction(TransactionEvent event) {
        
        // event.getShopInventory().remove(event.getStockItem());
        removeFromInventory(event.getShopInventory(), event.getStockItem(), event.getStockItem().getAmount());
        event.getClientInventory().addItem(event.getStockItem());

        // event.getClientInventory().remove(event.getPaymentItem());
        int newAmount = event.getClient().getItemInHand().getAmount() - event.getPaymentItem().getAmount();
        if (newAmount > 0) {
            event.getClient().getItemInHand().setAmount(newAmount);
        } else {
            event.getClientInventory().remove(event.getClient().getItemInHand());
        }
        event.getShopInventory().addItem(event.getPaymentItem());

        event.getClient().updateInventory();

        event.setTransactionOutcome(TransactionOutcome.SUCCESS);

    }

    protected static void removeFromInventory(Inventory inventory, ItemStack itemToRemove, int amount) {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (itemToRemove.isSimilar(item)) {
                int newAmount;
                if (item.getAmount() > amount) {
                    newAmount = item.getAmount() - amount;
                } else {
                    newAmount = 0;
                    removeFromInventory(inventory, itemToRemove, amount - item.getAmount());
                }
                if (newAmount > 0) {
                    item.setAmount(newAmount);
                } else {
                    inventory.remove(item);
                }
                return;
            }
        }
    }

}

package org.landofordos.ordosshops.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TransactionEvent extends Event {

    public enum TransactionOutcome {
        SHOP_DOES_NOT_BUY_THIS_ITEM,
        SHOP_DOES_NOT_SELL_THIS_ITEM,

        CLIENT_DOES_NOT_HAVE_PERMISSION,

        CLIENT_DOES_NOT_HAVE_ENOUGH_MONEY,
        SHOP_DOES_NOT_HAVE_ENOUGH_MONEY,

        CLIENT_DEPOSIT_FAILED,
        SHOP_DEPOSIT_FAILED,

        NOT_ENOUGH_SPACE_IN_CHEST,
        NOT_ENOUGH_SPACE_IN_INVENTORY,

        NOT_ENOUGH_STOCK_IN_CHEST,
        NOT_ENOUGH_STOCK_IN_INVENTORY,

        INVALID_SHOP,

        //SPAM_CLICKING_PROTECTION,
        //CREATIVE_MODE_PROTECTION,
        SHOP_IS_RESTRICTED,

        OTHER, //For plugin use!

        SUCCESS,
        
        DEFAULT
    }
    
    private static final HandlerList handlers = new HandlerList();

    private final Inventory shopInventory;
    private final Inventory clientInventory;

    private final ItemStack stock;
    private final ItemStack payment;

    private final Player client;
    //private final OfflinePlayer owner;

    private TransactionOutcome transactionOutcome = TransactionOutcome.DEFAULT;

    public TransactionEvent(Inventory shopInventory, Player client, ItemStack stock, ItemStack payment) {
        super();
        this.shopInventory = shopInventory;
        this.clientInventory = client.getInventory();
        this.stock = stock;
        this.payment = payment;
        this.client = client;
    }
    
    public TransactionOutcome getTransactionOutcome() {
        return transactionOutcome;
    }


    public void setTransactionOutcome(TransactionOutcome transactionOutcome) {
        this.transactionOutcome = transactionOutcome;
    }

    public Inventory getShopInventory() {
        return shopInventory;
    }

    public Inventory getClientInventory() {
        return clientInventory;
    }

    public ItemStack getStockItem() {
        return stock;
    }

    public ItemStack getPaymentItem() {
        return payment;
    }

    public Player getClient() {
        return client;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

}

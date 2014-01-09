package org.landofordos.ordosshops;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.bergerkiller.bukkit.common.inventory.ItemParser;

public class OrdosShop {

    private ItemParser topLineParser, bottomLineParser;
    private String owner;

    protected final Chest chest;

    public OrdosShop(Chest chest, ItemParser topLineParser, ItemParser bottomLineParser, String owner) {
        this.chest = chest;
        this.topLineParser = topLineParser;
        this.bottomLineParser = bottomLineParser;
        this.owner = owner;
    }

    public int getPaymentAmount() {
        return topLineParser.getAmount();
    }

    public ItemStack getPaymentItem() {
        if (topLineParser.hasData()) {
            return topLineParser.getItemStack();
        } else {
            return new ItemStack(this.getPaymentMaterial(), this.getPaymentAmount());
        }
    }

    public Material getPaymentMaterial() {
        return topLineParser.getType();
    }

    public ItemParser getTopLineParser() {
        return topLineParser;
    }

    public int getStockAmount() {
        return bottomLineParser.getAmount();
    }

    public ItemStack getStockItem() {
        if (bottomLineParser.hasData()) {
            return bottomLineParser.getItemStack();
        } else {
            return new ItemStack(this.getStockMaterial(), this.getStockAmount());
        }
    }

    public Material getStockMaterial() {
        return bottomLineParser.getType();
    }

    public ItemParser getBottomLineParser() {
        return bottomLineParser;
    }

    public String getOwner() {
        return owner;
    }

    public Chest getChest() {
        return chest;
    }

    public boolean canPay(ItemStack items) {
        if (items != null) {
            if (items.getType().equals(topLineParser.getType()) && (items.getAmount() >= topLineParser.getAmount())) {
                return true;
            }
        }
        return false;
    }
}

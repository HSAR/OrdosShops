package org.landofordos.ordosshops;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.landofordos.ordosshops.events.ProtectionCheckEvent;
import org.landofordos.ordosshops.events.TransactionEvent;
import org.landofordos.ordosshops.events.TransactionEvent.TransactionOutcome;
import org.landofordos.ordosshops.listeners.LocketteListener;
import org.landofordos.ordosshops.listeners.TransactionListener;

import com.bergerkiller.bukkit.common.inventory.ItemParser;

public class OrdosShops extends JavaPlugin implements Listener {

    private static enum Dependency {
        // LWC,
        Lockette// ,
        // WorldGuard,
    }

    private static enum StockCheckResult {
        IN_STOCK, OUT_OF_STOCK, INVALID_ITEM, INVALID_SHOP
    }

    // Important plugin objects
    private static Server server;
    private static Logger logger;
    private FileConfiguration config;
    //
    private boolean verbose;
    private boolean enabled;

    public void onDisable() {
        logger.info("Disabled.");
    }

    public void onEnable() {
        // static reference to this plugin and the server
        // plugin = this;
        server = getServer();
        // start the logger
        logger = getLogger();
        // save config to default location if not already there
        this.saveDefaultConfig();
        loadConfig();

        // register events
        registerEvents();
    }

    private void loadConfig() { //
        // ====== CONFIG LOAD START ======
        //
        // set config var
        config = this.getConfig();
        // first-run initialisation, if necessary
        final boolean firstrun = config.getBoolean("pluginvars.firstrun");
        if (firstrun) {
            // Whatever first run initialisation is required
            config.set("pluginvars.firstrun", false);
            this.saveConfig();
            if (verbose) {
                logger.info("First-run initialisation complete.");
            }
        }
        // verbose logging? retrieve value from config file.
        verbose = config.getBoolean("pluginvars.verboselogging");
        if (verbose) {
            logger.info("Verbose logging enabled.");
        } else {
            logger.info("Verbose logging disabled.");
        }
        // plugin effect enabled? retrieve value from config file.
        enabled = config.getBoolean("pluginvars.enabled");
        //
        // ====== CONFIG LOAD FINISH ======
        //
    }

    public void registerEvent(Listener listener) {
        server.getPluginManager().registerEvents(listener, this);
    }

    private void registerEvents() {
        registerEvent(this);
        registerEvent(new TransactionListener());
        loadPlugins();
    }

    public void loadPlugins() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for (String dependency : this.getDependencies()) {
            Plugin plugin = pluginManager.getPlugin(dependency);

            if (plugin != null) {
                initializePlugin(dependency, plugin);
            }
        }
        // loadEconomy();
    }

    private void initializePlugin(String name, Plugin plugin) { // Really messy, right? But it's short and fast :)
        Dependency dependency;

        try {
            dependency = Dependency.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return;
        }

        Listener listener = null;

        switch (dependency) {
        // Protection plugins
        /*
         * case LWC: listener = new LightweightChestProtection(); break;
         */
        case Lockette:
            listener = new LocketteListener();
            break;

        // Terrain protection plugins
        /*
         * case WorldGuard: WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin; boolean inUse = Properties.WORLDGUARD_USE_PROTECTION ||
         * Properties.WORLDGUARD_INTEGRATION;
         * 
         * if (!inUse) { return; }
         * 
         * if (Properties.WORLDGUARD_USE_PROTECTION) { ChestShop.registerListener(new WorldGuardProtection(worldGuard)); }
         * 
         * if (Properties.WORLDGUARD_INTEGRATION) { listener = new WorldGuardBuilding(worldGuard); }
         * 
         * break;
         */
        // Other plugins
        /*
         * case Heroes: Heroes heroes = Heroes.getHeroes(plugin);
         * 
         * if (heroes == null) { return; }
         * 
         * listener = heroes; break; case OddItem: MaterialUtil.Odd.initialize(); break;
         */
        }

        if (listener != null) {
            registerEvent(listener);
        }

        PluginDescriptionFile description = plugin.getDescription();
        logger.info(description.getName() + " version " + description.getVersion() + " loaded.");
    }

    public static void callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    public List<String> getDependencies() {
        return getDescription().getSoftDepend();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 1) {
            if (args[0].equals("reload")) {
                if (sender.hasPermission("ordosloot.reloadconfig")) {
                    loadConfig();
                    sender.sendMessage(ChatColor.YELLOW + "Configuration reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to reload OrdosLoot's config.");
                    return true;
                }
            }
            if (args[0].equals("toggle")) {
                if (sender.hasPermission("ordosloot.ingametoggle")) {
                    enabled = !enabled;
                    sender.sendMessage("Enchanted loot: " + enabled);
                    // save to config
                    config.set("enabled", enabled);
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to toggle OrdosLoot.");
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean isShopSign(Sign sign) {
        // top line has colour AND third line says "for"
        if (!sign.getLine(0).equals(ChatColor.stripColor(sign.getLine(0))) && (sign.getLine(2).equalsIgnoreCase("for"))) {
            return true;
        }
        return false;
    }

    /**
     * @param sign
     *            - sign to check is a shop
     * @return OrdosShop object if true, null if not
     */
    protected static OrdosShop getShop(Sign sign) {
        BlockState blockState = sign.getLocation().add(0, -1, 0).getBlock().getState();
        if (blockState instanceof Chest) {
            ItemParser topLineParser = ItemParser.parse(sign.getLine(1));
            ItemParser bottomLineParser = ItemParser.parse(sign.getLine(3));
            if ((topLineParser.getType() != Material.AIR) && (bottomLineParser.getType() != Material.AIR)) {
                return new OrdosShop((Chest) blockState, topLineParser, bottomLineParser, sign.getLine(0));
            }
        }
        return null;

    }

    protected boolean inStock(Chest chest, ItemStack item, int amount, boolean checkData) {
        ItemStack[] contents = chest.getInventory().getContents();
        int total = 0;
        for (ItemStack i : contents) {
            if (i != null) {
                if (checkData) {
                    if (item.isSimilar(i)) {
                        total = total + i.getAmount();
                    }
                } else {
                    if (item.getType().equals(i.getType())) {
                        total = total + i.getAmount();
                    }
                }
            }
        }
        return (total >= amount);
    }

    /**
     * @param sign
     *            - shop to check
     * @return true if shop in stock, false otherwise
     * 
     *         Changes top line of shop sign to red if out of stock, green if in stock
     */
    protected StockCheckResult checkStock(Sign sign) {
        OrdosShop shop = getShop(sign);
        if (shop == null) {
            return StockCheckResult.INVALID_SHOP;
        } else {
            return checkStock(shop);
        }
    }

    protected StockCheckResult checkStock(OrdosShop shop) {
        Sign sign = (Sign) shop.getChest().getLocation().add(0, 1, 0).getBlock().getState();
        if ((shop.getTopLineParser().getType() != Material.AIR) && (shop.getBottomLineParser().getType() != Material.AIR)) {
            String topLine = ChatColor.stripColor(sign.getLine(0));
            if (inStock(shop.getChest(), shop.getBottomLineParser().getItemStack(), shop.getBottomLineParser().getAmount(), shop
                    .getBottomLineParser().hasData())) {
                sign.setLine(0, ChatColor.DARK_GREEN + topLine);
                sign.update();
                return StockCheckResult.IN_STOCK;
            } else {
                sign.setLine(0, ChatColor.RED + topLine);
                sign.update();
                return StockCheckResult.OUT_OF_STOCK;
            }
        }
        return StockCheckResult.INVALID_ITEM;
    }

    public static boolean canAccess(Player player, Block block) {
        ProtectionCheckEvent event = new ProtectionCheckEvent(block, player);
        callEvent(event);

        return event.getResult() != Event.Result.DENY;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    // EventPriority.NORMAL by default
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            ItemStack item = event.getItem();
            if ((block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN_POST)) {
                Sign sign = (Sign) block.getState();
                if (isShopSign(sign)) {
                    OrdosShop shop = getShop(sign);
                    // assume the ItemParser succeeds, as it would have been checked at shop creation time
                    // check the shop has enough stock to sell, and the player has enough items to pay with
                    if ((checkStock(sign) == StockCheckResult.IN_STOCK) && shop.canPay(item)) {
                        // check player has enough room in their inventory
                        if (player.getInventory().firstEmpty() != -1) {
                            // commence transaction
                            TransactionEvent tEvent = new TransactionEvent(shop.getChest().getInventory(), player, shop.getStockItem(),
                                    shop.getPaymentItem());
                            callEvent(tEvent);
                            if (tEvent.getTransactionOutcome() == TransactionOutcome.SUCCESS) {
                                logger.info("Player " + player.getName() + " bought " + tEvent.getStockItem() + " for "
                                        + shop.getPaymentItem());
                            } else {
                                logger.info("Transaction failed.");
                            }
                        }
                    } else {
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        } else {
            // ignore event
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    // EventPriority.NORMAL by default
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        Player player = event.getPlayer();
        // middle line should read "for"
        if (lines[2].equalsIgnoreCase("for")) {
            // then check it has valid buy and sell fields
            ItemParser topLineParser = ItemParser.parse(lines[1]);
            ItemParser bottomLineParser = ItemParser.parse(lines[3]);
            if (topLineParser.hasAmount() && topLineParser.hasType() && bottomLineParser.hasAmount() && bottomLineParser.hasType()) {
                // check there is a chest beneath the sign
                Block chestBlock = event.getBlock().getLocation().add(0, -1, 0).getBlock();
                if (chestBlock.getType().equals(Material.CHEST)) {
                    // check the user has appropriate permissions AND that the chest is not protected
                    if (player.hasPermission("ordosshops.place") && canAccess(player, chestBlock)) {
                        // if all is well, place [PLAYERNAME] at the top of the sign as a confirmation.
                        if (verbose) {
                            Block block = event.getBlock();
                            logger.info("Player " + player.getName() + " created a new OrdosShop at " + block.getLocation().toString());
                        }
                        // check that it is in stock
                        switch (checkStock(new OrdosShop((Chest) chestBlock.getState(), topLineParser, bottomLineParser, lines[0]))) {
                        case IN_STOCK:
                            event.setLine(0, ChatColor.DARK_GREEN + player.getName());
                            break;
                        case OUT_OF_STOCK:
                            event.setLine(0, ChatColor.RED + player.getName());
                            break;
                        default:
                            event.setCancelled(true);
                            event.getBlock().setType(Material.AIR);
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.SIGN));
                            player.sendMessage(ChatColor.YELLOW + "Invalid item name(s)");
                            break;
                        }
                        // event.setLine(1, topLineParser.getAmount() + " " + topLineParser.getType().toString());
                        // event.setLine(3, bottomLineParser.getAmount() + " " + bottomLineParser.getType().toString());
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have permission to create a shop.");
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "This shop has not been set up correctly (needs to be above a chest).");
                    event.setLine(0, "");
                    return;
                }
            } else {
                player.sendMessage(ChatColor.RED + "This shop has not been set up correctly (item names failed parsing).");
                event.setLine(0, "");
                return;
            }
        }
        // if the user tried to fake a sign, cancel the event.
        String line0 = event.getLine(0);
        if ((line0.contains(Utils.stripName(player))) && (!(player.hasPermission("ordosshops.place")))) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to create a shop.");
            logger.info("Player " + player.getName() + " tried to fake an OrdosShop.");
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    // EventPriority.NORMAL by default
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if ((block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN_POST)) {
            Sign sign = (Sign) block.getState();
            if (isShopSign(sign)) {
                // if the sign is an OrdosShops sign, check if users have permission - if not, cancel the event.
                // different permissions required for personal or others' shops.
                if (sign.getLine(0).contains(Utils.stripName(player))) {
                    if (!(player.hasPermission("ordosshops.destroy"))) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to destroy this shop.");
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!(player.hasPermission("ordosshops.admin.destroy"))) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to destroy this shop.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

}

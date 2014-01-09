package org.landofordos.ordosshops.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.landofordos.ordosshops.Utils;
import org.landofordos.ordosshops.events.ProtectionCheckEvent;

/**
 * @author Acrobot
 */
public class LocketteListener implements Listener {
    @EventHandler
    public static void onProtectionCheck(ProtectionCheckEvent event) {
        if (event.getResult() == Event.Result.DENY) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // if not protected, return immediately
        if (!org.yi.acru.bukkit.Lockette.Lockette.isProtected(block)) {
            return;
        }

        String shortPlayerName = Utils.stripName(player);

        if (!org.yi.acru.bukkit.Lockette.Lockette.isUser(block, shortPlayerName, true)) {
            event.setResult(Event.Result.DENY);
        }
    }
}

package com.gmail.gogobebe2.portalcrafting;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PortalListener implements Listener {
    private PortalCrafting plugin;
    private Map<Player, Portal> selectedPortals = new HashMap<>();

    public PortalListener(PortalCrafting plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortalPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block blockPlaced = event.getBlockPlaced();
        if (PortalType.isItemPortal(item)) {
            for (PortalType type : PortalType.values()) {
                if (type.getItem().equals(item)) {
                    PortalCrafting.placePortal(new Portal(type, blockPlaced));
                    event.getPlayer().sendMessage(type.getDisplayName() + ChatColor.GREEN + " placed!");
                }
            }
        }
    }

    @EventHandler
    public void onPortalDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        Portal portal = PortalCrafting.getPortal(block);
        if (portal != null) {
            PortalCrafting.getPortals().remove(portal);
            event.getPlayer().sendMessage(portal.getType().getDisplayName() + ChatColor.GOLD + " removed!");
            if (portal.isLinked()) {
                Portal partner = portal.getPartner();
                Portal.breakLink(portal, partner);
                event.getPlayer().sendMessage(ChatColor.GOLD + "Link between " + portal.getType().getDisplayName()
                        + ChatColor.GOLD + " and " + partner.getType().getDisplayName() + " destroyed.");
            }
        }
    }

    @EventHandler
    public void onBlockRightPortal(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            Portal portal = PortalCrafting.getPortal(block);
            if (portal != null) {
                Portal oppositePortal = selectedPortals.get(player);
                if (selectedPortals.containsKey(player) && portal.getType().equals(oppositePortal.getType().getOpposite())) {
                    player.sendMessage(ChatColor.AQUA + "Linked " + portal.getType().getDisplayName() + ChatColor.AQUA
                            + " and " + oppositePortal.getType().getDisplayName());
                    Portal.createLink(portal, oppositePortal);
                    selectedPortals.remove(player);
                } else {
                    selectedPortals.put(player, portal);
                    player.sendMessage(ChatColor.GREEN + "Selected " + portal.getType().getDisplayName());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerStepOnPortal(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();
        Block toBlock = to.getBlock();
        Block fromBlock = from.getBlock();

        if (toBlock != fromBlock) {
            to.setY(to.getY() - 1);
            Block blockUnderneath = to.getBlock();
            Portal portal = PortalCrafting.getPortal(blockUnderneath);
            if (portal != null) {
                Player player = event.getPlayer();
                if (portal.isLinked()) {
                    if (portal.getType().equals(PortalType.ENTRY)) {
                        Portal partner = portal.getPartner();
                        Location destination = partner.getBlock().getLocation();
                        player.teleport(destination);
                        player.sendMessage(ChatColor.DARK_AQUA + "Whoosh!");
                        player.playSound(destination, Sound.PORTAL_TRAVEL, 2.0F, 1.0F);
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "You can only teleport through entry portals. 1 way only. "
                                + "New recipe for multiway portals coming soon! " +
                                "Bug me at gogobebe2@gmail.com to make me do it.");
                    }

                }
                else {
                    player.sendMessage(ChatColor.RED + "You cannot use this portal yet! It's not linked yet!");
                }
            }
        }
    }
}

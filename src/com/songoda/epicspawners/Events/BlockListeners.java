package com.songoda.epicspawners.Events;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.Entity.EPlayer;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Lang;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlockListeners implements Listener {

    private EpicSpawners plugin = EpicSpawners.pl();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        try {
            if (!e.isCancelled()) {
                if (plugin.getConfig().getBoolean("settings.spawners-repel-liquid")) {
                    if (e.getBlock().isLiquid()) {

                        Block block = e.getBlock();
                        World world = block.getWorld();

                        int blockX = block.getX();
                        int blockY = block.getY();
                        int blockZ = block.getZ();

                        int radius = plugin.getConfig().getInt("settings.spawners-repel-radius");
                        for (int fromX = -(radius + 1); fromX <= (radius + 1); fromX++) {
                            for (int fromY = -(radius + 1); fromY <= (radius + 1); fromY++) {
                                for (int fromZ = -(radius + 1); fromZ <= (radius + 1); fromZ++) {
                                    Block b = world.getBlockAt(blockX + fromX, blockY + fromY, blockZ + fromZ);
                                    if (b.getType().equals(Material.MOB_SPAWNER)) {
                                        e.setCancelled(true);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent e) {
        try {
            if (!e.isCancelled()) {
                if (Methods.isOffhand(e)) {
                    if (e.getPlayer().getInventory().getItemInOffHand().getType() == Material.MOB_SPAWNER)
                        e.setCancelled(true);
                } else {
                    Player p = e.getPlayer();
                    Block b = e.getBlockPlaced();
                    ItemStack inh = p.getItemInHand();
                    ItemMeta im = inh.getItemMeta();
                    if (e.getBlockPlaced() != null && e.getPlayer() != null && im != null) {
                        if (b.getType() == Material.MOB_SPAWNER && im.getDisplayName() != null) {

                            if (plugin.getConfig().getBoolean("settings.spawners-repel-liquid")) {
                                Block block = e.getBlock();
                                int bx = block.getX();
                                int by = block.getY();
                                int bz = block.getZ();
                                int radius = plugin.getConfig().getInt("settings.spawners-repel-radius");
                                for (int fx = -radius; fx <= radius; fx++) {
                                    for (int fy = -radius; fy <= radius; fy++) {
                                        for (int fz = -radius; fz <= radius; fz++) {
                                            Block b2 = b.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                                            if (((b2.getType().equals(Material.STATIONARY_LAVA) || (b2.getType().equals(Material.LAVA))))
                                                    || ((b2.getType().equals(Material.STATIONARY_WATER) || (b2.getType().equals(Material.WATER))))) {
                                                b2.setType(Material.AIR);
                                            }
                                        }
                                    }
                                }
                            }

                            String type = plugin.getApi().getIType(inh);

                            if (p.hasPermission("epicspawners.place." + type) || p.hasPermission("epicspawners.place." + Methods.getTypeFromString(type)) || p.hasPermission("epicspawners.place.*")) {
                                if (plugin.getConfig().getInt("settings.Force-Combine-Radius") != 0) {
                                    if (plugin.dataFile.getConfig().contains("data.spawner")) {
                                        ConfigurationSection cs = plugin.dataFile.getConfig().getConfigurationSection("data.spawner");
                                        for (String key : cs.getKeys(false)) {
                                            if (Arconix.pl().serialize().unserializeLocation(key).getWorld() != null) {
                                                if (Arconix.pl().serialize().unserializeLocation(key).getWorld().equals(b.getLocation().getWorld())) {
                                                    if (Arconix.pl().serialize().unserializeLocation(key).distance(b.getLocation()) <= plugin.getConfig().getInt("settings.Force-Combine-Radius")) {
                                                        if (inh.getItemMeta().getDisplayName() != null) {
                                                            if (!plugin.getApi().isOmniBlock(Arconix.pl().serialize().unserializeLocation(key))) {
                                                                Spawner eSpawner = new Spawner(Arconix.pl().serialize().unserializeLocation(key).getBlock());
                                                                String name = "";
                                                                try {
                                                                    name = eSpawner.getSpawner().getSpawnedType().name();
                                                                } catch (Exception ex) {
                                                                }
                                                                if (name.equals(type)) {
                                                                    if (plugin.getConfig().getBoolean("settings.Force-Combine-Deny")) {
                                                                        p.sendMessage(Lang.FORCE_DENY.getConfigValue());
                                                                        e.setCancelled(true);
                                                                    } else {
                                                                        if (eSpawner.processCombine(p, inh, null)) {
                                                                            p.sendMessage(Lang.Merge_Distance.getConfigValue());
                                                                            e.setCancelled(true);
                                                                        } else {
                                                                            e.setCancelled(true);
                                                                        }
                                                                    }
                                                                    return;

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spawner eSpawner = new Spawner(b);
                                boolean isCustom = false;
                                if (!type.equals("OMNI")) {
                                    try {
                                        eSpawner.setSpawner(EntityType.valueOf(type));
                                    } catch (Exception ex) {
                                        isCustom = true;
                                        eSpawner.setSpawner(EntityType.valueOf("DROPPED_ITEM"));
                                    }
                                }
                                int multi = 0;
                                if (im.hasDisplayName()) {
                                    if (plugin.blacklist.isBlacklisted(p, true)) {
                                        e.setCancelled(true);
                                    } else {
                                        multi = plugin.getApi().getIMulti(inh);
                                        eSpawner.updateDelay();
                                        plugin.dataFile.getConfig().set("data.spawner." + Arconix.pl().serialize().serializeLocation(b), multi);
                                        if (!type.equals("OMNI") && isCustom) {
                                            plugin.dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b) + ".type", type);
                                        }
                                        plugin.dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b) + ".player", p.getUniqueId().toString());
                                        plugin.getApi().saveCustomSpawner(inh, b);
                                    }
                                }
                                if (plugin.getConfig().getBoolean("settings.Alert-place-break")) {
                                    if (!plugin.blacklist.isBlacklisted(p, false) || !im.hasDisplayName()) {
                                        p.sendMessage(Lang.PLACE.getConfigValue(Methods.compileName(type, multi, true)));
                                    }
                                }
                                plugin.getApi().updateDisplayItem(type, b.getLocation());
                                if (multi <= 1) {
                                    plugin.dataFile.getConfig().set("data.spawner." + Arconix.pl().serialize().serializeLocation(b), 1);
                                    if (!type.equals("OMNI") && isCustom) {
                                        plugin.dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b) + ".type", type);
                                    }
                                    plugin.dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b) + ".player", p.getUniqueId().toString());

                                    if (!p.isOp() && plugin.spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(type) + ".Pickup-cost") != 0) {
                                        if (eSpawner.canCharge()) {
                                            int cost = plugin.spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(type) + ".Pickup-cost");
                                            p.sendMessage(Lang.PLACE_COST_WARN.getConfigValue(Arconix.pl().format().formatEconomy(cost)));
                                            plugin.freePickup.put(p, b.getLocation());
                                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.freePickup.remove(p), 1200L);
                                        }
                                    }
                                }
                            } else {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
                plugin.holo.processChange(e.getBlock());
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) //Leave this on high or WorldGuard will not work...
    public void onBlockBreak(BlockBreakEvent e) {
        try {
            if (!e.isCancelled()) {
                Block b = e.getBlock();
                if (b.getType() == Material.MOB_SPAWNER) {
                    Spawner eSpawner = new Spawner(b);
                    Player p = e.getPlayer();
                    e.setExpToDrop(0);

                    if (eSpawner.canBreak()) {

                        String type = Methods.getType(eSpawner.getSpawner().getSpawnedType());

                        if (plugin.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(eSpawner.getSpawner().getBlock()) + ".type")) {
                            if (!plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(eSpawner.getSpawner().getBlock()) + ".type").equals("OMNI"))
                                type = plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(eSpawner.getSpawner().getBlock()) + ".type");
                        }

                        int multi = eSpawner.getMulti();
                        int omulti = multi;

                        int newMulti = multi - 1;
                        if (plugin.getApi().isOmniBlock(b.getLocation())) {
                            type = "Omni";
                            if (omulti > 2) {
                                multi = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b) + ".entities")).size();
                            } else {
                                String old = plugin.dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b) + ".entities").get(0);
                                int n = Integer.parseInt(old.split("-")[1]);
                                newMulti = n;
                            }
                        }

                        if (p.isSneaking() && plugin.getConfig().getBoolean("settings.Only-drop-stacked") ||
                                p.isSneaking() && plugin.getConfig().getBoolean("settings.Sneak-for-stack")) {
                            if (!plugin.getApi().isOmniBlock(b.getLocation()))
                                newMulti = 0;
                        }
                        if (newMulti > 0) {

                            e.setCancelled(true);
                        }
                        if (p.getItemInHand().getItemMeta() == null) {
                            eSpawner.downgradeFinal(p, newMulti, omulti, type);
                        } else {
                            if (!p.getItemInHand().getItemMeta().hasEnchant(Enchantment.SILK_TOUCH) && !p.hasPermission("epicspawners.no-silk-drop")) {
                                eSpawner.downgradeFinal(p, newMulti, omulti, type);
                            } else {
                                if (b != null && plugin.getConfig().getBoolean("settings.Silktouch-spawners")) {

                                    boolean bought = true;
                                    if (!p.isOp() && !plugin.freePickup.containsValue(b.getLocation())) {
                                        if (plugin.spawnerFile.getConfig().getInt("Entities." + type + ".Pickup-cost") != 0) {
                                            int cost = plugin.spawnerFile.getConfig().getInt("Entities." + type + ".Pickup-cost");
                                            if (newMulti == 0) {
                                                cost = cost * multi;
                                            }
                                            if (!plugin.pickup.containsKey(p)) {
                                                p.sendMessage(Lang.BREAK_COST_WARN.getConfigValue(Arconix.pl().format().formatEconomy(cost)));
                                                plugin.pickup.put(p, true);
                                                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.pickup.remove(p), 300L);
                                                e.setCancelled(true);
                                                bought = false;
                                            } else {
                                                if (eSpawner.canCharge()) {
                                                    if (plugin.spawnerFile.getConfig().getInt("Entities." + type + ".Pickup-cost") != 0 && plugin.pickup.containsKey(p)) {
                                                        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
                                                            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                                                            net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                                                            if (econ.has(p, cost)) {
                                                                econ.withdrawPlayer(p, cost);
                                                            } else {
                                                                p.sendMessage(Lang.BREAK_COST_CANTAFFORD.getConfigValue());
                                                                bought = false;
                                                            }
                                                        } else {
                                                            bought = false;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (!bought) {
                                        e.setCancelled(true);
                                    } else {
                                        plugin.pickup.remove(p);

                                        int ch;
                                        if (plugin.dataFile.getConfig().contains("data.spawner." + Arconix.pl().serialize().serializeLocation(b))) {
                                            ch = Integer.parseInt(plugin.getConfig().getString("settings.Silktouch-placed-drop-chance").replace("%", ""));
                                        } else {
                                            ch = Integer.parseInt(plugin.getConfig().getString("settings.Silktouch-natural-drop-chance").replace("%", ""));
                                        }

                                        double rand = Math.random() * 100;

                                        if (!p.isSneaking() && !plugin.getConfig().getBoolean("settings.Only-drop-stacked") || p.isSneaking() && !plugin.getConfig().getBoolean("settings.Sneak-for-stack")) {
                                            multi = 1;
                                        }
                                        if (rand - ch < 0 || ch == 100) {
                                            if (p.hasPermission("epicspawners.silkdrop." + type) || p.hasPermission("epicspawners.silkdrop.*")) {
                                                new EPlayer(p).dropSpawner(b.getLocation(), multi, type);
                                            }
                                        }
                                        eSpawner.downgradeFinal(p, newMulti, omulti, type);

                                    }
                                }
                            }
                        }
                    }
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        plugin.holo.processChange(e.getBlock());
                        }, 10L);
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

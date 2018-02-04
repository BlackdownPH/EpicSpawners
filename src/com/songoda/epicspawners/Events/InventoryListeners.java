package com.songoda.epicspawners.Events;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Lang;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Spawners.SpawnerItem;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class InventoryListeners implements Listener {

    EpicSpawners plugin = EpicSpawners.pl();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        try {
            if (e.getClickedInventory() != null) {
                Inventory inv = e.getInventory();
                Player p = (Player) e.getWhoClicked();
                if (plugin.getConfig().getBoolean("settings.Inventory-Stacking")) {
                    if (p.getOpenInventory().getType().equals(InventoryType.CRAFTING)) {
                        if ((inv != null) && (e.getCursor() != null) && (e.getCurrentItem() != null)) {
                            ItemStack c = e.getCursor();
                            ItemStack item = e.getCurrentItem();
                            if (e.isRightClick()) {
                                if (item.getType() == Material.MOB_SPAWNER) {
                                    if (c.getType() == Material.MOB_SPAWNER) {
                                        if (item.getAmount() == 1 && c.getAmount() == 1) {
                                            if (item.getAmount() == 1) {
                                                if (c.getAmount() - item.getAmount() == 1 || c.getAmount() == 1) {
                                                    Spawner eSpawner = new Spawner();
                                                    if (eSpawner.processCombine(p, item, c)) {
                                                        e.setCurrentItem(new ItemStack(Material.AIR));
                                                    }
                                                }
                                            }
                                        } else {
                                            p.sendMessage(Lang.ONLY_ONE.getConfigValue());
                                        }
                                    } else if (c.getType() == Material.AIR) {
                                        if (item.getAmount() == 1) {
                                            if (plugin.getApi().getType(item).equals("OMNI")) {
                                                List<ItemStack> items = plugin.getApi().removeOmni(item);
                                                e.setCurrentItem(items.get(0));
                                                e.setCursor(items.get(1));
                                            } else {
                                                List<ItemStack> items = plugin.getApi().removeSpawner(item);
                                                if (items.size() == 2) {
                                                    e.setCurrentItem(items.get(1));
                                                    e.setCursor(items.get(0));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (plugin.inShow.containsKey(e.getWhoClicked())) {
                    e.setCancelled(true);
                    int amt = e.getInventory().getItem(22).getAmount();
                    if (e.getSlot() == 0) {
                        int page = plugin.page.get(p);
                        plugin.shop.open(p, page);
                    } else if (e.getSlot() == 8) {
                        p.closeInventory();
                    } else if (e.getSlot() == 19) {
                        if (amt != 1)
                            amt = 1;
                        plugin.shop.show(plugin.inShow.get(p), amt, p);
                    } else if (e.getSlot() == 29) {
                        if ((amt - 10) <= 64 && (amt - 10) >= 1)
                            amt = amt - 10;
                        plugin.shop.show(plugin.inShow.get(p), amt, p);
                    } else if (e.getSlot() == 11) {
                        if ((amt - 1) <= 64 && (amt - 1) >= 1)
                            amt = amt - 1;
                        plugin.shop.show(plugin.inShow.get(p), amt, p);
                    } else if (e.getSlot() == 15) {
                        if ((amt + 1) <= 64 && (amt + 1) >= 1)
                            amt = amt + 1;
                        plugin.shop.show(plugin.inShow.get(p), amt, p);
                    } else if (e.getSlot() == 33) {
                        if ((amt + 10) <= 64 && (amt + 10) >= 1)
                            amt = amt + 10;
                        plugin.shop.show(plugin.inShow.get(p), amt, p);
                    } else if (e.getSlot() == 25) {
                        if (amt != 64)
                            amt = 64;
                        plugin.shop.show(plugin.inShow.get(p), amt, p);
                    } else if (e.getSlot() == 40) {
                        plugin.shop.confirm(p, amt);
                        p.closeInventory();
                    }
                } else if (plugin.boosting.contains(p)) {
                    e.setCancelled(true);
                    Spawner eSpawner = new Spawner(plugin.lastSpawner.get(p));
                    if (e.getSlot() == 8) {
                        plugin.boostAmt.put(p, plugin.boostAmt.get(p) + 1);
                        eSpawner.playerBoost(p);
                    } else if (e.getSlot() == 0) {
                        plugin.boostAmt.put(p, plugin.boostAmt.get(p) - 1);
                        eSpawner.playerBoost(p);
                    } else if (e.getSlot() == 10) {
                        eSpawner.purchaseBoost(p, 5);
                    } else if (e.getSlot() == 12) {
                        eSpawner.purchaseBoost(p, 15);
                    } else if (e.getSlot() == 14) {
                        eSpawner.purchaseBoost(p, 30);
                    } else if (e.getSlot() == 16) {
                        eSpawner.purchaseBoost(p, 60);
                    }
                } else if (e.getInventory().getTitle().equals(Lang.SPAWNER_CONVERT.getConfigValue())) {
                    e.setCancelled(true);
                    ItemStack clicked = e.getCurrentItem();
                    Spawner eSpawner = new Spawner(plugin.spawnerLoc.get(p));

                    int page = plugin.page.get(p);

                    if (e.getClickedInventory().getType() == InventoryType.CHEST) {
                        if (e.getSlot() == 8) {
                            p.closeInventory();
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.BACK.getConfigValue())) {
                            if (page != 1) {
                                eSpawner.change(p, page - 1);
                            }
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.NEXT.getConfigValue())) {
                            eSpawner.change(p, page + 1);
                        } else if (clicked.getType() == Material.SKULL_ITEM || clicked.getType() == Material.MOB_SPAWNER) {
                            eSpawner.convert(plugin.getApi().getIType(clicked), p);
                        }
                    }
                } else if (plugin.spawnerLoc.containsKey(e.getWhoClicked())) {
                    e.setCancelled(true);
                    Spawner eSpawner = new Spawner(plugin.spawnerLoc.get(p));

                    if (plugin.spawnerFile.getConfig().getBoolean("Entities." + eSpawner.spawnedType + ".Upgradable")) {
                        if (e.getSlot() == 11) {
                            if (plugin.getConfig().getBoolean("settings.Upgrade-with-xp")) {
                                if (e.getCurrentItem().getItemMeta().getDisplayName() != "§l") {
                                    eSpawner.upgrade(p, "XP");
                                }
                                p.closeInventory();
                            }
                        } else if (e.getSlot() == 8) {
                            int page = 2;
                            if (plugin.infPage.containsKey(p))
                                page = plugin.infPage.get(p) + 1;
                            plugin.infPage.put(p, page);
                            eSpawner.view(p, page);
                        } else if (e.getSlot() == 13) {
                            if (e.getClick().isRightClick()) {
                                if (eSpawner.getBoost() == 0) {
                                    eSpawner.playerBoost(p);
                                }
                            } else if (e.getClick().isLeftClick()) {
                                boolean omni = false;
                                if (plugin.dataFile.getConfig().contains("data.spawnerstats." + eSpawner.locationStr + ".type")) {
                                    if (plugin.dataFile.getConfig().getString("data.spawnerstats." + eSpawner.locationStr + ".type").equals("OMNI")) {
                                        List<SpawnerItem> list = plugin.getApi().convertFromList(plugin.dataFile.getConfig().getStringList("data.spawnerstats." + eSpawner.locationStr + ".entities"));
                                        omni = true;
                                    }
                                }
                                if (p.hasPermission("epicspawners.convert") && !omni) {
                                    eSpawner.change(p, 1);
                                }
                            }
                        } else if (e.getSlot() == 15) {
                            if (plugin.getConfig().getBoolean("settings.Upgrade-with-eco")) {
                                if (e.getCurrentItem().getItemMeta().getDisplayName() != "§l") {
                                    eSpawner.upgrade(p, "ECO");
                                    p.closeInventory();
                                }
                            }
                        }
                    }
                } else if (plugin.editing.containsKey(p)) {
                    if (!plugin.subediting.containsKey(p)) {
                        e.setCancelled(true);
                        if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.BACK.getConfigValue()))
                            plugin.editor.open(p, plugin.page.get(p));
                        else if (e.getSlot() == 11) {
                            if (!e.getClick().isLeftClick() && !e.getClick().isRightClick()) {
                                String name = plugin.editor.getType(plugin.editing.get(p));
                                plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(name) + ".Display-Item", p.getItemInHand().getType().toString());
                                p.sendMessage(Arconix.pl().format().formatText(plugin.references.getPrefix() + "&7Display Item for &6" + name + " &7set to &6" + p.getItemInHand().getType().toString() + "&7."));
                                plugin.editor.overview(p, plugin.editing.get(p));
                            } else if (e.getClick().isLeftClick()) {
                                plugin.editor.editSpawnerName(p);
                            }
                        } else if (e.getSlot() == 25)
                            plugin.editor.editor(p, "Entity");
                        else if (e.getSlot() == 28) {

                            boolean right = e.isRightClick();
                            for (final EntityType val : EntityType.values()) {
                                if (val.isSpawnable() && val.isAlive()) {
                                    if (val.name().equals(plugin.editor.getType(plugin.editing.get(p)))) {
                                        right = false;
                                    }
                                }
                            }
                            if (!right) {
                                if (plugin.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Allowed"))
                                    plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Allowed", false);
                                else
                                    plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Allowed", true);
                                plugin.editor.overview(p, plugin.editing.get(p));
                            } else {
                                plugin.editor.destroy(p);
                            }
                        } else if (e.getSlot() == 23) {
                            plugin.editor.basicSettings(p);
                        } else if (e.getSlot() == 30) {
                            plugin.editor.save(p);
                            plugin.editor.overview(p, plugin.editing.get(p));
                        } else if (e.getSlot() == 41)
                            plugin.editor.editor(p, "Item");
                        else if (e.getSlot() == 43)
                            plugin.editor.editor(p, "Command");
                    } else {
                        if (plugin.subediting.get(p).equals("basic")) {
                            if (e.getClickedInventory().equals(p.getOpenInventory().getTopInventory())) {
                                if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.BACK.getConfigValue()))
                                    plugin.editor.overview(p, plugin.editing.get(p));
                                else if (e.getSlot() == 13) {
                                    if (plugin.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Upgradable"))
                                        plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Upgradable", false);
                                    else
                                        plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Upgradable", true);
                                    plugin.editor.basicSettings(p);
                                } else if (e.getSlot() == 19) {
                                    plugin.editor.alterSetting(p, "Shop-Price");
                                } else if (e.getSlot() == 20) {
                                    if (plugin.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".In-Shop"))
                                        plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".In-Shop", false);
                                    else
                                        plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".In-Shop", true);
                                    plugin.editor.basicSettings(p);
                                } else if (e.getSlot() == 22) {
                                    if (plugin.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Spawn-On-Fire"))
                                        plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Spawn-On-Fire", false);
                                    else
                                        plugin.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(plugin.editor.getType(plugin.editing.get(p))) + ".Spawn-On-Fire", true);
                                    plugin.editor.basicSettings(p);
                                } else if (e.getSlot() == 24) {
                                    plugin.editor.alterSetting(p, "Custom-ECO-Cost");
                                } else if (e.getSlot() == 25) {
                                    plugin.editor.alterSetting(p, "Custom-XP-Cost");
                                } else if (e.getSlot() == 30) {
                                    plugin.editor.alterSetting(p, "CustomGoal");
                                } else if (e.getSlot() == 32) {
                                    plugin.editor.alterSetting(p, "Pickup-cost");
                                }
                                e.setCancelled(true);
                            }
                        } else {
                            if (e.getClickedInventory().equals(p.getOpenInventory().getTopInventory())) {
                                if ((e.getSlot() < 10 || e.getSlot() > 25) || e.getSlot() == 17 || e.getSlot() == 18) {
                                    e.setCancelled(true);
                                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.BACK.getConfigValue()))
                                        plugin.editor.overview(p, plugin.editing.get(p));
                                    else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().format().formatText("&6Add Command")))
                                        plugin.editor.createCommand(p);
                                    else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().format().formatText("&6Add Entity")))
                                        plugin.editor.addEntityInit(p);
                                    else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().format().formatText("&aSave")))
                                        plugin.editor.saveInstance(p, plugin.editor.getItems(p));
                                    else if (e.getSlot() == 40)
                                        plugin.editor.editChatInit(p);
                                    else if (e.getSlot() == 49)
                                        plugin.editor.editSpawnLimit(p);
                                }
                            }
                        }
                    }
                } else if (e.getInventory().getTitle().equals("Spawner Editor")) {
                    e.setCancelled(true);
                    int page = plugin.page.get(p);
                    if (e.getSlot() == 8) {
                        p.closeInventory();
                    } else if ((e.getCurrentItem().getType().equals(Material.SKULL_ITEM) || e.getCurrentItem().getType().equals(Material.MOB_SPAWNER)) &&
                            !e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.NEXT.getConfigValue()) &&
                            !e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.BACK.getConfigValue())) {
                            String idd = e.getCurrentItem().getItemMeta().getLore().get(1);
                            idd = idd.replace("§", "");
                            int id = Integer.parseInt(idd);
                            //if (e.getClick().isLeftClick())
                            plugin.editor.overview(p, id);
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Arconix.pl().format().formatText("&9&lNew Spawner"))) {
                        plugin.editor.overview(p, 0);
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.BACK.getConfigValue())) {
                        if (page != 1) {
                            plugin.editor.open(p, page - 1);
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.NEXT.getConfigValue())) {
                        plugin.editor.open(p, page + 1);
                    }
                } else if (e.getInventory().getTitle().equals(Lang.SSTATS_TITLE.getConfigValue())) {
                    e.setCancelled(true);
                    if (e.getSlot() == 8) {
                        p.closeInventory();
                    }
                } else if (e.getInventory().getTitle().equals(Lang.SPAWNER_SHOP.getConfigValue())) {
                    e.setCancelled(true);
                    ItemStack clicked = e.getCurrentItem();

                    int page = plugin.page.get(p);

                    if (e.getClickedInventory().getType() == InventoryType.CHEST) {
                        if (e.getSlot() == 8) {
                            p.closeInventory();
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.BACK.getConfigValue())) {
                            if (page != 1) {
                                plugin.shop.open(p, page - 1);
                            }
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals(Lang.NEXT.getConfigValue())) {
                            plugin.shop.open(p, page + 1);
                        } else if (clicked.getType() == Material.SKULL_ITEM || clicked.getType() == Material.MOB_SPAWNER) {
                            plugin.shop.show(plugin.getApi().getIType(clicked), 1, p);
                        }
                    }
                }
                if (e.getSlot() != 64537) {
                    if (e.getInventory().getType() == InventoryType.ANVIL) {
                        if (e.getAction() != InventoryAction.NOTHING) {
                            if (e.getCurrentItem().getType() != Material.AIR) {
                                ItemStack item = e.getCurrentItem();
                                if (item.getType() == Material.MOB_SPAWNER) {
                                    e.setCancelled(true);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        try {
            final Player p = (Player) event.getPlayer();
            if (plugin.boosting.contains(p) || plugin.inShow.containsKey(p) || plugin.spawnerLoc.containsKey(p)) {
                if (plugin.boosting.contains(p))
                    plugin.boosting.remove(p);
                if (plugin.inShow.containsKey(p))
                    plugin.inShow.remove(p);
                if (plugin.spawnerLoc.containsKey(p))
                    plugin.spawnerLoc.remove(p);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!p.getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST))
                        p.closeInventory();
                }, 1L);
            }
            if (!p.getOpenInventory().getTopInventory().getTitle().contains("Editing"))
                plugin.editing.remove(p);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

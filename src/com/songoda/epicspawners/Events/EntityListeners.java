package com.songoda.epicspawners.Events;

import com.songoda.arconix.Arconix;
import com.songoda.epicspawners.Entity.EPlayer;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class EntityListeners implements Listener {

    EpicSpawners plugin = EpicSpawners.pl();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlow(EntityExplodeEvent e) {
        try {
            if (!e.isCancelled()) {
                List<Block> destroyed = e.blockList();
                Iterator<Block> it = destroyed.iterator();
                while (it.hasNext()) {
                    Block b = it.next();
                    if (b.getType() == Material.MOB_SPAWNER) {
                        if (plugin.getConfig().getBoolean("settings.Spawners-dont-explode"))
                            e.blockList().remove(b);
                        else if (e.getEntity() instanceof Creeper && plugin.getConfig().getBoolean("settings.Drop-on-creeper-explosion") || e.getEntity() instanceof TNTPrimed && plugin.getConfig().getBoolean("settings.Drop-on-tnt-explosion")) {
                            int multi = 0;

                            String locationStr = Arconix.pl().serialize().serializeLocation(b);
                            if (plugin.dataFile.getConfig().getInt("data.spawner." + locationStr) != 0) {
                                multi = plugin.dataFile.getConfig().getInt("data.spawner." + locationStr);
                                plugin.dataFile.getConfig().set("data.spawner." + locationStr, null);
                            }

                            Location spawnLocation = b.getLocation();
                            CreatureSpawner spawner = (CreatureSpawner) b.getState();
                            String type = spawner.getSpawnedType().name();
                            String chance = "";
                            if (e.getEntity() instanceof Creeper && plugin.getConfig().getBoolean("settings.Drop-on-creeper-explosion"))
                                chance = plugin.getConfig().getString("settings.Tnt-explosion-drop-chance");
                            else if (e.getEntity() instanceof TNTPrimed && plugin.getConfig().getBoolean("settings.Drop-on-tnt-explosion"))
                                chance = plugin.getConfig().getString("settings.Creeper-explosion-drop-chance");
                            int ch = Integer.parseInt(chance.replace("%", ""));
                            double rand = Math.random() * 100;
                            if (rand - ch < 0 || ch == 100) {
                                if (plugin.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b.getLocation()) + ".type")) {
                                    if (plugin.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().serialize().serializeLocation(b.getLocation()) + ".type").equals("OMNI")) {
                                        type = "Omni";
                                        multi = 100;
                                    }
                                }
                                new EPlayer(null).dropSpawner(spawnLocation, multi, type);
                                plugin.holo.processChange(b);
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
    public void onDeath(EntityDeathEvent e) {
        try {
            if (e.getEntity().getKiller() != null) {
                if (e.getEntity().getKiller() instanceof Player) {
                    Player p = e.getEntity().getKiller();
                    if (!plugin.dataFile.getConfig().getBoolean("data.Entities." + e.getEntity().getUniqueId()) || plugin.getConfig().getBoolean("settings.Count-unnatural-kills")) {
                        new EPlayer(p).plus(e.getEntity(), 1);
                    }
                }
            }
            plugin.dataFile.getConfig().set("data.Entities." + e.getEntity().getUniqueId(), null);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void onDeath(CreatureSpawnEvent e) {
        try {
            if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL &&
                    e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CHUNK_GEN) {
                plugin.dataFile.getConfig().set("data.Entities." + e.getEntity().getUniqueId(), true);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

package com.songoda.epicspawners.Spawners;

import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.entity.EntityType;

/**
 * Created by songo on 5/21/2017.
 */
public class SpawnerItem {

    private String type = "PIG";
    private int multi = 1;

    public SpawnerItem(String type, int multi) {
        this.type = type;
        this.multi = multi;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMulti() {
        return multi;
    }

    public void setMulti(int multi) {
        this.multi = multi;
    }
}
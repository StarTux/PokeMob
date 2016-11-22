package com.winthier.pokemob;

import com.winthier.pokemob.listener.BrewerListener;
import com.winthier.pokemob.listener.CommandListener;
import com.winthier.pokemob.listener.EntityListener;
import com.winthier.pokemob.listener.PotionListener;
import com.winthier.pokemob.listener.SpawnEggListener;
import java.util.List;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PokeMobPlugin extends JavaPlugin {
    private Configuration configuration;
    final CommandListener command = new CommandListener(this);
    final EntityListener entityListener = new EntityListener(this);
    @Getter static PokeMobPlugin instance;

    @Override
    public void onEnable() {
        this.instance = this;
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(new PotionListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnEggListener(this), this);
        getServer().getPluginManager().registerEvents(entityListener , this);
        getServer().getPluginManager().registerEvents(new BrewerListener(this), this);
        getCommand("pokemob").setExecutor(command);
    }

    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration(this);
            configuration.load();
        }
        return configuration;
    }

    public void reload() {
        configuration = null;
        reloadConfig();
    }

    Entity summon(EntityType et, Location loc, String json) {
        entityListener.setExpectedEntity(et);
        String source;
        if (!loc.getWorld().getPlayers().isEmpty()) {
            source = loc.getWorld().getPlayers().get(0).getName();
        } else {
            List<Entity> list = loc.getWorld().getEntities();
            if (list.isEmpty()) return null;
            source = list.get(0).getUniqueId().toString();
        }
        Msg.consoleCommand("minecraft:execute %s ~ ~ ~ minecraft:summon minecraft:%s %.2f %.2f %.2f %s",
                       source, et.getName(),
                       loc.getX(), loc.getY(), loc.getZ(),
                       json);
        entityListener.setExpectedEntity(null);
        Entity result = entityListener.getSpawnedEntity();
        entityListener.setSpawnedEntity(null);
        return result;
    }
}

package com.winthier.pokemob;

import com.winthier.pokemob.listener.CommandListener;
import com.winthier.pokemob.listener.EntityListener;
import com.winthier.pokemob.listener.PotionListener;
import com.winthier.pokemob.listener.SpawnEggListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class PokeMobPlugin extends JavaPlugin {
    private Configuration configuration;
    public final CommandListener command = new CommandListener(this);
    @Getter static PokeMobPlugin instance;

    @Override
    public void onEnable() {
        this.instance = this;
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(new PotionListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnEggListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);
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
}

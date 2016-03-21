package com.winthier.pokemob;

import com.winthier.pokemob.listener.CommandListener;
import com.winthier.pokemob.listener.EntityListener;
import com.winthier.pokemob.listener.PotionListener;
import com.winthier.pokemob.listener.SpawnEggListener;
import org.bukkit.plugin.java.JavaPlugin;

public class PokeMobPlugin extends JavaPlugin {
        public final Configuration config = new Configuration(this);
        public final CommandListener command = new CommandListener(this);
        public static PokeMobPlugin instance;

        @Override
        public void onEnable() {
                this.instance = this;
                // new PotionListener(this).onEnable();
                new SpawnEggListener(this).onEnable();
                new EntityListener(this).onEnable();
                config.onEnable();
                command.onEnable();
        }

        @Override
        public void onDisable() {
        }
}

package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.ExpUtil;
import com.mengcraft.playersql.lib.ExpUtilHandler;
import com.mengcraft.playersql.lib.ItemUtil;
import com.mengcraft.playersql.lib.ItemUtilHandler;
import com.mengcraft.playersql.lib.Metrics;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Created on 16-1-2.
 */
public class PluginMain extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        ItemUtil itemUtil = new ItemUtilHandler(this).handle();
        ExpUtil expUtil = new ExpUtilHandler(this).handle();

        EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
        if (db.isNotInitialized()) {
            db.define(User.class);

            db.setMaxSize(getConfig().getInt("plugin.max-db-connection"));
            try {
                db.initialize();
            } catch (Exception e) {
                throw new PluginException("Failed connect to database");
            }
        }

        db.install();
//        db.reflect();

        UserManager manager = UserManager.INSTANCE;
        manager.setMain(this);
        manager.setItemUtil(itemUtil);
        manager.setExpUtil(expUtil);
        manager.setDb(db);

        EventExecutor executor = new EventExecutor();
        executor.setMain(this);
        executor.setManager(manager);

        getServer().getPluginManager().registerEvents(executor, this);
        try {
            getServer().getPluginManager().registerEvents(new ExtendEventExecutor(manager), this);
        } catch (Exception ignore) {
        }// There is some event since 1.8.

        Metrics.start(this);
    }

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            UserManager.INSTANCE.saveUser(p, false);
        }
    }

    public Player getPlayer(UUID uuid) {
        return getServer().getPlayer(uuid);
    }

    public void log(Exception e) {
        getLogger().log(Level.SEVERE, e.toString(), e);
    }

    public void log(String info) {
        getLogger().info(info);
    }

    public BukkitTask runTimerAsync(Runnable r, int i) {
        return getServer().getScheduler().runTaskTimerAsynchronously(this, r, i, i);
    }

    public void runAsync(Runnable r) {
        CompletableFuture.runAsync(r);
    }

    public void run(Runnable r) {
        getServer().getScheduler().runTask(this, r);
    }

    public BukkitTask runTimer(Runnable r, int i) {
        return getServer().getScheduler().runTaskTimer(this, r, i, i);
    }

    public static boolean nil(Object i) {
        return i == null;
    }

}

package me.flukky;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import me.flukky.Commands.BossCommand;
import me.flukky.Listeners.BossListener;

import java.util.HashMap;

public class Main extends JavaPlugin {
    private HashMap<String, LivingEntity> worldBosses = new HashMap<>();
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new BossListener(this), this);

        getCommand("boss").setExecutor(new BossCommand(this)); // ส่ง this
    }

    @Override
    public void onDisable() {
        // ลบบอสทั้งหมดเมื่อปิดปลั๊กอิน
        for (String worldName : worldBosses.keySet()) {
            LivingEntity boss = worldBosses.get(worldName);
            if (boss != null && !boss.isDead()) {
                boss.remove(); // ลบบอส
            }
        }
        worldBosses.clear(); // เคลียร์ HashMap
        console.sendMessage(ChatColor.RED + "ลบบอสทั้งหมดทุก World แล้ว");
    }


    public HashMap<String, LivingEntity> getWorldBosses() {
        return worldBosses;
    }
}

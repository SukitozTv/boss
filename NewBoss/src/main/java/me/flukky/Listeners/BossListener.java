package me.flukky.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.flukky.Main;

public class BossListener implements Listener {

    private final Main plugin; // ตัวแปรเพื่อเข้าถึง Main

    public BossListener(Main plugin) {
        this.plugin = plugin; // กำหนดค่าตัวแปร plugin
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        // ตรวจสอบว่ามีบอสในโลกนี้หรือไม่
        LivingEntity entity = event.getEntity();
        String worldName = entity.getWorld().getName();

        // ตรวจสอบว่าบอสที่ตายเป็น LivingEntity หรือไม่
        if (plugin.getWorldBosses().containsKey(worldName)) {
            LivingEntity boss = plugin.getWorldBosses().get(worldName);

            // เปรียบเทียบ UUID ของบอสกับ UUID ของ entity ที่ตาย
            if (boss.getUniqueId().equals(entity.getUniqueId())) {
                // ลบออกจาก worldBosses
                plugin.getWorldBosses().remove(worldName);

                // แจ้งผู้เล่นในโลกว่าบอสถูกฆ่าแล้ว
                entity.getWorld().getPlayers().forEach(player -> player.sendMessage(ChatColor.GREEN
                        + "The boss in world '" + worldName + "' has been defeated!"));
            }
        }
    }

    @EventHandler
    public void onBossExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        String worldName = entity.getWorld().getName();

        if (plugin.getWorldBosses().containsKey(worldName)) {
            LivingEntity boss = plugin.getWorldBosses().get(worldName);

            // ตรวจสอบว่าบอสที่ระเบิดตัวเองหรือไม่
            if (boss.getUniqueId().equals(entity.getUniqueId())) {
                // ลบออกจาก worldBosses
                plugin.getWorldBosses().remove(worldName);

                // แจ้งผู้เล่นในโลกว่าบอสถูกฆ่าแล้ว
                entity.getWorld().getPlayers().forEach(player -> player.sendMessage(ChatColor.GREEN
                        + "The boss in world '" + worldName + "' has exploded and is defeated!"));
            }
        }
    }
}

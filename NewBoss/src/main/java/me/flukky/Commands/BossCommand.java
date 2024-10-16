package me.flukky.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.joml.Random;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import me.flukky.Main;

public class BossCommand implements CommandExecutor, TabCompleter {
    private final Main plugin; // เพิ่มตัวแปร plugin

    public BossCommand(Main plugin) {
        this.plugin = plugin; // กำหนดค่าตัวแปร plugin
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("boss")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /boss summon/check/remove <world>");
                return false;
            }

            String action = args[0];
            String worldName = args[1];
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                sender.sendMessage(ChatColor.RED + "World '" + worldName + "' does not exist.");
                return false;
            }

            switch (action.toLowerCase()) {
                case "summon":
                    if (plugin.getWorldBosses().containsKey(world.getName())) {
                        sender.sendMessage(ChatColor.RED + "There is already a boss in this world!");
                    } else {
                        spawnRandomBoss(world);
                        sender.sendMessage(ChatColor.GREEN + "A boss has been summoned in world '" + worldName + "'.");
                    }
                    break;

                case "check":
                    checkBossStatus(sender, world);
                    break;

                case "remove":
                    if (removeBoss(world)) {
                        sender.sendMessage(ChatColor.GREEN + "Boss removed from world '" + worldName + "'.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "No boss found in world '" + worldName + "'.");
                    }
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "Invalid action. Use: summon/check/remove");
                    break;
            }

            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("summon");
            suggestions.add("check");
            suggestions.add("remove");
        } else if (args.length == 2) {
            // ดึงชื่อโลกจาก config
            List<String> allowedWorlds = plugin.getConfig().getStringList("allowed-worlds");
            suggestions.addAll(allowedWorlds); // เพิ่มชื่อโลกที่อนุญาต
        }

        return suggestions;
    }

    public void spawnRandomBoss(World world) {
        List<String> allowedWorlds = plugin.getConfig().getStringList("allowed-worlds");
        // ตรวจสอบว่าโลกนี้อยู่ใน allowed-worlds หรือไม่
        if (!allowedWorlds.contains(world.getName())) {
            world.getPlayers().forEach(player -> player.sendMessage(ChatColor.RED + "Boss cannot be spawned in this world!"));
            return;
        }

        Random random = new Random();
        String bossKey = "bosses.boss" + (random.nextInt(plugin.getConfig().getConfigurationSection("bosses").getKeys(false).size()) + 1);
        String bossName = plugin.getConfig().getString(bossKey + ".name");
        double health = plugin.getConfig().getDouble(bossKey + ".health");
        double damage = plugin.getConfig().getDouble(bossKey + ".damage");
        EntityType type = EntityType.valueOf(plugin.getConfig().getString(bossKey + ".type"));

        // สุ่มตำแหน่งเกิด
        Location spawnLocation = getRandomLocation(world);

        // สร้างบอส
        LivingEntity boss = (LivingEntity) world.spawnEntity(spawnLocation, type);
        boss.setCustomName(bossName);
        boss.setCustomNameVisible(true);
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        boss.setHealth(health);
        boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);

        plugin.getWorldBosses().put(world.getName(), boss);

        // ประกาศในเกมพร้อมระยะห่างจากผู้เล่น
        world.getPlayers().forEach(player -> {
            double distance = player.getLocation().distance(spawnLocation); // คำนวณระยะห่าง
            player.sendMessage(ChatColor.RED + "A boss has spawned in world: " + world.getName() + " at " + 
                            "X: " + spawnLocation.getBlockX() + " Y: " + spawnLocation.getBlockY() + " Z: " + spawnLocation.getBlockZ());
            player.sendMessage(ChatColor.YELLOW + "The boss is " + ChatColor.GREEN + String.format("%.2f", distance) + ChatColor.YELLOW + " blocks away from you.");
        });

        plugin.getLogger().info("Spawned " + bossName + " in " + world.getName() + " at X: " + spawnLocation.getBlockX() +
                                " Y: " + spawnLocation.getBlockY() + " Z: " + spawnLocation.getBlockZ()); // ยังเก็บ log ใน console
    }

    private boolean removeBoss(World world) {
        if (plugin.getWorldBosses().containsKey(world.getName())) {
            LivingEntity boss = plugin.getWorldBosses().get(world.getName());
            boss.remove();
            plugin.getWorldBosses().remove(world.getName());
            return true;
        }
        return false;
    }

    private void checkBossStatus(CommandSender sender, World world) {
        if (plugin.getWorldBosses().containsKey(world.getName())) {
            LivingEntity boss = plugin.getWorldBosses().get(world.getName());
            sender.sendMessage(ChatColor.GREEN + "Boss in world '" + world.getName() + "': " + boss.getCustomName() +
                    " | Health: " + boss.getHealth() + "/" + boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        } else {
            sender.sendMessage(ChatColor.RED + "No boss found in world '" + world.getName() + "'.");
        }
    }

    public Location getRandomLocation(World world) {
        Random random = new Random();
        int x = random.nextInt(1000) - 500;
        int z = random.nextInt(1000) - 500;
        int y = world.getHighestBlockYAt(x, z); // หา y ที่สูงที่สุด
        return new Location(world, x, y, z);
    }
}

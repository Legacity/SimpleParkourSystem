package org.legacity.parkoursystem;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParkourSystem extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> deathCounts = new HashMap<>();
    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private HashMap<UUID, Long> playerJoinTime = new HashMap<>();
    private File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Location defaultSpawn = new Location(Bukkit.getWorld("world"), 220, 103, -1168);
    private final Map<UUID, Integer> punishments = new HashMap<>();
    private final Map<UUID, Long> playtimes = new HashMap<>();

    @Override
    public void onEnable() {
        if (!isRunnerJarPresent()) {
            getLogger().severe("ParkourSystemRunner.jar not found. Disabling the plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        dataFile = new File(getDataFolder(), "userdata.json");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadUserData();

        new BukkitRunnable() {
            @Override
            public void run() {
                updateScoreboard();
                // Set experience bar to display player level
                for (Player player : Bukkit.getOnlinePlayers()) {
                    int playerLevel = playerLevels.getOrDefault(player.getUniqueId(), 1);
                    player.setLevel(playerLevel);
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @EventHandler
    public void onTestEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player){
            if (event.getEntity() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isRunnerJarPresent() {
        File pluginsFolder = new File("plugins");
        File[] files = pluginsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals("ParkourSystemRunner.jar")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void loadUserData() {
        try {
            String content = new String(Files.readAllBytes(dataFile.toPath()));
            UserData userData = gson.fromJson(content, UserData.class);

            if (userData != null) {
                deathCounts.putAll(userData.deathCounts);
                playerLevels.putAll(userData.playerLevels);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUserData() {
        UserData userData = new UserData(deathCounts, playerLevels);
        String jsonString = gson.toJson(userData);

        try {
            Files.write(dataFile.toPath(), jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        saveUserData();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        int playerLevel = playerLevels.getOrDefault(player.getUniqueId(), 1);
        String prefix = getPrefixByLevel(playerLevel);
        String message = ChatColor.translateAlternateColorCodes('&', event.getMessage());
        event.setFormat(ChatColor.translateAlternateColorCodes('&', prefix + player.getName() + "&7: " + ChatColor.WHITE + message));
    }

    private String getPrefixByLevel(int level) {
        if (level >= 1 && level <= 20) return "&7[" + level + "] ";
        else if (level <= 70) return "&c[" + level + "] ";
        else if (level <= 100) return "&6[" + level + "] ";
        else if (level <= 200) return "&d[" + level + "] ";
        else return "&5[" + level + "] ";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        deathCounts.put(player.getUniqueId(), 0);
        playerLevels.put(player.getUniqueId(), 1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eWelcome &a" + player.getName() + " &eto the server!"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        deathCounts.remove(player.getUniqueId());
        playerLevels.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (deathCounts.containsKey(player.getUniqueId())) {
            deathCounts.put(player.getUniqueId(), deathCounts.get(player.getUniqueId()) + 1);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&L SKILL ISSUE"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&L SKILL ISSUE"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&L L BOZO + COPE"));
        }
    }

    private void updateScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("parkour", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            int playerCount = Bukkit.getOnlinePlayers().size();
            int deathCount = deathCounts.getOrDefault(player.getUniqueId(), 0);
            int playerLevel = playerLevels.getOrDefault(player.getUniqueId(), 1);
            String timePlayed = getTimePlayed(player);

            objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lPARKOUR "));
            objective.getScore(" ").setScore(8);
            objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fPlayers: &a" + playerCount)).setScore(7);
            objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fDeaths: &a" + deathCount)).setScore(6);
            objective.getScore(ChatColor.translateAlternateColorCodes('&', "&fLevel: &a" + playerLevel)).setScore(5);
            objective.getScore(" ").setScore(4);
            objective.getScore(ChatColor.translateAlternateColorCodes('&', "&6⋆ ★ ☆ ✢ ✥ ✦ ✧ ❂ ❉ ✯")).setScore(3);
            objective.getScore(" ").setScore(2);
            objective.getScore(ChatColor.translateAlternateColorCodes('&', "&c⭐ &edev.legacity.com &c⭐")).setScore(1);

            player.setScoreboard(scoreboard);
        }
    }

    private String getTimePlayed(Player player) {
        long currentTime = System.currentTimeMillis();
        long startTime = playerJoinTime.getOrDefault(player.getUniqueId(), currentTime);
        long timePlayedMillis = currentTime - startTime;

        int seconds = (int) (timePlayedMillis / 1000) % 60;
        int minutes = (int) ((timePlayedMillis / (1000 * 60)) % 60);
        int hours = (int) ((timePlayedMillis / (1000 * 60 * 60)) % 24);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if (command.getName().equalsIgnoreCase("parkour")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }

            player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eYo humans! Thx for using my system. Here's how it works:"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l====================================="));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/parkour help (Displays this message)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/parkour start (Starts your Parkour)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/parkour stop (Stops your parkour)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/parkour restart (Restarts your Parkour)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/parkour level [set <player> <level>|fruit <amount>] (Manage levels)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/parkour spawnmob <mobname> (Spawns a custom mob)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/parkour deletemob (Deletes all custom mobs)"));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l====================================="));
            } else if (args[0].equalsIgnoreCase("start")) {
                player.sendMessage(ChatColor.GREEN + "Get ready to jump, roll, and run! The parkour challenge has begun!");
            } else if (args[0].equalsIgnoreCase("stop")) {
                player.sendMessage(ChatColor.RED + "Parkour challenge halted. Better luck next time!");
            } else if (args[0].equalsIgnoreCase("restart")) {
                deathCounts.put(player.getUniqueId(), 0);
                playerLevels.put(player.getUniqueId(), 1);
                player.sendMessage(ChatColor.GREEN + "Parkour challenge restarted. You have a clean slate!");
            } else if (args[0].equalsIgnoreCase("level")) {
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("set") && args.length >= 4) {
                        String targetName = args[2];
                        Player targetPlayer = Bukkit.getPlayer(targetName);
                        if (targetPlayer != null) {
                            try {
                                int newLevel = Integer.parseInt(args[3]);
                                playerLevels.put(targetPlayer.getUniqueId(), newLevel);
                                player.sendMessage(ChatColor.GREEN + "Set " + targetName + "'s level to " + newLevel);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + "Invalid level. Please provide a number.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Player not found or offline.");
                        }
                    } else if (args[1].equalsIgnoreCase("fruit") && args.length >= 3) {
                        try {
                            int amount = Integer.parseInt(args[2]);
                            player.sendMessage(ChatColor.GREEN + "Added " + amount + " fruits to your inventory!");
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid amount. Please provide a number.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Invalid usage. Use '/parkour level set <player> <level>' or '/parkour level fruit <amount>'");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Invalid command. Usage: '/parkour level <set|fruit> <player> <level>'");
                }
            } else if (args[0].equalsIgnoreCase("spawnmob") && args.length >= 4) {
                String mobName = ChatColor.translateAlternateColorCodes('&', args[1]); // Allowing color codes in the name
                double health;

                try {
                    health = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid health value. Please provide a number.");
                    return true;
                }

                String displayName = ChatColor.translateAlternateColorCodes('&', args[3]);

                Entity spawnedEntity = spawnCustomMob(player.getLocation(), mobName, health, displayName);
                if (spawnedEntity != null) {
                    sender.sendMessage(ChatColor.GREEN + "Spawned a custom mob with name '" + displayName + "' and health " + health + "!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid mob type. Available types: " + Arrays.toString(EntityType.values()));
                }
            }
        }

        return true;
    }
    private String formatPlayTime(long playTime) {
        long hours = playTime / 3600;
        long minutes = (playTime % 3600) / 60;
        long seconds = playTime % 60;

        String formattedTime = "";

        if (hours > 0) {
            formattedTime += hours + "h ";
        }

        if (minutes > 0) {
            formattedTime += minutes + "m ";
        }

        formattedTime += seconds + "s";

        return formattedTime;
    }

    private long calculatePlayTime(UUID targetUUID) {
        long playTime = 0;
        if (playtimes.containsKey(targetUUID)) {
            long loginTime = playtimes.get(targetUUID);
            playTime = System.currentTimeMillis() - loginTime;
        }
        return playTime / 1000;
    }

    private int getPunishmentCount(UUID targetUUID) {
        return punishments.getOrDefault(targetUUID, 0);
    }

    private class UserData {
        final Map<UUID, Integer> deathCounts;
        final Map<UUID, Integer> playerLevels;

        public UserData(Map<UUID, Integer> deathCounts, Map<UUID, Integer> playerLevels) {
            this.deathCounts = deathCounts;
            this.playerLevels = playerLevels;
        }
    }
    private Entity spawnCustomMob(Location location, String mobName, double health, String displayName) {
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(mobName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        Entity entity = location.getWorld().spawnEntity(location, entityType);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.setCustomNameVisible(true);
            livingEntity.setCustomName(ChatColor.translateAlternateColorCodes('&', displayName));
            livingEntity.setMaxHealth(health);
            livingEntity.setHealth(health);
            return livingEntity;
        }
        return null;
    }
}
package xyfly.xyfly;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Xyfly extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {

    private final HashMap<UUID, Integer> flyTimeMap = new HashMap<>();
    private final HashMap<UUID, BukkitTask> flyTaskMap = new HashMap<>();
    private final HashMap<UUID, Boolean> noFallDamageMap = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        // 保存默认配置文件（如果不存在）
        saveDefaultConfig();
        // 检查并更新配置文件
        updateConfig();

        // 加载语言文件
        loadMessagesConfig();

        // 注册 xyfly 指令
        this.getCommand("xyfly").setExecutor(this);
        this.getCommand("xyfly").setTabCompleter(this);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);

        // 加载数据文件
        loadDataFromYAML();
    }

    @Override
    public void onDisable() {
        // 插件被禁用时的逻辑
        flyTaskMap.values().forEach(BukkitTask::cancel);

        // 保存数据文件
        saveDataToYAML();
    }

    private void loadMessagesConfig() {
        String language = getConfig().getString("language", "en");
        File messagesFile = new File(getDataFolder(), "languages/" + language + ".yml");
        if (!messagesFile.exists()) {
            saveResource("languages/" + language + ".yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("messages." + key, key));
    }

    private void loadDataFromYAML() {
        dataFile = new File(getDataFolder(), "flyData.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : dataConfig.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            int flyTime = dataConfig.getInt(key);
            flyTimeMap.put(playerId, flyTime);
        }
    }

    private void saveDataToYAML() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }
        for (UUID playerId : flyTimeMap.keySet()) {
            dataConfig.set(playerId.toString(), flyTimeMap.get(playerId));
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConfig() {
        FileConfiguration config = getConfig();
        boolean saveConfig = false;

        if (!config.contains("flyTimeMessage")) {
            config.set("flyTimeMessage", "剩余飞行时间: {time} 秒");
            saveConfig = true;
        }

        if (!config.contains("fallDamageAfterFlyTimeExpired")) {
            config.set("fallDamageAfterFlyTimeExpired", false);
            saveConfig = true;
        }

        if (saveConfig) {
            saveConfig();
        }
    }

    public HashMap<UUID, Integer> getFlyTimeMap() {
        return flyTimeMap;
    }

    public HashMap<UUID, BukkitTask> getFlyTaskMap() {
        return flyTaskMap;
    }

    public void stopFlying(Player player) {
        player.setFlying(false);
        player.setAllowFlight(false);

        UUID playerId = player.getUniqueId();
        if (flyTaskMap.containsKey(playerId)) {
            flyTaskMap.get(playerId).cancel();
            flyTaskMap.remove(playerId);
        }

        // 检查配置是否允许掉落伤害
        boolean fallDamage = getConfig().getBoolean("fallDamageAfterFlyTimeExpired", false);
        if (!fallDamage) {
            noFallDamageMap.put(playerId, true);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!player.hasPermission("xyfly.use")) {
            return;
        }

        // 玩家起飞设置
        if (event.isFlying()) {
            if (flyTimeMap.containsKey(playerId)) {
                int timeLeft = flyTimeMap.get(playerId);

                if (timeLeft <= 0) {
                    stopFlying(player);
                    player.sendMessage(getMessage("fly_time_expired"));
                    return;
                }

                // 启动倒计时任务
                if (!flyTaskMap.containsKey(playerId)) {
                    BukkitTask task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            int newTimeLeft = flyTimeMap.get(playerId) - 1;
                            flyTimeMap.put(playerId, newTimeLeft);

                            // 使用 Spigot API 发送 Action Bar 消息
                            BaseComponent[] message = new BaseComponent[] {
                                    new TextComponent(ChatColor.GREEN + getConfig().getString("flyTimeMessage").replace("{time}", String.valueOf(newTimeLeft)))
                            };
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);

                            if (newTimeLeft <= 0) {
                                stopFlying(player);
                                player.sendMessage(getMessage("fly_time_expired"));
                                cancel();
                            }
                        }
                    }.runTaskTimer(this, 20, 20);
                    flyTaskMap.put(playerId, task);
                }
            }
        } else {
            // 玩家降落
            if (flyTaskMap.containsKey(playerId)) {
                flyTaskMap.get(playerId).cancel();
                flyTaskMap.remove(playerId);
                player.sendMessage(getMessage("fly_time_paused"));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // 玩家退出时停止飞行任务
        if (flyTaskMap.containsKey(playerId)) {
            flyTaskMap.get(playerId).cancel();
            flyTaskMap.remove(playerId);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();

            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && noFallDamageMap.containsKey(playerId)) {
                event.setCancelled(true);
                noFallDamageMap.remove(playerId);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getMessage("usage"));
            return false;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(getMessage("only_players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xyfly.on")) {
                player.sendMessage(getMessage("no_permission"));
                return true;
            }
            handleFlyOn(player);
        } else if (args[0].equalsIgnoreCase("off")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(getMessage("only_players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xyfly.off")) {
                player.sendMessage(getMessage("no_permission"));
                return true;
            }
            stopFlying(player);
            player.sendMessage(getMessage("fly_off"));
        } else if (args[0].equalsIgnoreCase("settime")) {
            if (args.length < 2 || args.length > 3) {
                sender.sendMessage(getMessage("usage"));
                return false;
            }

            Player target;
            int timeIndex;

            if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(getMessage("only_players"));
                    return true;
                }
                target = (Player) sender;
                timeIndex = 1;
            } else {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(getMessage("player_not_online").replace("{player}", args[1]));
                    return true;
                }
                timeIndex = 2;
            }

            int time;
            try {
                time = Integer.parseInt(args[timeIndex]);
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage("invalid_time"));
                return true;
            }

            flyTimeMap.put(target.getUniqueId(), time);
            sender.sendMessage(getMessage("fly_time_set").replace("{player}", target.getName()).replace("{time}", String.valueOf(time)));
            target.sendMessage(getMessage("fly_time_set_target").replace("{time}", String.valueOf(time)));
        } else if (args[0].equalsIgnoreCase("gettime")) {
            if (args.length < 2) {
                sender.sendMessage(getMessage("usage"));
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(getMessage("player_not_online").replace("{player}", args[1]));
                return true;
            }

            int remainingTime = getRemainingFlyTime(target);
            sender.sendMessage(getMessage("remaining_fly_time").replace("{player}", target.getName()).replace("{time}", String.valueOf(remainingTime)));
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("xyfly.reload")) {
                sender.sendMessage(getMessage("no_permission"));
                return true;
            }

            reloadConfig();
            loadMessagesConfig();
            sender.sendMessage(getMessage("config_reloaded"));
        } else {
            sender.sendMessage(getMessage("usage"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("on");
            completions.add("off");
            completions.add("settime");
            completions.add("gettime");
            completions.add("reload");
            return completions;
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("settime") || args[0].equalsIgnoreCase("gettime"))) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }
        return null;
    }

    private void handleFlyOn(Player player) {
        UUID playerId = player.getUniqueId();
        int remainingTime = getRemainingFlyTime(player);

        if (remainingTime > 0) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(getMessage("fly_on"));
            sendActionBar(player, getMessage("fly_time_message").replace("{time}", String.valueOf(remainingTime)));
        } else {
            player.sendMessage(getMessage("not_enough_fly_time"));
        }
    }

    private int getRemainingFlyTime(Player player) {
        return flyTimeMap.getOrDefault(player.getUniqueId(), 0);
    }

    private void sendActionBar(Player player, String message) {
        try {
            String version = getVersion();
            if ("unknown".equals(version)) {
                return;
            }

            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutChat");
            Class<?> chatComponentTextClass = Class.forName("net.minecraft.network.chat.ChatComponentText");
            Class<?> chatMessageTypeClass = Class.forName("net.minecraft.network.chat.ChatMessageType");

            Constructor<?> chatComponentTextConstructor = chatComponentTextClass.getConstructor(String.class);
            Object chatComponentText = chatComponentTextConstructor.newInstance(message);

            Field field = chatMessageTypeClass.getField("GAME_INFO");
            Object chatMessageType = field.get(null);

            Constructor<?> packetPlayOutChatConstructor = packetPlayOutChatClass.getConstructor(chatComponentTextClass, chatMessageTypeClass, UUID.class);
            Object packetPlayOutChat = packetPlayOutChatConstructor.newInstance(chatComponentText, chatMessageType, player.getUniqueId());

            Object playerConnection = craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayer);
            playerConnection.getClass().getDeclaredMethod("sendPacket", Class.forName("net.minecraft.network.protocol.Packet")).invoke(playerConnection, packetPlayOutChat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] packageParts = packageName.split("\\.");
        if (packageParts.length >= 4) {
            return packageParts[3];
        } else {
            return "unknown";
        }
    }
}
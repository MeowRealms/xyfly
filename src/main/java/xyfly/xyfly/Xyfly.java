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
    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        // 保存默认配置文件（如果不存在）
        saveDefaultConfig();

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
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("用法: /xyfly <on|off|settime|gettime> [玩家] [时间(秒)]");
            return false;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("只有玩家可以使用这个命令！");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xyfly.on")) {
                player.sendMessage("你没有权限开启飞行模式！");
                return true;
            }
            handleFlyOn(player);
        } else if (args[0].equalsIgnoreCase("off")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("只有玩家可以使用这个命令！");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xyfly.off")) {
                player.sendMessage("你没有权限关闭飞行模式！");
                return true;
            }
            stopFlying(player);
            player.sendMessage("飞行模式已关闭！");
        } else if (args[0].equalsIgnoreCase("settime")) {
            if (args.length < 2 || args.length > 3) {
                sender.sendMessage("用法: /xyfly settime [玩家] <时间(秒)>");
                return false;
            }

            Player target;
            int timeIndex;

            if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("控制台必须指定玩家！");
                    return true;
                }
                target = (Player) sender;
                timeIndex = 1;
            } else {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("玩家 " + args[1] + " 不在线！");
                    return true;
                }
                timeIndex = 2;
            }

            try {
                int time = Integer.parseInt(args[timeIndex]);
                flyTimeMap.put(target.getUniqueId(), time);
                sender.sendMessage("玩家 " + target.getName() + " 的飞行时间已设置为 " + time + " 秒。");
                if (!target.equals(sender)) {
                    target.sendMessage("你的飞行时间已被设置为 " + time + " 秒。");
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("时间必须是一个整数！");
            }
        } else if (args[0].equalsIgnoreCase("gettime")) {
            if (args.length != 2) {
                sender.sendMessage("用法: /xyfly gettime <玩家>");
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("玩家 " + args[1] + " 不在线！");
                return true;
            }

            int timeLeft = flyTimeMap.getOrDefault(target.getUniqueId(), 0);
            sender.sendMessage("玩家 " + target.getName() + " 的剩余飞行时间为 " + timeLeft + " 秒。");
        } else {
            sender.sendMessage("用法: /xyfly <on|off|settime|gettime> [玩家] [时间(秒)]");
            return false;
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

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!player.hasPermission("xyfly.use")) {
            return;
        }

        // 玩家起飞
        if (event.isFlying()) {
            if (flyTimeMap.containsKey(playerId)) {
                int timeLeft = flyTimeMap.get(playerId);

                if (timeLeft <= 0) {
                    stopFlying(player);
                    player.sendMessage(ChatColor.RED + "你的飞行时间已用完！");
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
                                    new TextComponent(ChatColor.GREEN + "剩余飞行时间: " + newTimeLeft + " 秒")
                            };
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);

                            if (newTimeLeft <= 0) {
                                stopFlying(player);
                                player.sendMessage(ChatColor.RED + "你的飞行时间已用完！");
                                cancel();
                            }
                        }
                    }.runTaskTimer(this, 20, 20);
                    flyTaskMap.put(playerId, task);
                }
            } else {
                stopFlying(player);
                player.sendMessage(ChatColor.RED + "你没有飞行时间！");
            }
        }
    }

    private void handleFlyOn(Player player) {
        UUID playerId = player.getUniqueId();
        int remainingTime = getRemainingFlyTime(player);

        if (remainingTime > 0) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(ChatColor.GREEN + "飞行模式已开启！");
            sendActionBar(player, ChatColor.GREEN + "剩余飞行时间: " + remainingTime + " 秒");
        } else {
            player.sendMessage(ChatColor.RED + "你没有足够的飞行时间！");
        }
    }

    private int getRemainingFlyTime(Player player) {
        return flyTimeMap.getOrDefault(player.getUniqueId(), 0);
    }

    private void sendActionBar(Player player, String message) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getVersion() + ".entity.CraftPlayer");
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
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}

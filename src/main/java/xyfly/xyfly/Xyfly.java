package xyfly.xyfly;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Xyfly extends JavaPlugin {

    private final HashMap<UUID, Integer> flyTimeMap = new HashMap<>();
    private final HashMap<UUID, BukkitTask> flyTaskMap = new HashMap<>();
    public final HashMap<UUID, Boolean> noFallDamageMap = new HashMap<>();
    private DataManager dataManager;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        // 保存默认配置文件（如果不存在）
        saveDefaultConfig();
        // 检查并更新配置文件
        updateConfig();

        // 加载语言文件
        loadMessagesConfig();

        // 初始化 DataManager
        dataManager = DataManager.getInstance(this);

        // 注册 xyfly 指令
        XyflyCommandExecutor commandExecutor = new XyflyCommandExecutor(this);
        this.getCommand("xyfly").setExecutor(commandExecutor);
        this.getCommand("xyfly").setTabCompleter(commandExecutor);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new XyflyEventListener(this), this);

        // 加载数据文件
        dataManager.loadData();
    }

    @Override
    public void onDisable() {
        // 插件被禁用时的逻辑
        flyTaskMap.values().forEach(BukkitTask::cancel);

        // 保存数据文件
        dataManager.saveData();
        dataManager.closeConnection();
    }

    private void loadMessagesConfig() {
        String language = getConfig().getString("language", "en");
        File messagesFile = new File(getDataFolder(), "languages/" + language + ".yml");
        if (!messagesFile.exists()) {
            saveResource("languages/" + language + ".yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("messages." + key, key));
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

    public Map<UUID, Integer> getFlyTimeMap() {
        return flyTimeMap;
    }

    public Map<UUID, BukkitTask> getFlyTaskMap() {
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

    public void handleFlyOn(Player player) {
        UUID playerId = player.getUniqueId();
        int remainingTime = getRemainingFlyTime(player);

        if (remainingTime > 0) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(getMessage("fly_on"));
            sendActionBar(player, getMessage("flyTimeMessage").replace("{time}", String.valueOf(remainingTime)));
        } else {
            player.sendMessage(getMessage("not_enough_fly_time"));
        }
    }

    public int getRemainingFlyTime(Player player) {
        return flyTimeMap.getOrDefault(player.getUniqueId(), 0);
    }

    public void sendActionBar(Player player, String message) {
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

    public void publicLoadMessagesConfig() {
        loadMessagesConfig();
    }
}
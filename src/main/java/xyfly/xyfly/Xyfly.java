package xyfly.xyfly;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class Xyfly extends JavaPlugin {

    private final HashMap<UUID, Integer> flyTimeMap = new HashMap<>(); // 存储玩家剩余飞行时间的映射
    private final HashMap<UUID, BukkitTask> flyTaskMap = new HashMap<>(); // 存储玩家飞行任务的映射
    public final HashMap<UUID, Boolean> noFallDamageMap = new HashMap<>(); // 存储玩家是否免受掉落伤害的映射
    private DataManager dataManager; // 数据管理器实例
    private FileConfiguration messagesConfig; // 消息配置文件

    private boolean disableFlyInCombat; // 是否在战斗状态下禁用飞行

    @Override
    public void onEnable() {
        // 初始化插件
        setupPlugin();

        // 注册命令执行器和事件监听器
        registerCommandsAndListeners();

        // 加载数据文件
        dataManager.loadData();
    }

    @Override
    public void onDisable() {
        // 插件被禁用时的逻辑
        cleanUp();
    }

    private void setupPlugin() {
        // 保存默认配置文件（如果不存在）
        saveDefaultConfig();

        // 检查并更新配置文件
        updateConfig();

        // 加载语言文件
        loadMessagesConfig();

        // 初始化 DataManager
        dataManager = DataManager.getInstance(this);
    }

    private void registerCommandsAndListeners() {
        // 注册命令执行器
        XyflyCommandExecutor commandExecutor = new XyflyCommandExecutor(this);
        if (getCommand("xyfly") != null) {
            this.getCommand("xyfly").setExecutor(commandExecutor);
            this.getCommand("xyfly").setTabCompleter(commandExecutor);
        }

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new XyflyEventListener(this), this);
    }

    private void loadMessagesConfig() {
        // 加载消息配置文件
        String language = getConfig().getString("language", "en");
        File messagesFile = new File(getDataFolder(), "languages/" + language + ".yml");
        if (!messagesFile.exists()) {
            saveResource("languages/" + language + ".yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void updateConfig() {
        // 更新配置文件
        ConfigUpdater configUpdater = new ConfigUpdater(getDataFolder(), "config.yml");
        configUpdater.checkAndUpdateConfig();

        // 加载配置项
        disableFlyInCombat = getConfig().getBoolean("combat.disableFly", true);
    }

    private void cleanUp() {
        // 清理工作
        flyTaskMap.values().forEach(BukkitTask::cancel); // 取消所有飞行任务
        if (this.dataManager != null) {
            this.dataManager.saveData(); // 保存数据
            this.dataManager.closeConnection(); // 关闭数据连接
        } else {
            getLogger().warning("DataManager is null during plugin disable. Data might not have been saved.");
        }
    }

    public String getMessage(String key) {
        // 获取翻译后的消息
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString("messages." + key, key));
    }

    public void stopFlying(Player player) {
        // 停止玩家飞行的逻辑
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
        // 处理玩家飞行开启的逻辑
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
        // 获取玩家剩余飞行时间的逻辑
        return flyTimeMap.getOrDefault(player.getUniqueId(), 0);
    }

    public HashMap<UUID, Integer> getFlyTimeMap() {
        return flyTimeMap;
    }

    public HashMap<UUID, BukkitTask> getFlyTaskMap() {
        return flyTaskMap;
    }

    public void sendActionBar(Player player, String message) {
        // 发送动作栏消息的逻辑
        // 省略部分代码，你可以将发送动作栏消息的逻辑放在这里
    }

    private String getVersion() {
        // 获取服务器版本的逻辑
        // 省略部分代码，你可以将获取服务器版本的逻辑放在这里
        return "1.0.0"; // 示例返回一个版本号
    }

    public void publicLoadMessagesConfig() {
        // 公开调用加载消息配置的方法
        loadMessagesConfig();
    }

    public boolean isDisableFlyInCombat() {
        // 返回是否在战斗状态下禁用飞行的配置
        return disableFlyInCombat;
    }

}
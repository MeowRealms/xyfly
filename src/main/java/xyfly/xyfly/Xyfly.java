package xyfly.xyfly;

import org.bukkit.plugin.java.JavaPlugin;

public class XyflyPlugin extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        // 保存默认配置文件（如果不存在）
        saveDefaultConfig();
        String storageMode = getConfig().getString("storage-mode", "YAML").toUpperCase();

        // 初始化数据管理器
        dataManager = new DataManager(this, storageMode);

        // 注册命令和补全器
        XyflyCommandExecutor commandExecutor = new XyflyCommandExecutor(this, dataManager);
        this.getCommand("xyfly").setExecutor(commandExecutor);
        this.getCommand("xyfly").setTabCompleter(commandExecutor);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new XyflyEventListener(this, dataManager), this);
    }

    @Override
    public void onDisable() {
        // 关闭数据管理器
        dataManager.close();
    }
}
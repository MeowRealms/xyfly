package xyfly.xyfly;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUpdater {

    private File configFile;
    private FileConfiguration config;
    private int configVersion = 1; // 当前配置文件版本号

    public ConfigUpdater(File configFile, String configFileName) {
        this.configFile = new File(configFile, configFileName);
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public void checkAndUpdateConfig() {
        if (config.contains("configVersion")) {
            int savedVersion = config.getInt("configVersion");
            if (savedVersion < configVersion) {
                // 执行配置文件更新操作
                updateConfig(savedVersion);
            }
        } else {
            // 首次加载配置文件，设置版本号
            config.set("configVersion", configVersion);
            saveConfig();
        }
    }

    private void updateConfig(int savedVersion) {
        // 根据不同版本号执行不同的更新操作
        if (savedVersion < 1) {
            // 版本1的更新操作
            config.set("newConfigItem", "defaultValue");
        }

        // 更新版本号为当前版本
        config.set("configVersion", configVersion);
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
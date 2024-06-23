package xyfly.xyfly;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YAMLDataManager extends DataManager {

    private File dataFile;
    private FileConfiguration dataConfig;

    public YAMLDataManager(Xyfly plugin) {
        super(plugin);
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "flyData.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public void loadData() {
        if (dataFile.exists()) {
            // 检查flyTimes部分是否存在
            if (dataConfig.isConfigurationSection("flyTimes")) {
                for (String key : dataConfig.getConfigurationSection("flyTimes").getKeys(false)) {
                    int flyTime = dataConfig.getInt("flyTimes." + key);
                    plugin.getFlyTimeMap().put(UUID.fromString(key), flyTime);
                }
            }
        }
    }

    @Override
    public void saveData() {
        for (UUID uuid : plugin.getFlyTimeMap().keySet()) {
            int flyTime = plugin.getFlyTimeMap().get(uuid);
            dataConfig.set("flyTimes." + uuid.toString(), flyTime);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        // 对于基于文件的存储，通常不需要关闭连接的操作
    }

    @Override
    public void saveFlyTime(String playerUUID, int time) {
        UUID uuid = UUID.fromString(playerUUID);
        plugin.getFlyTimeMap().put(uuid, time); // 更新内存中的数据

        // 更新配置文件中的数据
        dataConfig.set("flyTimes." + playerUUID, time);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getFlyTime(String playerUUID) {
        UUID uuid = UUID.fromString(playerUUID);

        // 从内存中获取数据
        if (plugin.getFlyTimeMap().containsKey(uuid)) {
            return plugin.getFlyTimeMap().get(uuid);
        }

        // 从配置文件中获取数据
        return dataConfig.getInt("flyTimes." + playerUUID, 0);
    }
}

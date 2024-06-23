package xyfly.xyfly;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public void loadData() {
        // 省略已有的实现...
    }

    @Override
    public void saveData() {
        // 省略已有的实现...
    }

    @Override
    public void closeConnection() {
        // 对于基于文件的存储，这个方法可能不需要实现任何操作。
    }

    @Override
    public void saveFlyTime(String playerUUID, int time) {
        dataConfig.set("flyTimes." + playerUUID, time);
        try {
            dataConfig.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getFlyTime(String playerUUID) {
        return dataConfig.getInt("flyTimes." + playerUUID, 0);
    }
}
package xyfly.xyfly;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class YAMLDataManager extends DataManager {

    private final File dataFile;
    private FileConfiguration dataConfig;

    public YAMLDataManager(Xyfly plugin) {
        super(plugin);
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public void loadData() {
        for (String key : dataConfig.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            int flyTime = dataConfig.getInt(key + ".flyTime");
            plugin.getFlyTimeMap().put(playerId, flyTime);
        }
    }

    @Override
    public void saveData() {
        for (Map.Entry<UUID, Integer> entry : plugin.getFlyTimeMap().entrySet()) {
            UUID playerId = entry.getKey();
            int flyTime = entry.getValue();
            dataConfig.set(playerId.toString() + ".flyTime", flyTime);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        // YAML 不需要关闭连接
    }
}
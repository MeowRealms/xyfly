package xyfly.xyfly;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class DataManager {

    protected final Xyfly plugin;

    public DataManager(Xyfly plugin) {
        this.plugin = plugin;
    }

    public abstract void loadData();

    public abstract void saveData();

    public abstract void closeConnection();

    public static DataManager getInstance(Xyfly plugin) {
        FileConfiguration config = plugin.getConfig();
        String storageType = config.getString("storage.type", "yaml");

        if ("sqlite".equalsIgnoreCase(storageType)) {
            return new SQLiteDataManager(plugin);
        } else {
            return new YAMLDataManager(plugin);
        }
    }
}
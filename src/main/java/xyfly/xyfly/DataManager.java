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

    public abstract void saveFlyTime(String player, int time);

    public abstract int getFlyTime(String player);

    public static DataManager getInstance(Xyfly plugin) {
        FileConfiguration config = plugin.getConfig();
        String storageType = config.getString("storage.type", "yaml").toLowerCase();

        switch (storageType) {
            case "mysql":
                return new MySQLDataManager(plugin);
            case "sqlite":
                return new SQLiteDataManager(plugin);
            case "yaml":
            default:
                return new YAMLDataManager(plugin);
        }
    }
}

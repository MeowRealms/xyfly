package xyfly.xyfly;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLiteDataManager extends DataManager {

    private Connection connection;

    public SQLiteDataManager(Xyfly plugin) {
        super(plugin);
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File databaseFile = new File(dataFolder, "data.db");
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS fly_data (" +
                "player_uuid TEXT PRIMARY KEY," +
                "fly_time INTEGER" +
                ")";
        try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadData() {
        String sql = "SELECT player_uuid, fly_time FROM fly_data";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String playerUUID = rs.getString("player_uuid");
                int flyTime = rs.getInt("fly_time");
                // 将数据加载到内存中
                plugin.getFlyTimeMap().put(UUID.fromString(playerUUID), flyTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveData() {
        String sql = "INSERT OR REPLACE INTO fly_data (player_uuid, fly_time) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (UUID playerUUID : plugin.getFlyTimeMap().keySet()) {
                int flyTime = plugin.getFlyTimeMap().get(playerUUID);
                pstmt.setString(1, playerUUID.toString());
                pstmt.setInt(2, flyTime);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveFlyTime(String playerUUID, int time) {
        UUID uuid = UUID.fromString(playerUUID);
        plugin.getFlyTimeMap().put(uuid, time); // 更新内存中的数据

        // 更新数据库中的数据
        String sql = "INSERT OR REPLACE INTO fly_data (player_uuid, fly_time) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            pstmt.setInt(2, time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
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

        // 从数据库中获取数据
        String sql = "SELECT fly_time FROM fly_data WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int flyTime = rs.getInt("fly_time");
                // 加载到内存中
                plugin.getFlyTimeMap().put(uuid, flyTime);
                return flyTime;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // 如果没有找到记录，返回0
    }
}
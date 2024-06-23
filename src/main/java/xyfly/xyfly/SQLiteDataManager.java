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
        // 省略已有的实现...
    }

    @Override
    public void saveData() {
        // 省略已有的实现...
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
        String sql = "SELECT fly_time FROM fly_data WHERE player_uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("fly_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // 如果没有找到记录，返回0
    }
}
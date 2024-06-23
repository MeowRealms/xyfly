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
        String selectSQL = "SELECT player_uuid, fly_time FROM fly_data";
        try (PreparedStatement stmt = connection.prepareStatement(selectSQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UUID playerId = UUID.fromString(rs.getString("player_uuid"));
                int flyTime = rs.getInt("fly_time");
                plugin.getFlyTimeMap().put(playerId, flyTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveData() {
        String insertOrUpdateSQL = "INSERT OR REPLACE INTO fly_data (player_uuid, fly_time) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertOrUpdateSQL)) {
            for (UUID playerId : plugin.getFlyTimeMap().keySet()) {
                stmt.setString(1, playerId.toString());
                stmt.setInt(2, plugin.getFlyTimeMap().get(playerId));
                stmt.addBatch();
            }
            stmt.executeBatch();
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
}
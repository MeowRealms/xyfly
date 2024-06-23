package xyfly.xyfly;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDataManager extends DataManager {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private Connection connection;

    public MySQLDataManager(Xyfly plugin) {
        super(plugin);
        this.host = plugin.getConfig().getString("mysql.host");
        this.port = plugin.getConfig().getInt("mysql.port");
        this.database = plugin.getConfig().getString("mysql.database");
        this.username = plugin.getConfig().getString("mysql.username");
        this.password = plugin.getConfig().getString("mysql.password");
        if(connect()){
            createTableIfNotExists();
        } else {
            plugin.getLogger().severe("Failed to connect to the MySQL database.");
            // 可以选择在这里禁用插件，因为数据库连接是必要的
            // plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    private boolean connect() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC", username, password
            );
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createTableIfNotExists() {
        if (connection == null) {
            throw new IllegalStateException("Attempted to create tables without a database connection.");
        }
        String createTableSQL = "CREATE TABLE IF NOT EXISTS fly_time (" +
                "player VARCHAR(36) PRIMARY KEY, " +
                "time INT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadData() {
        // 实现加载数据的逻辑
    }

    @Override
    public void saveData() {
        // 实现保存数据的逻辑
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
    public void saveFlyTime(String player, int time) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "REPLACE INTO fly_time (player, time) VALUES (?, ?)"
            );
            ps.setString(1, player);
            ps.setInt(2, time);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getFlyTime(String player) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT time FROM fly_time WHERE player=?"
            );
            ps.setString(1, player);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
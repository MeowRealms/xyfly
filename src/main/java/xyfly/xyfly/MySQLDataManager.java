package xyfly.xyfly;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQLDataManager extends DataManager {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private Connection connection;
    private Map<UUID, Integer> flyTimes; // 内存中存储玩家飞行时间

    public MySQLDataManager(Xyfly plugin) {
        super(plugin);
        this.host = plugin.getConfig().getString("mysql.host");
        this.port = plugin.getConfig().getInt("mysql.port");
        this.database = plugin.getConfig().getString("mysql.database");
        this.username = plugin.getConfig().getString("mysql.username");
        this.password = plugin.getConfig().getString("mysql.password");
        this.flyTimes = new HashMap<>(); // 初始化存储玩家飞行时间的Map
        if (connect()) {
            createTableIfNotExists();
            loadFlyTimes(); // 插件启动时加载数据
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

    public void loadFlyTimes() {
        String sql = "SELECT player, time FROM fly_time";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player"));
                int time = rs.getInt("time");
                flyTimes.put(playerUUID, time); // 加载数据到内存中
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveFlyTimes() {
        String sql = "REPLACE INTO fly_time (player, time) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map.Entry<UUID, Integer> entry : flyTimes.entrySet()) {
                ps.setString(1, entry.getKey().toString());
                ps.setInt(2, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch(); // 批量保存数据到数据库
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadData() {
        loadFlyTimes();
    }

    @Override
    public void saveData() {
        saveFlyTimes();
    }

    @Override
    public void closeConnection() {
        saveFlyTimes(); // 插件禁用时保存数据
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
        UUID playerUUID = UUID.fromString(player);
        flyTimes.put(playerUUID, time); // 更新内存中的数据
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
        UUID playerUUID = UUID.fromString(player);
        return flyTimes.getOrDefault(playerUUID, 0); // 从内存中获取数据
    }
}

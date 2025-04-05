package ClanTerritory.clanTerritory;



import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private static final Dotenv dotenv = Dotenv.load(); // Загрузка переменных окружения из .env

    private static final String URL = dotenv.get("DATABASE_URL"); // Чтение URL из .env
    private static final String USER = dotenv.get("DATABASE_USER"); // Чтение имени пользователя из .env
    private static final String PASSWORD = dotenv.get("DATABASE_PASSWORD"); // Чтение пароля из .env

    private static Connection connection;

    // Подключение к базе данных
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    // Создание таблиц (если они не существуют)
    public static void createTables() {
        try (Connection conn = getConnection()) {
            String createClanTableQuery = "CREATE TABLE IF NOT EXISTS clan_zones (" +
                    "id SERIAL PRIMARY KEY," +
                    "clan_name VARCHAR(255) NOT NULL," +
                    "center_x INT NOT NULL," +
                    "center_y INT NOT NULL," +
                    "center_z INT NOT NULL," +
                    "radius INT NOT NULL" +
                    ");";
            String createPlayerClanTableQuery = "CREATE TABLE IF NOT EXISTS player_clans (" +
                    "id SERIAL PRIMARY KEY," +
                    "player_id BIGINT NOT NULL," +
                    "clan_name VARCHAR(255) NOT NULL," +
                    "FOREIGN KEY (clan_name) REFERENCES clan_zones(clan_name)" +
                    ");";
            try (PreparedStatement stmt = conn.prepareStatement(createClanTableQuery)) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(createPlayerClanTableQuery)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Сохранение клана в базе данных (без зоны)
    public static void saveClan(String clanName) {
        try (Connection conn = getConnection()) {
            String insertQuery = "INSERT INTO clan_zones (clan_name) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, clanName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Сохранение зоны для клана в базе данных
    public static void saveClanZone(String clanName, int centerX, int centerY, int centerZ, int radius) {
        try (Connection conn = getConnection()) {
            String insertQuery = "INSERT INTO clan_zones (clan_name, center_x, center_y, center_z, radius) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, clanName);
                stmt.setInt(2, centerX);
                stmt.setInt(3, centerY);
                stmt.setInt(4, centerZ);
                stmt.setInt(5, radius);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получение информации о клане по его имени
    public static Region getClanZone(String clanName) {
        try (Connection conn = getConnection()) {
            String selectQuery = "SELECT center_x, center_y, center_z, radius FROM clan_zones WHERE clan_name = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setString(1, clanName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int centerX = rs.getInt("center_x");
                        int centerY = rs.getInt("center_y");
                        int centerZ = rs.getInt("center_z");
                        int radius = rs.getInt("radius");
                        return new Region(centerX, centerY, centerZ, radius);  // Возвращаем объект Region
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Если клан не найден
    }

    // Сохранение игрока в клан
    public static void savePlayerClan(UUID playerUuid, String clanName) {
        try (Connection conn = getConnection()) {
            String insertQuery = "INSERT INTO player_clans (player_uuid, clan_name) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setObject(1, playerUuid);
                stmt.setString(2, clanName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Загрузка клана игрока
    public static String getPlayerClan(UUID playerUuid) {
        try (Connection conn = getConnection()) {
            String selectQuery = "SELECT clan_name FROM player_clans WHERE player_uuid = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setObject(1, playerUuid);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("clan_name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Если клан не найден
    }

    // Удаление игрока из клана
    public static void removePlayerFromClan(UUID playerUuid) {
        try (Connection conn = getConnection()) {
            String deleteQuery = "DELETE FROM player_clans WHERE player_uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                stmt.setObject(1, playerUuid);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

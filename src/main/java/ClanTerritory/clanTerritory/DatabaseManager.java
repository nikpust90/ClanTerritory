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

    private static final Dotenv dotenv = Dotenv.load(); // Загрузка из .env

    private static final String URL = dotenv.get("DATABASE_URL");
    private static final String USER = dotenv.get("DATABASE_USER");
    private static final String PASSWORD = dotenv.get("DATABASE_PASSWORD");

    private static Connection connection;


    static {
        try {
            // 1. Проверка наличия драйвера
            Class.forName("org.postgresql.Driver");
            System.out.println("[ClanTerritory] PostgreSQL драйвер успешно зарегистрирован");

            // 2. Тестовое подключение
            System.out.println("[ClanTerritory] Тестирование подключения к: " + URL);
            try (Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                System.out.println("[ClanTerritory] ✅ Тестовое подключение успешно!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[ClanTerritory] ❌ PostgreSQL драйвер не найден!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[ClanTerritory] ❌ Ошибка тестового подключения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Подключение к БД
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("[ClanTerritory] Подключение к базе данных...");
            System.out.println("[ClanTerritory] URL: " + URL);
            System.out.println("[ClanTerritory] USER: " + USER);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[ClanTerritory] Успешное подключение к базе данных!");
        }
        return connection;
    }

    // Создание таблиц
    public static void createTables() {
        System.out.println("[ClanTerritory] Создание таблиц, если они не существуют...");

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
                System.out.println("[ClanTerritory] Таблица clan_zones создана или уже существует.");
            }

            try (PreparedStatement stmt = conn.prepareStatement(createPlayerClanTableQuery)) {
                stmt.executeUpdate();
                System.out.println("[ClanTerritory] Таблица player_clans создана или уже существует.");
            }

            System.out.println("[ClanTerritory] Все таблицы готовы к использованию!");

        } catch (SQLException e) {
            System.out.println("[ClanTerritory] Ошибка при создании таблиц:");
            e.printStackTrace();
        }
    }

    // Сохранение клана (без зоны)
    public static void saveClan(String clanName, UUID ownerUuid) {
        try (Connection conn = getConnection()) {
            System.out.println("[ClanTerritory] Сохраняем клан '" + clanName + "' с владельцем " + ownerUuid);
            String insertQuery = "INSERT INTO clan_zones (clan_name, owner_uuid) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, clanName);
                stmt.setObject(2, ownerUuid);
                stmt.executeUpdate();
                System.out.println("[ClanTerritory] Клан '" + clanName + "' успешно сохранён в базе.");
            }
        } catch (SQLException e) {
            System.out.println("[ClanTerritory] Ошибка при сохранении клана:");
            e.printStackTrace();
        }
    }

    // Сохранение зоны клана
    public static void saveClanZone(String clanName, int centerX, int centerY, int centerZ, int radius) {
        try (Connection conn = getConnection()) {
            System.out.println("[ClanTerritory] Сохраняем зону клана '" + clanName + "'");
            String insertQuery = "INSERT INTO clan_zones (clan_name, center_x, center_y, center_z, radius) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, clanName);
                stmt.setInt(2, centerX);
                stmt.setInt(3, centerY);
                stmt.setInt(4, centerZ);
                stmt.setInt(5, radius);
                stmt.executeUpdate();
                System.out.println("[ClanTerritory] Зона клана '" + clanName + "' успешно сохранена.");
            }
        } catch (SQLException e) {
            System.out.println("[ClanTerritory] Ошибка при сохранении зоны клана:");
            e.printStackTrace();
        }
    }

    // Получение информации о зоне клана
    public static Region getClanZone(String clanName) {
        try (Connection conn = getConnection()) {
            System.out.println("[ClanTerritory] Загружаем зону клана '" + clanName + "'");
            String selectQuery = "SELECT center_x, center_y, center_z, radius FROM clan_zones WHERE clan_name = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setString(1, clanName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int centerX = rs.getInt("center_x");
                        int centerY = rs.getInt("center_y");
                        int centerZ = rs.getInt("center_z");
                        int radius = rs.getInt("radius");
                        System.out.println("[ClanTerritory] Зона найдена: (" + centerX + ", " + centerY + ", " + centerZ + "), радиус " + radius);
                        return new Region(centerX, centerY, centerZ, radius);
                    } else {
                        System.out.println("[ClanTerritory] Зона для клана '" + clanName + "' не найдена.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[ClanTerritory] Ошибка при загрузке зоны клана:");
            e.printStackTrace();
        }
        return null;
    }

    // Сохранение игрока в клан
    public static void savePlayerClan(UUID playerUuid, String clanName) {
        try (Connection conn = getConnection()) {
            System.out.println("[ClanTerritory] Добавляем игрока " + playerUuid + " в клан '" + clanName + "'");
            String insertQuery = "INSERT INTO player_clans (player_uuid, clan_name) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setObject(1, playerUuid);
                stmt.setString(2, clanName);
                stmt.executeUpdate();
                System.out.println("[ClanTerritory] Игрок " + playerUuid + " добавлен в клан '" + clanName + "'");
            }
        } catch (SQLException e) {
            System.out.println("[ClanTerritory] Ошибка при добавлении игрока в клан:");
            e.printStackTrace();
        }
    }

    // Загрузка клана игрока
    public static String getPlayerClan(UUID playerUuid) {
        try (Connection conn = getConnection()) {
            System.out.println("[ClanTerritory] Получаем клан игрока " + playerUuid);
            String selectQuery = "SELECT clan_name FROM player_clans WHERE player_uuid = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setObject(1, playerUuid);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String clan = rs.getString("clan_name");
                        System.out.println("[ClanTerritory] Игрок " + playerUuid + " состоит в клане '" + clan + "'");
                        return clan;
                    } else {
                        System.out.println("[ClanTerritory] Игрок " + playerUuid + " не состоит в клане.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[ClanTerritory] Ошибка при получении клана игрока:");
            e.printStackTrace();
        }
        return null;
    }

    // Удаление игрока из клана
    public static void removePlayerFromClan(UUID playerUuid) {
        try (Connection conn = getConnection()) {
            System.out.println("[ClanTerritory] Удаляем игрока " + playerUuid + " из клана");
            String deleteQuery = "DELETE FROM player_clans WHERE player_uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                stmt.setObject(1, playerUuid);
                stmt.executeUpdate();
                System.out.println("[ClanTerritory] Игрок " + playerUuid + " успешно удалён из клана.");
            }
        } catch (SQLException e) {
            System.out.println("[ClanTerritory] Ошибка при удалении игрока из клана:");
            e.printStackTrace();
        }
    }
}

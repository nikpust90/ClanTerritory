package ClanTerritory.clanTerritory;



import io.github.cdimascio.dotenv.Dotenv;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class DatabaseManager {

    private static DatabaseManager instance;

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

    // Создание таблиц с правильными ограничениями
    public void createTables() {
        String[] tableQueries = {
                "CREATE TABLE IF NOT EXISTS clans (" +
                        "id SERIAL PRIMARY KEY," +
                        "name VARCHAR(255) NOT NULL UNIQUE," +
                        "owner_uuid UUID NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                "CREATE TABLE IF NOT EXISTS clan_zones (" +
                        "id SERIAL PRIMARY KEY," +
                        "clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE," +
                        "center_x INTEGER NOT NULL," +
                        "center_y INTEGER NOT NULL," +
                        "center_z INTEGER NOT NULL," +
                        "radius INTEGER NOT NULL," +
                        "world VARCHAR(255) NOT NULL" +
                        ")",

                "CREATE TABLE IF NOT EXISTS clan_members (" +
                        "id SERIAL PRIMARY KEY," +
                        "clan_id INTEGER NOT NULL REFERENCES clans(id) ON DELETE CASCADE," +
                        "player_uuid UUID NOT NULL UNIQUE," +
                        "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        };

        String[] indexQueries = {
                "CREATE INDEX IF NOT EXISTS idx_clan_members_player ON clan_members(player_uuid)",
                "CREATE INDEX IF NOT EXISTS idx_clan_zones_clan ON clan_zones(clan_id)"
        };

        try (Connection conn = getConnection()) {
            System.out.println("Создание таблиц...");

            conn.setAutoCommit(false); // включаем ручной коммит

            for (String query : tableQueries) {
                try (Statement stmt = conn.createStatement()) {
                    System.out.println("Выполняется: " + query);
                    stmt.executeUpdate(query);
                    System.out.println("✅ Успешно");
                } catch (SQLException e) {
                    System.err.println("❌ Ошибка при выполнении запроса таблицы:");
                    System.err.println(query);
                    e.printStackTrace();
                }
            }

            conn.commit(); // фиксируем структуру таблиц

            System.out.println("Создание индексов...");

            for (String query : indexQueries) {
                try (Statement stmt = conn.createStatement()) {
                    System.out.println("Выполняется: " + query);
                    stmt.executeUpdate(query);
                    System.out.println("✅ Успешно");
                } catch (SQLException e) {
                    System.err.println("❌ Ошибка при выполнении запроса индекса:");
                    System.err.println(query);
                    e.printStackTrace();
                }
            }

            System.out.println("✅ Все таблицы и индексы успешно созданы");

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при создании таблиц или индексов");
            e.printStackTrace();
        }
    }




    // Сохранение клана без зоны
    public static boolean saveClan(Clan clan) {
        String clanQuery = "INSERT INTO clans (name, owner_uuid) VALUES (?, ?) " +
                "ON CONFLICT (name) DO UPDATE SET owner_uuid = EXCLUDED.owner_uuid " +
                "RETURNING id";

        try (Connection conn = getConnection()) {
            // Сохраняем клан и получаем его ID
            try (PreparedStatement stmt = conn.prepareStatement(clanQuery)) {
                stmt.setString(1, clan.getName());
                stmt.setObject(2, clan.getOwnerUuid());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int clanId = rs.getInt(1);
                        clan.setId(clanId); // если setId() есть, иначе можно просто игнорировать
                    }
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("[ClanTerritory] Ошибка сохранения клана:");
            e.printStackTrace();
            return false;
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }


    /**
     * Сохраняет или обновляет зону клана в базе данных
     * @param clan Клан, чья зона сохраняется
     * @param region Регион, который описывает зону
     * @return true, если операция прошла успешно
     */
    public boolean saveClanZone(ClanZone clanZone) {
        String sql = "INSERT INTO clan_zones (clan_id, center_x, center_y, center_z, radius, world) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (clan_id) DO UPDATE SET " +
                "center_x = excluded.center_x, center_y = excluded.center_y, center_z = excluded.center_z, " +
                "radius = excluded.radius, world = excluded.world";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clanZone.getClan().getId());
            stmt.setInt(2, clanZone.getRegion().getCenterX());
            stmt.setInt(3, clanZone.getRegion().getCenterY());
            stmt.setInt(4, clanZone.getRegion().getCenterZ());
            stmt.setInt(5, clanZone.getRegion().getRadius());
            stmt.setString(6, clanZone.getRegion().getWorld());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }






    public static Optional<ClanZone> getClanZone(String clanName) {
        ClanZone clanZone = null;

        String getClanQuery = "SELECT id, name FROM clans WHERE name = ?";
        String getZoneQuery = "SELECT * FROM clan_zones WHERE clan_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement clanStatement = connection.prepareStatement(getClanQuery)) {

            clanStatement.setString(1, clanName);

            try (ResultSet clanResult = clanStatement.executeQuery()) {
                if (clanResult.next()) {
                    int clanId = clanResult.getInt("id");
                    String name = clanResult.getString("name");

                    Clan clan = new Clan(clanId, name);

                    try (PreparedStatement zoneStatement = connection.prepareStatement(getZoneQuery)) {
                        zoneStatement.setInt(1, clanId);

                        try (ResultSet zoneResult = zoneStatement.executeQuery()) {
                            if (zoneResult.next()) {
                                int centerX = zoneResult.getInt("center_x");
                                int centerY = zoneResult.getInt("center_y");
                                int centerZ = zoneResult.getInt("center_z");
                                int radius = zoneResult.getInt("radius");
                                String worldName = zoneResult.getString("world");

                                Region region = new Region(centerX, centerY, centerZ, radius, worldName);
                                clanZone = new ClanZone(clan, region);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(clanZone);
    }

    public static void setInstance(DatabaseManager instance) {
        DatabaseManager.instance = instance;
    }

    // Метод для получения клана игрока по UUID
    public static Optional<Clan> getPlayerClan(UUID playerUuid) {
        // SQL-запрос для поиска клана по UUID игрока
        String query = "SELECT c.id, c.name, c.owner_uuid FROM clans c " +
                "JOIN clan_members cm ON c.id = cm.clan_id " +
                "WHERE cm.player_uuid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Устанавливаем параметр для запроса (UUID игрока)
            statement.setString(1, playerUuid.toString());

            // Выполняем запрос и получаем результат
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Извлекаем данные о клане
                    int clanId = resultSet.getInt("id");
                    String clanName = resultSet.getString("name");
                    UUID ownerUuid = UUID.fromString(resultSet.getString("owner_uuid"));

                    // Создаем объект Clan с полученными данными
                    Clan clan = new Clan(clanId, clanName, ownerUuid);

                    // Возвращаем клан в Optional
                    return Optional.of(clan);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Если клан не найден, возвращаем Optional.empty()
        return Optional.empty();
    }


    public List<ClanZone> getAllClanZones() {
        List<ClanZone> zones = new ArrayList<>();

        String sql = "SELECT cz.center_x, cz.center_y, cz.center_z, cz.radius, cz.world, c.name " +
                "FROM clan_zones cz " +
                "JOIN clans c ON cz.clan_id = c.id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int centerX = rs.getInt("center_x");
                int centerY = rs.getInt("center_y");
                int centerZ = rs.getInt("center_z");
                int radius = rs.getInt("radius");
                String world = rs.getString("world");
                String clanName = rs.getString("name");

                Region region = new Region(centerX, centerY, centerZ, radius, world);
                Clan clan = new Clan(clanName); // предполагаем, что тебе достаточно только имени
                ClanZone clanZone = new ClanZone(clan, region);

                zones.add(clanZone);
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Лучше логировать
        }

        return zones;
    }

    // Метод для добавления игрока в клан по его UUID
    public boolean addPlayerToClan(UUID playerUuid, int clanId) {
        String sql = "INSERT INTO clan_members (clan_id, player_uuid) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clanId);  // Устанавливаем ID клана
            stmt.setObject(2, playerUuid);  // Устанавливаем UUID игрока
            int rowsAffected = stmt.executeUpdate();  // Выполняем запрос

            return rowsAffected > 0;  // Возвращаем true, если запись была успешной
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Возвращаем false в случае ошибки
        }
    }


    // Метод для удаления игрока из клана по его UUID
    public boolean removePlayerFromClan(UUID playerUuid) {
        String sql = "DELETE FROM clan_members WHERE player_uuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, playerUuid);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Метод для получения списка UUID игроков в клане по ID клана
    public List<UUID> getClanMembers(int clanId) {
        List<UUID> members = new ArrayList<>();
        String query = "SELECT player_uuid FROM clan_members WHERE clan_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, clanId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                    members.add(playerUuid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }




}

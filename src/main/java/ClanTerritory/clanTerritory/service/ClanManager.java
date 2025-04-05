package ClanTerritory.clanTerritory.service;


import ClanTerritory.clanTerritory.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanManager {

    // Создание нового клана
    public void createClan(String clanName) {
        if (DatabaseManager.getClanZone(clanName) != null) {
            Bukkit.getLogger().info("Clan " + clanName + " already exists!");
            return;
        }

        // Сохраняем новый клан в базе данных
        DatabaseManager.saveClan(clanName);
        Bukkit.getLogger().info("Clan " + clanName + " has been created successfully.");
    }

    // Добавление игрока в клан
    public void addPlayerToClan(UUID playerUuid, String clanName) {
        // Проверяем, существует ли клан
        if (DatabaseManager.getClanZone(clanName) == null) {
            Bukkit.getLogger().info("This clan does not exist!");
            return;
        }

        // Сохраняем игрока в клан
        DatabaseManager.savePlayerClan(playerUuid, clanName);
        Bukkit.getLogger().info("Player " + playerUuid + " has been added to clan " + clanName);
    }

    // Получение клана игрока
    public String getPlayerClan(UUID playerUuid) {
        return DatabaseManager.getPlayerClan(playerUuid);
    }
}

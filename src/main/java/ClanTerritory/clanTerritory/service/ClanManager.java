package ClanTerritory.clanTerritory.service;


import ClanTerritory.clanTerritory.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanManager {

    // Создание нового клана
    public boolean createClan(Player player, String clanName) {
        if (DatabaseManager.getClanZone(clanName) != null) {
            player.sendMessage("§cClan '" + clanName + "' already exists!");
            return false;
        }

        // Проверяем, есть ли у игрока уже клан
        String existingClan = DatabaseManager.getPlayerClan(player.getUniqueId());
        if (existingClan != null) {
            player.sendMessage("§cYou are already in a clan: " + existingClan);
            return false;
        }

        // Сохраняем клан с владельцем
        DatabaseManager.saveClan(clanName, player.getUniqueId());

        // Добавляем создателя в участники
        DatabaseManager.savePlayerClan(player.getUniqueId(), clanName);

        Bukkit.getLogger().info("Clan " + clanName + " has been created by " + player.getName());
        return true;
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

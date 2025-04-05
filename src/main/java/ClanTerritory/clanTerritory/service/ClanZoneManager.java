package ClanTerritory.clanTerritory.service;

import ClanTerritory.clanTerritory.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ClanZoneManager {

    // Создание зоны для клана
    public void createClanZone(Player player, String clanName) {
        // Проверяем, существует ли клан с таким названием
        if (DatabaseManager.getClanZone(clanName) == null) {
            player.sendMessage("This clan does not exist!");
            return;
        }

        // Получаем координаты блока, на который смотрит игрок
        Block block = player.getTargetBlock(null, 5);

        // Создаем зону для клана в базе данных
        int radius = 10; // Радиус зоны
        DatabaseManager.saveClanZone(clanName, block.getX(), block.getY(), block.getZ(), radius);

        // Сохраняем клан для игрока
        DatabaseManager.savePlayerClan(player.getUniqueId(), clanName);
        player.sendMessage("The zone for your clan " + clanName + " has been created at the specified location!");
    }
}

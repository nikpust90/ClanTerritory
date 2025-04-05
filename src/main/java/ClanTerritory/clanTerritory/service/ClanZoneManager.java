package ClanTerritory.clanTerritory.service;

import ClanTerritory.clanTerritory.ClanTerritory;
import ClanTerritory.clanTerritory.DatabaseManager;
import ClanTerritory.clanTerritory.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ClanZoneManager {

    // Создание зоны для клана
    // Создание зоны для клана
    public void createClanZone(Player player, String clanName) {
        // Проверяем, существует ли клан с таким названием
        if (DatabaseManager.getClanZone(clanName) == null) {
            player.sendMessage("This clan does not exist!");
            return;
        }

        // Получаем блок, на который смотрит игрок (максимум 5 блоков вперёд)
        Block block = player.getTargetBlock(null, 5);

        // Координаты центра зоны
        int centerX = block.getX();
        int centerY = block.getY();
        int centerZ = block.getZ();
        int radius = 10; // Радиус зоны базы клана

        // Сохраняем координаты зоны в базе данных
        DatabaseManager.saveClanZone(clanName, centerX, centerY, centerZ, radius);

        // Добавляем игрока в клан, если он ещё не добавлен
        if (DatabaseManager.getPlayerClan(player.getUniqueId()) == null) {
            DatabaseManager.savePlayerClan(player.getUniqueId(), clanName);
        }

        // Сообщение об успешном создании зоны
        player.sendMessage("The zone for your clan " + clanName + " has been created at the specified location!");

        // Создаём объект региона
        Region region = new Region(centerX, centerY, centerZ, radius);

        // Подсветка границы зоны частицами
        ClanTerritory.highlightBorder(player, region);

        // Установка флагов на краях зоны
        placeFlags(region, player.getWorld());
    }

    // Метод для установки флагов по краям зоны
    private void placeFlags(Region region, World world) {
        int x1 = region.getCenterX() - region.getRadius();
        int x2 = region.getCenterX() + region.getRadius();
        int z1 = region.getCenterZ() - region.getRadius();
        int z2 = region.getCenterZ() + region.getRadius();
        int y = region.getCenterY();

        // Углы зоны
        int[][] corners = {
                {x1, z1}, {x1, z2}, {x2, z1}, {x2, z2}
        };

        // Ставим флаг на каждом углу
        for (int[] corner : corners) {
            int x = corner[0];
            int z = corner[1];
            Block flagBlock = world.getBlockAt(x, y + 1, z); // +1, чтобы поставить флаг над землей
            flagBlock.setType(Material.RED_BANNER);
        }
    }

    // Телепорт игрока на базу клана
    public void teleportToClanBase(Player player) {
        String clanName = DatabaseManager.getPlayerClan(player.getUniqueId());
        if (clanName == null) {
            player.sendMessage("You are not part of any clan!");
            return;
        }

        Region region = DatabaseManager.getClanZone(clanName);
        if (region == null) {
            player.sendMessage("Your clan doesn't have a base set yet!");
            return;
        }

        // Телепортируем игрока в центр базы
        Location baseLocation = new Location(player.getWorld(), region.getCenterX(), region.getCenterY() + 1, region.getCenterZ());
        player.teleport(baseLocation);
        player.sendMessage("Teleported to your clan base.");
    }
}

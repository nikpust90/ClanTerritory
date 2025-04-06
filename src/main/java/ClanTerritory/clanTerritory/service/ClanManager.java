package ClanTerritory.clanTerritory.service;


import ClanTerritory.clanTerritory.Clan;
import ClanTerritory.clanTerritory.ClanZone;
import ClanTerritory.clanTerritory.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class ClanManager {
    private final DatabaseManager dbManager;

    public ClanManager() {
        this.dbManager = new DatabaseManager();
    }

    // Создание нового клана без зоны
    public boolean createClan(Player player, String clanName) {
        // Проверяем существует ли клан с таким именем
        if (dbManager.getClanZone(clanName).isPresent()) {
            player.sendMessage("§cКлан '" + clanName + "' уже существует!");
            return false;
        }

        // Проверяем состоит ли игрок уже в каком-то клане
        if (dbManager.getPlayerClan(player.getUniqueId()).isPresent()) {
            player.sendMessage("§cВы уже состоите в другом клане!");
            return false;
        }

        // Создаем новый клан
        Clan newClan = new Clan(0, clanName, player.getUniqueId());
        boolean success = dbManager.saveClan(newClan); // Без зоны!

        if (success) {
            // Добавляем владельца в участники
            dbManager.addPlayerToClan(player.getUniqueId(), newClan.getId());

            Bukkit.getLogger().info("Создан новый клан " + clanName + " владельцем " + player.getName());
            player.sendMessage("§aКлан '" + clanName + "' успешно создан!");
            return true;
        } else {
            player.sendMessage("§cПроизошла ошибка при создании клана");
            return false;
        }
    }

    // Добавление игрока в клан
    public void addPlayerToClan(Player player, String clanName) {
        // Получаем данные о клане
        Optional<ClanZone> clanZone = dbManager.getClanZone(clanName);
        if (clanZone.isEmpty()) {
            player.sendMessage("§cКлан не найден!");
            return;
        }

        // Проверяем, не состоит ли игрок уже в этом клане
        Optional<Clan> currentClan = dbManager.getPlayerClan(player.getUniqueId());
        if (currentClan.isPresent() && currentClan.get().getId() == clanZone.get().getClan().getId()) {
            player.sendMessage("§cВы уже состоите в этом клане!");
            return;
        }

        // Добавляем игрока в клан
        boolean success = dbManager.addPlayerToClan(player.getUniqueId(), clanZone.get().getClan().getId());
        if (success) {
            player.sendMessage("§aВы были добавлены в клан " + clanName);
            Bukkit.getLogger().info("Игрок " + player.getName() + " добавлен в клан " + clanName);
        } else {
            player.sendMessage("§cНе удалось добавить вас в клан");
        }
    }


    // Получение информации о клане игрока
    public Optional<Clan> getPlayerClan(UUID playerUuid) {
        return dbManager.getPlayerClan(playerUuid);
    }

    // Удаление игрока из клана
    public boolean removePlayerFromClan(Player player) {
        Optional<Clan> clan = dbManager.getPlayerClan(player.getUniqueId());
        if (clan.isEmpty()) {
            player.sendMessage("§cВы не состоите ни в одном клане!");
            return false;
        }

        boolean success = dbManager.removePlayerFromClan(player.getUniqueId());
        if (success) {
            player.sendMessage("§aВы покинули клан " + clan.get().getName());
            return true;
        } else {
            player.sendMessage("§cНе удалось покинуть клан");
            return false;
        }
    }
}



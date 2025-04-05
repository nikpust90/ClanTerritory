package ClanTerritory.clanTerritory;

import ClanTerritory.clanTerritory.service.ClanManager;
import ClanTerritory.clanTerritory.service.ClanZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClanTerritory extends JavaPlugin {

    private ClanManager clanManager;
    private ClanZoneManager clanZoneManager;

    @Override
    public void onEnable() {
        // Инициализация сервисных классов
        clanManager = new ClanManager();
        clanZoneManager = new ClanZoneManager();

        // Подключаемся к базе данных и создаем таблицы
        DatabaseManager.createTables();
        getLogger().info("ClanTerritory Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ClanTerritory Plugin Disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("createClan") && sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage("Please specify a name for your clan!");
                return false;
            }

            String clanName = args[0]; // Имя клана, переданное в команде

            // Создаем клан через менеджер кланов
            clanManager.createClan(clanName);
            player.sendMessage("Your clan " + clanName + " has been created!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("createClanZone") && sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage("Please specify a clan name!");
                return false;
            }

            String clanName = args[0]; // Имя клана

            // Создаем зону для клана через менеджер зон
            clanZoneManager.createClanZone(player, clanName);
            return true;
        }

        return false;
    }
}

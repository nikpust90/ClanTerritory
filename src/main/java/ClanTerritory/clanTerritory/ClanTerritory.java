package ClanTerritory.clanTerritory;

import ClanTerritory.clanTerritory.service.ClanManager;
import ClanTerritory.clanTerritory.service.ClanZoneManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

public final class ClanTerritory extends JavaPlugin {

    private static ClanTerritory instance;

    private Region region;
    private ClanManager clanManager;
    private ClanZoneManager clanZoneManager;

    public static ClanTerritory getInstance() {
        return instance;
    }

    public static void setInstance(ClanTerritory instance) {
        ClanTerritory.instance = instance;
    }


    @Override
    public void onEnable() {
        clanManager = new ClanManager();
        clanZoneManager = new ClanZoneManager(DatabaseManager.getInstance());

        // Регистрируем команды
        getCommand("createclan").setExecutor(this);
        getCommand("createclanzone").setExecutor(this);
        getCommand("clan").setExecutor(this);
        getCommand("setclanflag").setExecutor(this);
        getCommand("teleport").setExecutor(this);

        // Слушатель защиты территории
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);

        // Создаём таблицы
        DatabaseManager.createTables();

        getLogger().info("ClanTerritory Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ClanTerritory Plugin Disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        UUID uuid = player.getUniqueId();

        switch (command.getName().toLowerCase()) {
            case "createclan":
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /createClan <name>");
                    return true;
                }

                String clanName = args[0];

                // Проверка: уже в клане?
                if (clanManager.getPlayerClan(uuid) != null) {
                    player.sendMessage("§cYou are already in a clan.");
                    return true;
                }

                // Проверка: имя занято?
                if (DatabaseManager.getClanZone(clanName) != null) {
                    player.sendMessage("§cClan with that name already exists.");
                    return true;
                }

                // Создаём клан с игроком как владельцем
                boolean created = clanManager.createClan(player, clanName);
                if (created) {
                    player.sendMessage("§aClan '" + clanName + "' created successfully! You are the leader.");
                } else {
                    player.sendMessage("§cFailed to create the clan.");
                }
                return true;

            case "createclanzone":
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /createClanZone <clanName>");
                    return true;
                }
                clanZoneManager.createOrUpdateClanZone(player);
                return true;

            case "teleport":
                if (args.length < 1) {
                    player.sendMessage("§cUsage: /teleport");
                    return true;
                }
                clanZoneManager.teleportToClanBase(player);
                return true;

            case "clan":
                if (args.length < 1) return false;
                String sub = args[0].toLowerCase();
                switch (sub) {
                    case "join":
                        if (args.length < 2) {
                            player.sendMessage("§cUsage: /clan join <name>");
                            return true;
                        }
                        if (clanManager.getPlayerClan(uuid) != null) {
                            player.sendMessage("§cYou are already in a clan.");
                            return true;
                        }
                        if (DatabaseManager.getClanZone(args[1]) == null) {
                            player.sendMessage("§cClan does not exist.");
                            return true;
                        }
                        clanManager.addPlayerToClan(player, args[1]);
                        player.sendMessage("§aYou joined clan " + args[1]);
                        return true;

                    case "leave":
                        if (clanManager.getPlayerClan(uuid) == null) {
                            player.sendMessage("§cYou are not in a clan.");
                            return true;
                        }
                        clanManager.removePlayerFromClan(player);
                        player.sendMessage("§eYou left the clan.");
                        return true;

//                    case "base":
//                        // Получаем клан игрока
//                        Optional<Clan> optionalClan = clanManager.getPlayerClan(uuid);
//                        if (optionalClan.isEmpty()) {  // Проверяем, состоит ли игрок в клане
//                            player.sendMessage("§cYou are not in a clan.");
//                            return true;
//                        }
//
//                        // Получаем название клана
//                        String clanName = optionalClan.get().getName();
//
//                        // Получаем зону клана
//                        ClanZone clanZone = clanZoneManager.getClanZone(clanName);
//                        if (clanZone == null) {
//                            player.sendMessage("§cYour clan has no base.");
//                            return true;
//                        }
//
//                        // Получаем координаты базы
//                        Region region = clanZone.getRegion();
//                        Location tp = new Location(player.getWorld(), region.getCenterX(), region.getCenterY(), region.getCenterZ());
//
//                        // Телепортируем игрока
//                        player.teleport(tp);
//                        player.sendMessage("§aTeleported to clan base.");
//                        return true;

                    default:
                        return false;
                }
        }

        return false;
    }

}

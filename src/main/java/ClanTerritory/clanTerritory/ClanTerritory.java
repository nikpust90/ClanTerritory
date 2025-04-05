package ClanTerritory.clanTerritory;

import ClanTerritory.clanTerritory.commands.CreateClanCommand;
import ClanTerritory.clanTerritory.commands.CreateClanZoneCommand;
import ClanTerritory.clanTerritory.service.ClanManager;
import ClanTerritory.clanTerritory.service.ClanZoneManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class ClanTerritory extends JavaPlugin {

    private static ClanTerritory instance;
    private ClanManager clanManager;
    private ClanZoneManager clanZoneManager;



    @Override
    public void onEnable() {
        clanManager = new ClanManager();
        clanZoneManager = new ClanZoneManager();
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
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
                clanZoneManager.createClanZone(player, args[0]);
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
                        clanManager.addPlayerToClan(uuid, args[1]);
                        player.sendMessage("§aYou joined clan " + args[1]);
                        return true;

                    case "leave":
                        if (clanManager.getPlayerClan(uuid) == null) {
                            player.sendMessage("§cYou are not in a clan.");
                            return true;
                        }
                        DatabaseManager.removePlayerFromClan(uuid);
                        player.sendMessage("§eYou left the clan.");
                        return true;

                    case "base":
                        String clan = clanManager.getPlayerClan(uuid);
                        if (clan == null) {
                            player.sendMessage("§cYou are not in a clan.");
                            return true;
                        }
                        Region region = DatabaseManager.getClanZone(clan);
                        if (region == null) {
                            player.sendMessage("§cYour clan has no base.");
                            return true;
                        }
                        Location tp = new Location(player.getWorld(), region.getCenterX(), region.getCenterY(), region.getCenterZ());
                        player.teleport(tp);
                        player.sendMessage("§aTeleported to clan base.");
                        return true;

                    default:
                        return false;
                }
        }



        return false;
    }

    // Метод подсветки границы территории
    public static void highlightBorder(Player player, Region region) {
        World world = player.getWorld();
        int centerX = region.getCenterX();
        int centerY = region.getCenterY();
        int centerZ = region.getCenterZ();
        int radius = region.getRadius();

        // Подсветка границы зоны по окружности
        for (int angle = 0; angle < 360; angle += 10) { // Шаг 10 градусов для частиц
            double radian = Math.toRadians(angle);
            double x = centerX + radius * Math.cos(radian);
            double z = centerZ + radius * Math.sin(radian);

            // Частицы на границе зоны
            world.spawnParticle(Particle.REDSTONE, x, centerY, z, 1, new Particle.DustOptions(Color.RED, 1)); // Можно выбрать другой цвет или тип частиц
        }
    }
}

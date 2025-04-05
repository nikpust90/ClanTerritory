package ClanTerritory.clanTerritory.commands;

import ClanTerritory.clanTerritory.service.ClanZoneManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateClanZoneCommand implements CommandExecutor {

    private final ClanZoneManager clanZoneManager;

    public CreateClanZoneCommand(ClanZoneManager clanZoneManager) {
        this.clanZoneManager = clanZoneManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length < 1) {
            player.sendMessage("§cPlease specify a clan name!");
            return false;
        }

        String clanName = args[0];
        clanZoneManager.createClanZone(player, clanName);
        player.sendMessage("§aZone for clan '" + clanName + "' has been created!");
        return true;
    }
}

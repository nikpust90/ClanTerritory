package ClanTerritory.clanTerritory.commands;

import ClanTerritory.clanTerritory.service.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateClanCommand implements CommandExecutor {

    private final ClanManager clanManager;

    public CreateClanCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length < 1) {
            player.sendMessage("§cPlease specify a name for your clan!");
            return false;
        }

        String clanName = args[0];
        boolean created = clanManager.createClan(player, clanName);
        if (created) {
            player.sendMessage("§aYour clan '" + clanName + "' has been created!");
        } else {
            player.sendMessage("§cClan with that name already exists or you already have a clan.");
        }
        return true;
    }
}

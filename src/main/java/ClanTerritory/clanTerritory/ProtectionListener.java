package ClanTerritory.clanTerritory;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class ProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String playerClan = DatabaseManager.getPlayerClan(uuid);
        if (playerClan == null) return;

        Region region = DatabaseManager.getClanZone(playerClan);
        if (region == null) return;

        Location loc = event.getBlock().getLocation();
        if (region.isInside(loc)) {
            if (!playerClan.equals(DatabaseManager.getPlayerClan(uuid))) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot break blocks in this clan territory!");
            }
        }
    }
}

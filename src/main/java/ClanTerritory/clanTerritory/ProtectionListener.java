package ClanTerritory.clanTerritory;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;
import java.util.UUID;

public class ProtectionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Получаем клан игрока
        Optional<Clan> playerClanOpt = DatabaseManager.getPlayerClan(playerUUID);
        if (playerClanOpt.isEmpty()) return;
        Clan playerClan = playerClanOpt.get();

        // Получаем зону клана
        Optional<ClanZone> clanZoneOpt = DatabaseManager.getClanZone(playerClan.getName());
        if (clanZoneOpt.isEmpty()) return;
        ClanZone clanZone = clanZoneOpt.get();

        // Проверяем, находится ли блок в зоне
        Location blockLocation = event.getBlock().getLocation();
        if (!clanZone.contains(blockLocation)) return;

        // Проверка — игрок из этого ли клана
        if (playerClan.getId() != clanZone.getClan().getId()) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете ломать блоки на территории другого клана!");
        }
    }
}



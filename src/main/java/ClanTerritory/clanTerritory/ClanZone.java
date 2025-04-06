package ClanTerritory.clanTerritory;

import org.bukkit.Location;
import org.bukkit.World;

public class ClanZone {
    private final Clan clan;
    private final Region region;

    public ClanZone(Clan clan, Region region) {
        this.clan = clan;
        this.region = region;
    }

    public Clan getClan() {
        return clan;
    }

    public Region getRegion() {
        return region;
    }

    // Метод для получения центра зоны
    public Location getCenterLocation(World world) {
        return new Location(world, region.getCenterX(), region.getCenterY(), region.getCenterZ());
    }

    // Метод для проверки, входит ли точка в зону
    public boolean contains(Location location) {
        return region.contains(location);
    }
}

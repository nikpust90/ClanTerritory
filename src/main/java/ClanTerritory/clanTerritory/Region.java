package ClanTerritory.clanTerritory;


import org.bukkit.Location;

public class Region {
    private final int centerX, centerY, centerZ, radius;

    public Region(int x, int y, int z, int radius) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.radius = radius;
    }

    public int getCenterX() { return centerX; }
    public int getCenterY() { return centerY; }
    public int getCenterZ() { return centerZ; }

    public int getRadius() {
        return radius;
    }

    public boolean isInside(Location loc) {
        double dx = centerX - loc.getX();
        double dy = centerY - loc.getY();
        double dz = centerZ - loc.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius;
    }
}


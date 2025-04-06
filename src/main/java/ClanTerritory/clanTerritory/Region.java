package ClanTerritory.clanTerritory;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

/**
 * Представляет регион (зону) клана в мире
 * @param centerX Центральная координата X
 * @param centerY Центральная координата Y
 * @param centerZ Центральная координата Z
 * @param radius Радиус зоны
 * @param worldName Название мира
 */
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Region {

    private int centerX;
    private int centerY;
    private int centerZ;
    private int radius;
    private String worldName;

    public Region(int centerX, int centerY, int centerZ, int radius, String worldName) {
    }

    public int getMinX() { return centerX - radius; }
    public int getMaxX() { return centerX + radius; }
    public int getMinZ() { return centerZ - radius; }
    public int getMaxZ() { return centerZ + radius; }
    public int getMinY() { return centerY - radius; }
    public int getMaxY() { return centerY + radius; }

    /**
     * Проверяет, содержит ли регион указанные координаты
     */
    public boolean contains(int x, int y, int z, String world) {
        return worldName.equals(world) &&
                x >= getMinX() && x <= getMaxX() &&
                z >= getMinZ() && z <= getMaxZ() &&
                y >= getMinY() && y <= getMaxY();
    }

    /**
     * Проверяет, содержит ли регион указанную локацию
     */
    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) return false;
        return contains(location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName());
    }

    /**
     * Преобразует регион в BoundingBox для проверки столкновений
     */
    public BoundingBox toBoundingBox() {
        return new BoundingBox(
                getMinX(), getMinY(), getMinZ(),
                getMaxX(), getMaxY(), getMaxZ()
        );
    }

    /**
     * Получает центральную локацию региона
     */
    public Location getCenterLocation(World world) {
        return new Location(world, centerX + 0.5, centerY + 0.5, centerZ + 0.5);
    }

    /**
     * Возвращает все блоки по границе региона
     */
    public Set<Block> getBorderBlocks(World world) {
        Set<Block> blocks = new HashSet<>();
        if (world == null || !world.getName().equals(worldName)) return blocks;

        // Границы по X
        for (int x = getMinX(); x <= getMaxX(); x++) {
            for (int y = getMinY(); y <= getMaxY(); y++) {
                blocks.add(world.getBlockAt(x, y, getMinZ()));
                blocks.add(world.getBlockAt(x, y, getMaxZ()));
            }
        }

        // Границы по Z (исключая уже добавленные углы)
        for (int z = getMinZ() + 1; z < getMaxZ(); z++) {
            for (int y = getMinY(); y <= getMaxY(); y++) {
                blocks.add(world.getBlockAt(getMinX(), y, z));
                blocks.add(world.getBlockAt(getMaxX(), y, z));
            }
        }

        return blocks;
    }

    /**
     * Визуализирует границы региона частицами
     */
    public void displayBorder(Player player, Particle particle, int count) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        for (int x = getMinX(); x <= getMaxX(); x++) {
            for (int z = getMinZ(); z <= getMaxZ(); z++) {
                if (x == getMinX() || x == getMaxX() || z == getMinZ() || z == getMaxZ()) {
                    for (int y = getMinY(); y <= getMaxY(); y++) {
                        player.spawnParticle(particle,
                                new Location(world, x + 0.5, y + 0.5, z + 0.5),
                                count);
                    }
                }
            }
        }
    }

    /**
     * Проверяет пересечение с другим регионом
     */
    public boolean intersects(Region other) {
        if (!worldName.equals(other.getWorld())) return false;

        return getMinX() <= other.getMaxX() &&
                getMaxX() >= other.getMinX() &&
                getMinY() <= other.getMaxY() &&
                getMaxY() >= other.getMinY() &&
                getMinZ() <= other.getMaxZ() &&
                getMaxZ() >= other.getMinZ();
    }

    /**
     * Возвращает случайную точку внутри региона
     */
    public Location getRandomLocation(World world) {
        if (world == null || !world.getName().equals(worldName)) return null;

        int x = getMinX() + (int)(Math.random() * (radius * 2 + 1));
        int y = getMinY() + (int)(Math.random() * (radius * 2 + 1));
        int z = getMinZ() + (int)(Math.random() * (radius * 2 + 1));

        return new Location(world, x + 0.5, y + 0.5, z + 0.5);
    }

    /**
     * Возвращает вектор от центра к указанной точке
     */
    public Vector getDirectionFromCenter(Location location) {
        if (!contains(location)) return null;
        return new Vector(
                location.getX() - centerX,
                location.getY() - centerY,
                location.getZ() - centerZ
        ).normalize();
    }

    /**
     * Проверяет, находится ли точка на границе региона
     */
    public boolean isOnBorder(Location location) {
        if (!contains(location)) return false;

        int x = location.getBlockX();
        int z = location.getBlockZ();

        return x == getMinX() || x == getMaxX() ||
                z == getMinZ() || z == getMaxZ();
    }

    public int getCenterY() {
        return centerY;
    }
    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public int getRadius() {
        return radius;
    }


    public String getWorld() {
        return worldName;
    }
}

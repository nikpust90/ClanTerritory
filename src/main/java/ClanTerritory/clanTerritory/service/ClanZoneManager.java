package ClanTerritory.clanTerritory.service;

import ClanTerritory.clanTerritory.*;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ClanZoneManager {
    private final DatabaseManager dbManager;
    private final Material FLAG_MATERIAL = Material.RED_BANNER;
    private final Material BORDER_MATERIAL = Material.GLOWSTONE;

    private final int RADIUS = 15;
    private final int FLAG_HEIGHT_OFFSET = 2;
    private final int VISUALIZATION_DURATION = 20 * 30; // 30 секунд в тиках

    public ClanZoneManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Создает или обновляет зону клана
     *
     * @param player Игрок-владелец клана
     */
    public void createOrUpdateClanZone(Player player) {
        // Проверка прав и валидация
        if (!validateZoneCreation(player, RADIUS)) {
            return;
        }

        // Создание региона
        Region region = createRegionFromPlayerLook(player, RADIUS);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Не удалось определить позицию для зоны!");
            return;
        }

        // Проверка пересечений с другими зонами
        if (hasZoneCollisions(region, player)) {
            return;
        }

        // Получаем клан игрока
        Clan clan = dbManager.getPlayerClan(player.getUniqueId()).get();

        // Создаем объект зоны
        ClanZone clanZone = new ClanZone(clan, region);

        // Сохраняем в БД
        if (dbManager.saveClanZone(clanZone)) {
            visualizeZone(player, region);
            player.sendMessage(ChatColor.GREEN + "Зона клана успешно установлена!");
            return;
        }

        player.sendMessage(ChatColor.RED + "Ошибка при сохранении зоны!");
    }

    /**
     * Визуализирует зону для игрока
     */
    public void visualizeZone(Player player, Region region) {
        World world = Bukkit.getWorld(region.getWorld());
        if (world == null) return;

        // 1. Подсветка границ частицами
        region.displayBorder(player, Particle.GLOW, 1);

        // 2. Установка временных блоков по границе
        setTemporaryBorderBlocks(world, region);

        // 3. Установка флагов по углам
        placeClanFlags(world, region);  // Добавляем вызов для установки флагов

        // 4. Запланировать удаление временных блоков
        Bukkit.getScheduler().runTaskLater(
                ClanTerritory.getInstance(),
                () -> removeTemporaryBorderBlocks(world, region),
                VISUALIZATION_DURATION
        );
    }

    /**
     * Телепортирует игрока на базу его клана
     */
    public void teleportToClanBase(Player player) {
        Optional<ClanZone> clanZoneOpt = getPlayerClanZone(player);

        if (clanZoneOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Вы не состоите в клане или база не установлена!");
            return;
        }

        ClanZone clanZone = clanZoneOpt.get();
        Region region = clanZone.getRegion();
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Ваш клан еще не установил базу!");
            return;
        }

        World world = Bukkit.getWorld(region.getWorld());
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Мир базы недоступен!");
            return;
        }

        Location baseLocation = region.getCenterLocation(world);
        baseLocation.setYaw(player.getLocation().getYaw());
        baseLocation.setPitch(player.getLocation().getPitch());

        player.teleport(baseLocation);
        player.sendMessage(ChatColor.GREEN + "Телепортирован на базу клана!");
    }



    // ========== Внутренние методы ==========

    private boolean validateZoneCreation(Player player, int radius) {
        // Проверка радиуса
        if (radius < 5 || radius > 50) {
            player.sendMessage(ChatColor.RED + "Радиус зоны должен быть от 5 до 50 блоков!");
            return false;
        }

        // Проверка прав игрока
        Optional<Clan> clanOpt = dbManager.getPlayerClan(player.getUniqueId());
        if (clanOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Вы не состоите в клане!");
            return false;
        }

        if (!clanOpt.get().isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Только владелец клана может создавать зону!");
            return false;
        }

        return true;
    }

    private Region createRegionFromPlayerLook(Player player, int radius) {
        Block targetBlock = player.getTargetBlock(null, 10);
        if (targetBlock == null) return null;

        return new Region(
                targetBlock.getX(),
                targetBlock.getY(),
                targetBlock.getZ(),
                radius,
                player.getWorld().getName()
        );
    }

    private boolean hasZoneCollisions(Region newRegion, Player player) {
        for (ClanZone clanZone : dbManager.getAllClanZones()) {
            if (clanZone.getRegion() != null &&
                    newRegion.intersects(clanZone.getRegion())) {
                player.sendMessage(ChatColor.RED +
                        "Зона пересекается с зоной клана " + clanZone.getClan().getName() + "!");
                return true;
            }
        }
        return false;
    }

    private void setTemporaryBorderBlocks(World world, Region region) {
        Material borderMaterial = Material.STONE; // замените на нужный материал для границ

        // Устанавливаем блоки по границе зоны
        for (Block borderBlock : region.getBorderBlocks(world)) {
            if (borderBlock.getType() == Material.AIR) {
                borderBlock.setType(borderMaterial);
            }
        }

        // Устанавливаем флаги на углы
//        setFlagsOnCorners(world, region);
    }

    private void removeTemporaryBorderBlocks(World world, Region region) {
        for (Block borderBlock : region.getBorderBlocks(world)) {
            if (borderBlock.getType() == BORDER_MATERIAL) {
                borderBlock.setType(Material.AIR);
            }
        }
    }

    /**
     * Устанавливает флаги по углам зоны
     */
    private void placeClanFlags(World world, Region region) {
        // Получаем координаты центра зоны
        Location center = region.getCenterLocation(world);

        // Вычисление углов зоны на основе радиуса
        int radius = region.getRadius();
        int xMin = center.getBlockX() - radius;
        int xMax = center.getBlockX() + radius;
        int zMin = center.getBlockZ() - radius;
        int zMax = center.getBlockZ() + radius;

        // Массив для хранения всех углов (которые будут 4: левый верхний, правый верхний, левый нижний, правый нижний)
        Location[] corners = new Location[]{
                new Location(world, xMin, center.getBlockY(), zMin), // Левый верхний угол
                new Location(world, xMax, center.getBlockY(), zMin), // Правый верхний угол
                new Location(world, xMin, center.getBlockY(), zMax), // Левый нижний угол
                new Location(world, xMax, center.getBlockY(), zMax)  // Правый нижний угол
        };

        // Размещение флагов на углах
        for (Location corner : corners) {
            placeClanFlag(world, corner, "Клан");
        }
    }

    /**
     * Размещает флаг на заданной позиции
     */
    private void placeClanFlag(World world, Location corner, String clanName) {
        // Установка флага на заборе
        corner.getBlock().setType(Material.OAK_FENCE);

        // Размещение баннера на верхней части забора
        Location bannerLocation = corner.clone().add(0, 1, 0);
        bannerLocation.getBlock().setType(Material.BLACK_BANNER);

        // Кастомизация баннера
        BlockState state = bannerLocation.getBlock().getState();
        if (state instanceof Banner) {
            Banner banner = (Banner) state;
            banner.setBaseColor(DyeColor.RED); // основной цвет

            // Добавим узоры
            banner.addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_DOWNRIGHT));
            banner.addPattern(new Pattern(DyeColor.BLUE, PatternType.BORDER));

            banner.setCustomName("Флаг клана " + clanName);
            banner.update();
        }
    }

    private Optional<ClanZone> getPlayerClanZone(Player player) {
        return dbManager.getPlayerClan(player.getUniqueId())
                .flatMap(clan -> dbManager.getClanZone(clan.getName()));
    }
}


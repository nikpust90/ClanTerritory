package ClanTerritory.clanTerritory;

import java.util.UUID;


public class Clan {
    private int id;
    private String name;
    private UUID ownerUuid;

    public Clan(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Clan(int id, String name, UUID ownerUuid) {
        this.id = id;
        this.name = name;
        this.ownerUuid = ownerUuid;
    }

    public Clan(String clanName) {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    /**
     * Проверяет, является ли игрок владельцем этого клана
     * @param playerUuid UUID игрока для проверки
     * @return true если игрок является владельцем
     */
    public boolean isOwner(UUID playerUuid) {
        return ownerUuid.equals(playerUuid);
    }

    /**
     * Проверяет валидность названия клана
     * @param name Название для проверки
     * @return true если название валидно
     */
    public static boolean isValidName(String name) {
        return name != null &&
                name.length() >= 3 &&
                name.length() <= 16 &&
                name.matches("[a-zA-Z0-9_]+");
    }
}


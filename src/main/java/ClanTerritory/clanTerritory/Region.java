package ClanTerritory.clanTerritory;



public class Region {
    private int centerX;
    private int centerY;
    private int centerZ;
    private int radius;

    // Конструктор
    public Region(int centerX, int centerY, int centerZ, int radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
    }

    // Геттеры и сеттеры
    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(int centerZ) {
        this.centerZ = centerZ;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}


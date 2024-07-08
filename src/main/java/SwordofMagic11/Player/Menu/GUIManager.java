package SwordofMagic11.Player.Menu;

import SwordofMagic11.Component.SomTask;
import SwordofMagic11.Custom.CustomItemStack;
import SwordofMagic11.Player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class GUIManager {

    public static int Digit = 7;
    public static CustomItemStack ItemFlame = new CustomItemStack(Material.IRON_BARS).setNonDecoDisplay(" ");

    protected final PlayerData playerData;
    private CustomItemStack[] container;
    private String display;
    private int row;
    protected Inventory inventory;

    public GUIManager(PlayerData playerData, String display, int row) {
        playerData.registerGUIManager(this);
        this.playerData = playerData;
        this.display = display;
        this.row = row;
        inventoryInit();
        containerInit();
    }

    public PlayerData playerData() {
        return playerData;
    }

    public void inventoryInit() {
        inventory = Bukkit.createInventory(playerData.getPlayer(), row * 9, display);
    }

    public void containerInit() {
        container = new CustomItemStack[row * 9];
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
        inventoryInit();
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
        inventoryInit();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setItem(int slot, CustomItemStack itemStack) {
        container[slot] = itemStack;
    }

    public void setItem(int x, int y, CustomItemStack itemStack) {
        setItem(x + y * 9, itemStack);
    }

    public int[] getSlot(int slot) {
        return new int[]{slot % 9, slot/9};
    }

    public int getSlot(int x, int y) {
        return x+y*9;
    }

    public CustomItemStack getItem(int slot) {
        return container[slot];
    }

    public CustomItemStack getItem(int x, int y) {
        return getItem(x + y * 9);
    }

    public void open() {
        Player player = playerData.getPlayer();
        player.setItemOnCursor(null);
        SomTask.sync(() -> player.openInventory(inventory));
        update();
    }

    public abstract void updateContainer();

    public void update() {
        SomTask.async(() -> {
            containerInit();
            updateContainer();
            inventory.setContents(container);
        });
    }

    public void fastUpdate(CustomItemStack[] content) {
        inventory.setContents(content);
    }

    public void fastUpdate() {
        inventory.setContents(container);
    }

    public abstract void topClick(int slot, ItemStack clickedItem, ClickType clickType);

    public abstract void bottomClick(int slot, ItemStack clickedItem, ClickType clickType);

    public abstract void close();

    public static boolean isInvalidSlot(int slot) {
        return slot == 8 || slot == 17 || slot == 26 || slot == 35 || slot == 44 || slot == 53;
    }
}

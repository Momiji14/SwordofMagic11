package SwordofMagic11.Player.Shop;

import java.util.ArrayList;
import java.util.List;

public class ShopData {

    private String display;
    private final List<Container> containers = new ArrayList<>();

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void addContainer(Container container) {
        containers.add(container);
    }

    public record Container(String itemId, int mel, List<Recipe> recipe, int slot) {
    }

    public record Recipe(String materialId, int amount) {

    }
}

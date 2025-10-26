package isep.ipp.pt.g322.model;

import java.util.*;

public class Wagon {

    private final String wagonId;
    private final List<Box> boxes = new ArrayList<>();

    public Wagon(String wagonId) { this.wagonId = wagonId; }

    public String getWagonId() { return wagonId; }
    public List<Box> getBoxes() { return boxes; }

    public void addBox(Box box) { boxes.add(box); }

    @Override
    public String toString() {
        return "Wagon " + wagonId + " (" + boxes.size() + " boxes)";
    }
}

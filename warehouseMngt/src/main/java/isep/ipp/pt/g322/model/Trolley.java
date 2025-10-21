package isep.ipp.pt.g322.model;

import java.util.ArrayList;
import java.util.List;

public class Trolley {
    private int trolleyID;
    private double capacity;
    private double usedWeight;
    private List<AllocationFragment> fragments;

    public Trolley(int trolleyID, double capacity) {
        this.trolleyID = trolleyID;
        this.capacity = capacity;
        this.usedWeight = 0.0;
        this.fragments = new ArrayList<>();
    }

    public int getTrolleyID() {
        return trolleyID;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getUsedWeight() {
        return usedWeight;
    }

    public List<AllocationFragment> getAllocations() {
        return fragments;
    }

    public void setTrolleyID(int trolleyID) {
        this.trolleyID = trolleyID;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public void setUsedWeight(double usedWeight) {
        this.usedWeight = usedWeight;
    }

    public void setFragments(List<AllocationFragment> fragments) {
        this.fragments = fragments;
    }

    public boolean canFit(double weight) {
        return (usedWeight + weight) <= capacity;
    }

    public void addFragment(AllocationFragment fragment) {
        fragments.add(fragment);
        usedWeight += fragment.getWeightPlaced();
    }

    public double getRemainingCapacity() {
        return capacity - usedWeight;
    }

    public double getUtilization() {
        return (usedWeight / capacity)*100.0;
    }
}

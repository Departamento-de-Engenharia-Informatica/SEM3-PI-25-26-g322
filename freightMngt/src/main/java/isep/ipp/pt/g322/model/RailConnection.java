package isep.ipp.pt.g322.model;

import java.util.Objects;

/**
 * Represents a railway connection between two stations.
 * Contains distance, capacity, and cost metrics.
 * Cost can be negative to represent preferred routes.
 *
 */
public class RailConnection {
    private final double distance;
    private final int capacity;
    private final double cost;

    /**
     * Creates a new RailConnection
     *
     * @param distance distance in kilometers (must be >= 0)
     * @param capacity maximum flow of trains per day (must be >= 0)
     * @param cost combined metric including distance, capacity, congestion, penalties/bonuses (can be negative)
     */
    public RailConnection(double distance, int capacity, double cost) {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }

        this.distance = distance;
        this.capacity = capacity;
        this.cost = cost;
    }

    public double getDistance() {
        return distance;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("RailConnection{distance=%.2f km, capacity=%d trains/day, cost=%.2f}",
                distance, capacity, cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RailConnection that = (RailConnection) o;
        return Double.compare(that.distance, distance) == 0 &&
                capacity == that.capacity &&
                Double.compare(that.cost, cost) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance, capacity, cost);
    }
}
package isep.ipp.pt.g322.model;

import java.util.Map;

public class KDTree2Stats {
    public final int size;
    public final int height;
    public final Map<Integer, Integer> bucketDistribution;

    public KDTree2Stats(int size, int height, Map<Integer, Integer> bucketDistribution) {
        this.size = size;
        this.height = height;
        this.bucketDistribution = bucketDistribution;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Size: ").append(size).append("\n");
        sb.append("Height: ").append(height).append("\n");
        sb.append("Bucket Distribution: ").append(bucketDistribution);
        return sb.toString();
    }
}

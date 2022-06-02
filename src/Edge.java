import java.io.Serializable;
import java.util.Objects;

public class Edge<T> implements Serializable {
    private final T destination;
    private final String roadName;
    private int weight;

    public Edge(T destination, String name, int weight) {
        this.destination = destination;
        this.roadName = name;
        this.weight = weight;
    }

    public T getDestination() {
        return destination;
    }

    public String getName() {
        return roadName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException();
        }
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, roadName, weight);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Edge edge) {
            return Objects.equals(destination, edge.destination) && Objects.equals(roadName, edge.roadName);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("to %s by %s takes %d", destination, roadName, weight);
    }
}
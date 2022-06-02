import java.io.Serializable;
import java.util.*;

public class ListGraph<T> implements Graph<T>, Serializable {
    private final Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    public void add(T node) {
        nodes.putIfAbsent(node, new HashSet<>());
    }

    public void remove(T node) {
        if (!nodes.containsKey(node)) {
            throw new NoSuchElementException();
        }

        for (Edge<T> edge : new HashSet<>(getEdgesFrom(node))) {
            disconnect(node, edge.getDestination());
        }
        nodes.remove(node);
    }

    public Set<T> getNodes() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    public List<Edge<T>> getPath(T from, T to) {
        Map<T, T> connection = new HashMap<>();

        getConnections(from, null, connection);

        if (!connection.containsKey(to)) {
            return null;
        }

        return gatherPath(from, to, connection);
    }

    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> connection) {
        LinkedList<Edge<T>> path = new LinkedList<>();

        T current = to;

        while (!current.equals(from)) {
            T next = connection.get(current);
            Edge<T> edge = getEdgeBetween(next, current);

            path.addFirst(edge);

            current = next;
        }

        return Collections.unmodifiableList(path);
    }

    private void getConnections(T to, T from, Map<T, T> connection) {
        connection.put(to, from);

        for (Edge<T> edge : nodes.get(to)) {
            if (!connection.containsKey(edge.getDestination())) {
                getConnections(edge.getDestination(), to, connection);
            }
        }
    }

    public boolean pathExists(T a, T b) {
        if (!nodes.containsKey(a) || !nodes.containsKey(b)) {
            return false;
        }

        Set<T> visited = new HashSet<>();
        visitPaths(a, visited);

        return visited.contains(b);
    }

    private void visitPaths(T current, Set<T> visited) {
        visited.add(current);

        for (Edge<T> edge : nodes.get(current)) {
            if (!visited.contains(edge.getDestination())) {
                visitPaths(edge.getDestination(), visited);
            }
        }
    }

    public void setConnectionWeight(T a, T b, int weight) {
        if (!nodes.containsKey(a) || !nodes.containsKey(b) || getEdgeBetween(a, b) == null
                || getEdgeBetween(b, a) == null) {
            throw new NoSuchElementException();
        }

        getEdgeBetween(a, b).setWeight(weight);
        getEdgeBetween(b, a).setWeight(weight);
    }

    public void disconnect(T a, T b) {
        if (!nodes.containsKey(a) || !nodes.containsKey(b)) {
            throw new NoSuchElementException();
        }

        if (getEdgeBetween(a, b) == null || getEdgeBetween(b, a) == null) {
            throw new IllegalStateException();
        }

        nodes.get(a).remove(getEdgeBetween(a, b));
        nodes.get(b).remove(getEdgeBetween(b, a));
    }

    public Set<Edge<T>> getEdgesFrom(T node) {
        if (!nodes.containsKey(node)) {
            throw new NoSuchElementException();
        }

        return Collections.unmodifiableSet(nodes.get(node));
    }

    public void connect(T a, T b, String name, int weight) {
        if (!nodes.containsKey(a) || !nodes.containsKey(b)) {
            throw new NoSuchElementException();
        }

        if (weight < 0) {
            throw new IllegalArgumentException();
        }

        if (getEdgeBetween(a, b) != null || getEdgeBetween(b, a) != null) {
            throw new IllegalStateException();
        }

        nodes.get(a).add(new Edge<>(b, name, weight));
        nodes.get(b).add(new Edge<>(a, name, weight));
    }

    public Edge<T> getEdgeBetween(T from, T to) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            throw new NoSuchElementException();
        }

        for (Edge<T> edge : nodes.get(from)) {
            if (edge.getDestination().equals(to)) {
                return edge;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (T node : nodes.keySet()) {
            sb.append(node.toString()).append(" {\n");

            for (Edge<T> edge : nodes.get(node)) {
                sb.append("- ").append(edge.toString()).append("\n");
            }
            sb.append("}\n\n");
        }

        return sb.toString();
    }
}

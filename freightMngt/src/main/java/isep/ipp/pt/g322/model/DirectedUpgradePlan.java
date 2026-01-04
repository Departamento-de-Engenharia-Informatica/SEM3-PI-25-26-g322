package isep.ipp.pt.g322.model;

import isep.ipp.pt.g322.datastructures.graph.Edge;
import isep.ipp.pt.g322.datastructures.graph.TopologicalSorting;

import java.util.List;

public class DirectedUpgradePlan {

    private final RailwayNetwork network;

    /**
     * Creates a new upgrade plan analyzer.
     *
     * @param network the railway network (must be directed as per requirements)
     */
    public DirectedUpgradePlan(RailwayNetwork network) {
        if (network == null) {
            throw new IllegalArgumentException("Network cannot be null");
        }
        if (!network.getGraph().isDirected()) {
            throw new IllegalArgumentException("Upgrade plan requires a directed network");
        }
        this.network = network;
    }

    /**
     * Computes the upgrade plan ordering or detects cycles.
     *
     * @return TopologicalResult containing the upgrade order or cycle information
     */
    public TopologicalSorting.TopologicalResult<Station> computeUpgradePlan() {
        return TopologicalSorting.topologicalSort(network.getGraph());
    }

    /**
     * Prints the upgrade plan results in a formatted way.
     *
     * @param result the topological sorting result
     */
    public void printUpgradePlan(TopologicalSorting.TopologicalResult<Station> result) {
        if (!result.hasCycles()) {
            // graph without cycles - print order
            System.out.println("✓ No cycles detected - Valid upgrade ordering exists\n");
            System.out.println("Upgrade Order (stations must be upgraded in this sequence):");
            System.out.println("─────────────────────────────────────────────────────────");

            List<Station> order = result.getOrderedVertices();
            for (int i = 0; i < order.size(); i++) {
                Station station = order.get(i);
                System.out.printf("%4d. [%s] %s%n",
                        i + 1,
                        station.getStationId(),
                        station.getName()
                );
            }

            System.out.println("\nTotal stations: " + order.size());

        } else {
            // graph with cycles - print cycle information
            System.out.println("✗ Cycles detected - Cannot create valid upgrade ordering\n");
            System.out.println("Stations involved in cycles:");
            System.out.println("─────────────────────────────────────────────────────────");

            List<Station> cycleStations = result.getCycleVertices();
            for (Station station : cycleStations) {
                System.out.printf("  [%s] %s%n",
                        station.getStationId(),
                        station.getName()
                );
            }

            System.out.println("\nEdges involved in cycles:");
            System.out.println("─────────────────────────────────────────────────────────");

            List<Edge<Station, ?>> cycleEdges = result.getCycleEdges();
            for (Edge<Station, ?> edge : cycleEdges) {
                System.out.printf("  [%s] %s  →  [%s] %s%n",
                        edge.getVOrig().getStationId(),
                        edge.getVOrig().getName(),
                        edge.getVDest().getStationId(),
                        edge.getVDest().getName()
                );
            }

            System.out.println("\nTotal stations in cycles: " + cycleStations.size());
            System.out.println("Total edges in cycles: " + cycleEdges.size());
        }

        // temporal complexity analysis
        System.out.println("\nAnalise Temporal:");
        System.out.println("---------------------------------------------------------");
        System.out.printf("Tempo de computacao: %.3f ms%n", result.getComputationTimeMillis());
        System.out.printf("Complexidade temporal: O(V + E) = O(%d + %d) = O(%d)%n",
                network.getNumStations(),
                network.getNumConnections(),
                network.getNumStations() + network.getNumConnections()
        );
        System.out.println("  Explicacao: O algoritmo de Kahn processa cada vertice uma vez (V operacoes)");
        System.out.println("  e examina cada aresta uma vez (E operacoes), resultando em O(V + E) total.");
        System.out.printf("Complexidade espacial: O(V) = O(%d)%n", network.getNumStations());
        System.out.println("  Explicacao: Armazena contagem de grau de entrada e fila para vertices.");
        System.out.println("Algoritmo: Algoritmo de Kahn (ordenacao topologica baseada em BFS)");
    }
}

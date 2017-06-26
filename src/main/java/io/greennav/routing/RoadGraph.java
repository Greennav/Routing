package io.greennav.routing;

import de.topobyte.osm4j.core.model.impl.Node;
import io.greennav.persistence.Persistence;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.specifics.Specifics;
import java.util.HashSet;
import java.util.Set;

public class RoadGraph extends SimpleDirectedWeightedGraph<Node, RoadEdge> {
    private final Persistence persistence;
    private final NodeWeightFunction nodeWeightFunction;
    private final Set<Node> cachedNeighbors = new HashSet<>();

    RoadGraph(Persistence persistence, NodeWeightFunction nodeWeightFunction) {
        super(RoadEdge.class);
        this.persistence = persistence;
        this.nodeWeightFunction = nodeWeightFunction;
    }

    void resetCache() {
        cachedNeighbors.clear();
    }

    void cacheNeighborsIfAbsent(Node node) {
        if (!cachedNeighbors.contains(node)) {
            final Set<Node> neighbors = persistence.getNeighbors(node);
            neighbors.forEach(neighbor -> {
                this.addVertex(neighbor);
                final RoadEdge edge = addEdge(node, neighbor);
                final double weight = nodeWeightFunction.apply(node, neighbor);
                setEdgeWeight(edge, weight);
            });
            cachedNeighbors.add(node);
        }
    }

    @Override
    protected Specifics<Node, RoadEdge> createSpecifics() {
        return new RoadGraphSpecifics(this);
    }
}

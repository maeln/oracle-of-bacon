package com.serli.oracle.of.bacon.repository;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;

import java.util.LinkedList;
import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;


public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "master"));
    }

	public String getConnectionsToKevinBaconToJson(String actorName) {
		List<GraphItem> path = getConnectionsToKevinBacon(actorName);
		StringBuffer json = new StringBuffer("[");
		json.append(path.get(0));
		for(int i=1; i<path.size(); ++i) {
			json.append(", ");
			json.append(path.get(i));
		}
		json.append("]");
		return json.toString();
	}

    public List<GraphItem> getConnectionsToKevinBacon(String actorName) {
	    LinkedList<GraphItem> path = new LinkedList<>();
        Session session = driver.session();

	    StatementResult result = session.run("MATCH p=shortestPath(" +
			    "(k:Actor {name: \"Bacon, Kevin (I)\"})-[*]-(d:Actor {name: {actor}})) RETURN p",
			    parameters( "actor", actorName));

	    Record r = result.next();
	    Path p = r.get("p").asPath();
		for(Node node : p.nodes())
			path.add(new GraphNode(node.id(),
					node.containsKey("title") ? node.get("title").asString() : node.get("name").asString(),
					node.containsKey("title") ? "Movie" : "Actor"));
	    for(Relationship rel : p.relationships())
	        path.add(new GraphEdge(rel.id(), rel.startNodeId(), rel.endNodeId(), rel.type()));

	    session.close();
        return path;
    }

    private static abstract class GraphItem {
        public final long id;

        private GraphItem(long id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphItem graphItem = (GraphItem) o;

            return id == graphItem.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class GraphNode extends GraphItem {
        public final String type;
        public final String value;

        public GraphNode(long id, String value, String type) {
            super(id);
            this.value = value;
            this.type = type;
        }

	    @Override
	    public String toString() {
		    return "{ \"data\": { \"id\": \"" + id + "\", \"type\": \"" + type + "\", \"value\": \"" + value + "\" } }";
	    }
    }

    private static class GraphEdge extends GraphItem {
        public final long source;
        public final long target;
        public final String value;

        public GraphEdge(long id, long source, long target, String value) {
            super(id);
            this.source = source;
            this.target = target;
            this.value = value;
        }

	    @Override
	    public String toString() {
		    return "{ \"data\": { \"id\": \"" + id + "\", \"source\": \"" + source + "\", \"target\": \"" + target + "\", \"value\": \"" + value + "\" } }";
	    }
    }
}

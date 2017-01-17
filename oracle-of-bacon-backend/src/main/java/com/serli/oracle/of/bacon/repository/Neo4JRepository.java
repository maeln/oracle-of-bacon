package com.serli.oracle.of.bacon.repository;


import org.neo4j.driver.v1.*;

import java.util.LinkedList;
import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;


public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
    }

    public List<?> getConnectionsToKevinBacon(String actorName) {
	    LinkedList<String> path = new LinkedList<>();
        Session session = driver.session();
	    StatementResult result = session.run( "MATCH p=shortestPath(\n" +
					    "\t\t(k:Actor)-[*..6]-(d:Actor)\n" +
					    "\t) WHERE k.name contains \"Bacon, Kevin\" and d.name contains \"{actor}\" RETURN p LIMIT 1",
			    parameters( "actor", actorName ) );
	    while (result.hasNext()) {
		    Record record = result.next();
			if(record.containsKey("title"))
		        path.add(record.get("title").asString());
		    else if(record.containsKey("name"))
				path.add(record.get("name").asString());
	    }
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
    }
}

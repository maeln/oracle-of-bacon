# Degrees of Kevin Bacon

Maël NACCACHE - FIL A3, Valentin COCAUD - FIL A3

## 1: Import data into Neo4J

	neo4j-import --into neo4j.db --nodes:Actor imdb-data/actors.csv --nodes:Movie imdb-data/movies.csv --relationships imdb-data/roles.csv

## 2: Neo4J request to find the shortest Path between him and any actor

	MATCH p=shortestPath((k:Actor {name: "Bacon, Kevin (I)"})-[*]-(d:Actor {name: "<ACTOR>"})) RETURN p



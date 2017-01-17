# Degrees of Kevin Bacon

Maël NACCACHE - FIL A3, Valentin COCAUD - FIL A3

## 1: Import data into Neo4J

	neo4j-import --into neo4j.db --nodes:Actor imdb-data/actors.csv --nodes:Movie imdb-data/movies.csv --relationships imdb-data/roles.csv

## 2: Neo4J request to find the shortest Path between him and any actor

	MATCH p=shortestPath(
		(k:Actor)-[*]-(d:Actor)
	) WHERE k.name contains "Bacon, Kevin" and d.name contains "Pacino, Al" RETURN p

But, this request is unbounded and can search forever for a relation, we should add an upper limit like this :

	MATCH p=shortestPath(
		(k:Actor)-[*..6]-(d:Actor)
	) WHERE k.name contains "Bacon, Kevin" and d.name contains "Pacino, Al" RETURN p

But this request can find several shortest path, espacially if some actor have the same name so we should limit ourselves to one path :

	MATCH p=shortestPath(
		(k:Actor)-[*..6]-(d:Actor)
	) WHERE k.name contains "Bacon, Kevin" and d.name contains "Pacino, Al" RETURN p LIMIT 1

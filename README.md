# Degrees of Kevin Bacon

Maël NACCACHE - FIL A3, Valentin COCAUD - FIL A3

## 1: Import data into Neo4J

	neo4j-import --into neo4j.db --nodes:Actor imdb-data/actors.csv --nodes:Movie imdb-data/movies.csv --relationships imdb-data/roles.csv

## 2: Neo4J request to find the shortest Path between him and any actor

```
MATCH p=shortestPath(
	(k:Actor {name: "Bacon, Kevin (I)"})-[*]-(d:Actor {name: "<ACTOR>"})
) RETURN p
```

## 3: Elasticsearch request to suggest actors

```
GET imdb/_search
{
  "_source": ["name"],
  "from" : 0, "size" : 4,
  "query" : {
    "match": {
      "name": "<ACTOR>"
    }
  }
}
```

## 4: For the fun : Adding suggestion based on movie name.

Due to my inability to find a way to make "suggest" work, I tried something a bit different : Giving actors suggestions based on movie name.
To do this, several steps was needed.

NOTA: The movie-based search is only a backup if the name search does not work. The result of the name search as to be empty to force the system to search by movie title.

### 1: Edit the roles.csv to name the columns :
The original imdb-data/roles.csv contain tag on it's header. We want to make them more explicit so we change the first line of the CSV to :

	name, movie, role

### 2: Configure Logstash :
We have to put the CSV using the `csv` filter in log stash. But, there is a twist.
In roles.csv, the data are formatted like this : <ACTOR> <MOVIE> <ROLE>.
The problem is that if an actor worked on several movies, he will be duplicated inside ElasticSearch.
So what we want to do is aggregate the rows to merge each actor into one record and have his movies represented as a Array.
We can do this with the plugin aggregate of Logstash, so we first need to install it :

	$ ./bin/logstash-plugin install logstash-filter-aggregate


Now, we need to use this configuration file

```
input {  
      	file {
         	 path => "/home/maeln/Projets/oracle-of-bacon/imdb-data/roles.csv"
         	 start_position => "beginning"
      	}
}

filter {  
    	csv {
			columns => [ "name", "movie" ]
			remove_field => ["role", "message", "host", "column3", "path"]
       	 	separator => ","
		}
		
		aggregate {
			task_id => "%{name}"
			code => "
				map['movie'] ||= []
					event.to_hash.each do |key,value|
					map[key] = value unless map.has_key?(key)
					map[key] << value if map[key].is_a?(Array)
				end
				"
			push_previous_map_as_event => true
			timeout => 30
			timeout_tags => ['aggregated']
		}
		
		if "aggregated" not in [tags] {
			drop {}
		}
}

output {
	elasticsearch {
		hosts => "localhost:9200"
		index => "imdb15"
	}
}
```

The important part is the ```aggregate``` configuration. We tell it that our "key" is the field name and every time he encounter one, he should create a map of movie and add the second column ('movie') or just add the second column if the map already exist.

### 3: Caveat: concurency
The aggregate plugin fail when Logstash use several Thread because each map might be created in their own local memory and never merged, which will result in duplication in ElasticSearch, precisely what we are trying to fix.
To avoid this, we need to force Logstash to run with *only one worker*. We can do this by either using the option `-w 1` in the Logstash command line, or by appending `workers => 1` in the ouput:elasticsearch part of the Logstash configuration file.

### 4: Creating the index 
You need to create the index to receive our data in ElasticSearch, so you can just use a simple mapping like this :

```
PUT imdb16
{
  "mappings": {
    "actor": {
      "properties": { 
        "name":     {"type": "text", "fielddata": true},
        "movies": 	{"type": "completion"}
      }
    }
  }
}

```

### 5: Result and search
You can now execute Logstash (remenber, use *only one worker*) and search for actors involved in a movie :

```
GET imdb16/_search
{
  "_source": ["name"],
  "from" : 0, "size" : 4,
  "query" : {
    "match": {
      "movie": "star wars the return of the jedi"
    }
  }
}
```




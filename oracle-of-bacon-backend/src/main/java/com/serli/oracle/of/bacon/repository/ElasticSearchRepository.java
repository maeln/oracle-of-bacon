package com.serli.oracle.of.bacon.repository;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ElasticSearchRepository {

	private static class Actor {
		public final String name;
		public Actor(String n) {
			name = n;
		}
	}

	private final JestClient jestClient;

    public ElasticSearchRepository() {
        jestClient = createClient();

    }

    public static JestClient createClient() {
        JestClient jestClient;
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
                .multiThreaded(true)
                .readTimeout(60000)
                .build());

        jestClient = factory.getObject();
        return jestClient;
    }

    public List<String> getActorsSuggests(String searchQuery) {
	    String query = "{\"_source\": [\"name\"], \"from\" : 0, \"size\" : 4,\"query\" : " +
			    "{\"match\": {\"name\": \""+searchQuery+"\"}}}";
	    Search search = new Search.Builder(query).addIndex("imdb").build();

	    try {
		    SearchResult result = jestClient.execute(search);
		    LinkedList<String> actors = new LinkedList<>();
		    for(SearchResult.Hit<Actor, Void> res : result.getHits(Actor.class)) {
			    actors.add(res.source.name);
		    }
		    return actors;
	    } catch (IOException e) {
		    return null;
	    }
    }
}

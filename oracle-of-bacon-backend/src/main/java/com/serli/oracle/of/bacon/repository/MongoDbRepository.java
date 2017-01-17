package com.serli.oracle.of.bacon.repository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.Optional;

public class MongoDbRepository {

    private final MongoClient mongoClient;
	private final MongoDatabase db;
	private final  MongoCollection<Document> collection;

    public MongoDbRepository() {
        mongoClient = new MongoClient("localhost", 27017);
	    db = mongoClient.getDatabase("workshop");
	    collection = db.getCollection("actors");
    }

    public Optional<Document> getActorByName(String name) {
        return Optional.ofNullable(collection.find(Filters.eq("name", name)).first());
    }

	public String getActorByNameToJSON(String name) {
		Optional<Document> peutEtreUnDocument = getActorByName(name);
		if(!peutEtreUnDocument.isPresent())
			return "";
		Document doc = peutEtreUnDocument.get();
		return doc.toJson();
	}
}

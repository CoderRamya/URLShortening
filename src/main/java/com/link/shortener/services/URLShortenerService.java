package com.link.shortener.services;

import com.link.shortener.entity.URLEntity;
import com.link.shortener.util.URLBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class URLShortenerService {

	private  MongoClient mongoClient;
	private  MongoDatabase database;
	private  MongoCollection<Document> collection;

	@Value("${mongodb.uri}")
	public void setMongoUri(String uri) {
		this.mongoClient = MongoClients.create(uri);
	}

	@Value("${mongodb.database}")
	public void setDatabase(String db) {
		this.database = mongoClient.getDatabase(db);
		this.collection = database.getCollection("url_entity");
	}


	public URLEntity save(String longURL) {
		String shortCode = URLBuilder.encodeBase64(longURL);
		URLEntity url = new URLEntity(longURL, shortCode, 0, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now()));
		Document doc = new Document()
				.append("longURL", url.longURL())
				.append("shortCode", url.shortCode())
				.append("accessCount", 0)
				.append("createdAt", url.createdAt())
				.append("updatedAt", url.updatedAt());
		collection.insertOne(doc);
		return url;
		
	}

	public URLEntity update(String shortCode, String longURL) {
		Document query = new Document("shortCode", shortCode);
		Document update = new Document("$set", new Document("longURL", longURL)
				.append("updatedAt", Timestamp.valueOf(LocalDateTime.now())));
		Document result = database.getCollection("url_entity")
				.findOneAndUpdate(query, update);

		if (result != null) {
			return new URLEntity(
					result.getString("longURL"),
					result.getString("shortCode"),
					result.getInteger("accessCount"),
					result.get("createdAt", java.util.Date.class),
					result.get("updatedAt", java.util.Date.class)
			);
		}
		return null;
	}

	public Optional<URLEntity> getLongURl(String shortCode) {
		Document query = new Document("shortCode", shortCode);
		Document result = database
				.getCollection("url_entity")
				.find(query)
				.first();

		if (result != null) {
			URLEntity urlEntity= updateAccessCount(result);
			return Optional.of(urlEntity);
		}
		return Optional.empty();
	}

	public Optional<URLEntity> getLongURlAndUpdateAccessCount(String shortCode) {
		Document query = new Document("shortCode", shortCode);
		Document result = database
				.getCollection("url_entity")
				.find(query)
				.first();

		if (result != null) {
			return Optional.of(new URLEntity(
					result.getString("longURL"),
					result.getString("shortCode"),
					result.getInteger("accessCount") ,
					result.get("createdAt", java.util.Date.class),
					result.get("updatedAt", java.util.Date.class))
			);
		}
		return Optional.empty();
	}



	URLEntity updateAccessCount(Document entity) {
		Document update = new Document("$inc", new Document("accessCount", 1))
				.append("$set", new Document("updatedAt", Timestamp.valueOf(LocalDateTime.now())));
		Document result = database.getCollection("url_entity")
				.findOneAndUpdate(entity, update);

		if (result != null) {
			return new URLEntity(
					result.getString("longURL"),
					result.getString("shortCode"),
					result.getInteger("accessCount") ,
					result.get("createdAt", java.util.Date.class),
					result.get("updatedAt", java.util.Date.class)
			);
		}
		return null;
	}

	public boolean entityExists(String longURL) {
		Document query = new Document("longURL", longURL);
		Document result=database
				.getCollection("url_entity")
				.find(query)
				.first();
		return result != null;
	}

	public boolean findIfEntityExistsByShortCode(String shortCode) {
		Document query = new Document("shortCode", shortCode);
		Document result=database
				.getCollection("url_entity")
				.find(query)
				.first();
		return result != null;
	}


	@PreDestroy
	public void close() {
		mongoClient.close();
	}

	public void delete(String shortCode) {
		Document query = new Document("shortCode", shortCode);
		Document result = database.getCollection("url_entity")
				.findOneAndDelete(query);

	}
}

package com.link.shortener.services;
import com.link.shortener.entity.URLEntity;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.bson.Document;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


public class URLShortenerServicesTest {


    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document>   collection;
    private URLShortenerService service;


    @BeforeEach
    public void setUp() {
        mongoClient = mock(MongoClient.class);
        database = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);

        when(mongoClient.getDatabase(any())).thenReturn(database);
        when(database.getCollection(any())).thenReturn(collection);
        service = new URLShortenerService();
        service.setMongoUri("mongodb://localhost:27017");
        service.setDatabase("test");

        try {
            var mongoClientField = URLShortenerService.class.getDeclaredField("mongoClient");
            mongoClientField.setAccessible(true);
            mongoClientField.set(service, mongoClient);

            var databaseField = URLShortenerService.class.getDeclaredField("database");
            databaseField.setAccessible(true);
            databaseField.set(service, database);

            var collectionField = URLShortenerService.class.getDeclaredField("collection");
            collectionField.setAccessible(true);
            collectionField.set(service, collection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


        @Test
        public void testSave() {
            String longURL = "http://example.com";
            URLEntity entity = service.save(longURL);

            assertEquals(longURL, entity.longURL());
            assertNotNull(entity.shortCode());
            assertEquals(0, entity.accessCount());
            assertNotNull(entity.createdAt());
            assertNotNull(entity.updatedAt());

            verify(collection, times(1)).insertOne(any(org.bson.Document.class));
        }


        @Test
        public void testUpdate() {
            String shortCode = "abc123";
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            String updatedURL="http://example.co.org";
            Document updatedDoc = new Document("longURL", updatedURL)
                    .append("shortCode", shortCode)
                    .append("accessCount", 1)
                    .append("createdAt", now)
                    .append("updatedAt", now);
            Document query = new Document("shortCode", shortCode);
            when(collection.findOneAndUpdate(eq(query), any(Document.class))).thenReturn(updatedDoc);
            var updatedEntity = service.update(shortCode, updatedURL);
            assertEquals(updatedURL, updatedEntity.longURL());
            assertEquals(shortCode, updatedEntity.shortCode());
            assertNotNull(updatedEntity.updatedAt());
            verify(collection, times(1)).findOneAndUpdate(eq(query), any(Document.class));
        }


        @Test
        public void testGetLongURl(){
            String shortCode = "abc123";
            String longURL = "http://example.com";
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            Document doc = new Document("longURL", "http://example.com")
                    .append("shortCode", shortCode)
                    .append("accessCount", 0)
                    .append("createdAt",now)
                    .append("updatedAt", now);

            Document updatedDoc = new Document("longURL", "http://example.com")
                    .append("shortCode", shortCode)
                    .append("accessCount", 1)
                    .append("createdAt", now)
                    .append("updatedAt", now);

            FindIterable<Document> findIterable = mock(FindIterable.class);
            when(collection.find(any(Document.class))).thenReturn(findIterable);
            when(findIterable.first()).thenReturn(doc);
            when(collection.findOneAndUpdate(eq(doc), any(Document.class))).thenReturn(updatedDoc);
            var result = service.getLongURl(shortCode);
            assertNotNull(result);
            assertEquals(longURL, result.get().longURL());
            assertEquals(shortCode, result.get().shortCode());
            assertEquals(1, result.get().accessCount());
            assertEquals(now, result.get().createdAt());
            assertEquals(now, result.get().updatedAt());
            verify(collection, times(1)).find(any(Document.class));
        }


        @Test
        public void testEntityExistsByLongURL() {
            String longURL = "http://example.com";
            Document doc = new Document("longURL", longURL)
                    .append("shortCode", "abc123")
                    .append("accessCount", 0)
                    .append("createdAt", Timestamp.valueOf(LocalDateTime.now()))
                    .append("updatedAt", Timestamp.valueOf(LocalDateTime.now()));

            FindIterable<Document> findIterable = mock(FindIterable.class);
            when(collection.find(any(Document.class))).thenReturn(findIterable);
            when(findIterable.first()).thenReturn(doc);

            boolean exists = service.entityExists(longURL);
            assertEquals(true, exists);
            verify(collection, times(1)).find(any(Document.class));
        }


        @Test
        public void entityExistsByShortCode() {
            String shortCode = "abc123";
            Document doc = new Document("longURL", "http://example.com")
                    .append("shortCode", shortCode)
                    .append("accessCount", 0)
                    .append("createdAt", Timestamp.valueOf(LocalDateTime.now()))
                    .append("updatedAt", Timestamp.valueOf(LocalDateTime.now()));

            FindIterable<Document> findIterable = mock(FindIterable.class);
            when(collection.find(any(Document.class))).thenReturn(findIterable);
            when(findIterable.first()).thenReturn(doc);

            boolean exists = service.findIfEntityExistsByShortCode(shortCode);
            assertEquals(true, exists);
            verify(collection, times(1)).find(any(Document.class));
        }

        @Test
        public void testDelete() {
            String shortCode = "abc123";
            Document query = new Document("shortCode", shortCode);
            when(collection.findOneAndDelete(query)).thenReturn(null); // Simulate successful deletion
            service.delete(shortCode);
            verify(collection, times(1)).findOneAndDelete(query);
        }

        @Test
        public void testUpdateAccessCount() {
            String shortCode = "abc123";
            Document query = new Document("shortCode", shortCode);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            Document updatedDoc = new Document("longURL", "http://example.com")
                    .append("shortCode", shortCode)
                    .append("accessCount", 1)
                    .append("createdAt", now)
                    .append("updatedAt", now);

            when(collection.findOneAndUpdate(eq(query), any(Document.class))).thenReturn(updatedDoc);
            URLEntity updatedEntity = service.updateAccessCount(query);
            assertNotNull(updatedEntity);
            assertEquals(shortCode, updatedEntity.shortCode());
            assertEquals(1, updatedEntity.accessCount());
            assertEquals(now, updatedEntity.updatedAt());
            verify(collection, times(1)).findOneAndUpdate(eq(query), any(Document.class));
        }

        @Test
        public  void testGetLongURLAndAccessCount(){
            String shortCode = "abc123";
            String longURL = "http://example.com";
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            Document doc = new Document("longURL", longURL)
                    .append("shortCode", shortCode)
                    .append("accessCount", 0)
                    .append("createdAt", now)
                    .append("updatedAt", now);

            FindIterable<Document> findIterable = mock(FindIterable.class);
            when(collection.find(any(Document.class))).thenReturn(findIterable);
            when(findIterable.first()).thenReturn(doc);

            var result = service.getLongURlAndUpdateAccessCount(shortCode);
            assertNotNull(result);
            assertEquals(longURL, result.get().longURL());
            assertEquals(shortCode, result.get().shortCode());
            assertEquals(0, result.get().accessCount());
            assertEquals(now, result.get().createdAt());
            assertEquals(now, result.get().updatedAt());
        }

        @AfterEach
        public void tearDown() {
            // Clear all documents from the mocked collection after each test
            collection.deleteMany(new Document());
        }

    }












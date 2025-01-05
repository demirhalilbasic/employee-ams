package controllers;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

public class MongoDBController {

    private static final String CONNECTION_STRING = "mongodb+srv://username:password@employees.mqhpv.mongodb.net/?retryWrites=true&w=majority&appName=employees";
    private static final String DATABASE_NAME = "attendance_system";
    private static final String COLLECTION_NAME = "employees";

    private static MongoDBController instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private MongoDBController() {
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(DATABASE_NAME);

            database.runCommand(new Document("ping", 1));
            System.out.println("Successfully connected to MongoDB!");

        } catch (MongoException e) {
            e.printStackTrace();
            throw new RuntimeException("MongoDB connection failed");
        }
    }

    public static synchronized MongoDBController getInstance() {
        if (instance == null) {
            instance = new MongoDBController();
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }

    public static String getCollectionName() {
        return COLLECTION_NAME;
    }
}
package scripts;

import controllers.MongoDBController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

public class HashPasswordScript {
    public static void main(String[] args) {
        MongoDBController mongoDBController = MongoDBController.getInstance();
        MongoDatabase database = mongoDBController.getDatabase();
        MongoCollection<Document> collection = database.getCollection(MongoDBController.getCollectionName());

        for (Document doc : collection.find()) {
            String plainPassword = doc.getString("password");
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

            collection.updateOne(Filters.eq("_id", doc.getString("_id")),
                    new Document("$set", new Document("password", hashedPassword)));
        }

        System.out.println("Passwords updated successfully!");
        mongoDBController.closeConnection();
    }
}
package scripts;

import controllers.MongoDBController;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.bson.Document;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class InsertDocumentsScript {
    public static void main(String[] args) {
        MongoDBController mongoDBController = MongoDBController.getInstance();
        MongoDatabase database = mongoDBController.getDatabase();

        MongoCollection<Document> mainCollection = database.getCollection(MongoDBController.getCollectionName());
        try (InputStream inputStream = InsertDocumentsScript.class.getResourceAsStream("/eams_project_2_placeholder_data.json");
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            Gson gson = new Gson();
            List<Document> documents = gson.fromJson(reader, new TypeToken<List<Document>>() {}.getType());

            mainCollection.insertMany(documents);
            System.out.println("Main collection documents inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        MongoCollection<Document> bugReportsCollection = database.getCollection("bug_reports");
        try (InputStream inputStream = InsertDocumentsScript.class.getResourceAsStream("/eams_project_2_ticket_placeholder_data.json");
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            Gson gson = new Gson();
            List<Document> bugReports = gson.fromJson(reader, new TypeToken<List<Document>>() {}.getType());

            bugReportsCollection.insertMany(bugReports);
            System.out.println("Bug reports collection documents inserted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mongoDBController.closeConnection();
    }
}
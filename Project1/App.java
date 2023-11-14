import static spark.Spark.*;
import com.google.gson.*;

public class App {
    private Database db = new Database();
    
    public static void main(String[] args) {
        new App().run();
    }

    void run() {
        //db.openConnection("project-db-sqlite");

    }
}
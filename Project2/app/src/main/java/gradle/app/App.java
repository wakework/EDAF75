package gradle.app;

import static spark.Spark.*;

public class App {
    private Database db = new Database();

    public static void main(String[] args) {
        new App().run();
    }

    void run() {

        port(8888);
        db.openConnection("C:\\Programmering\\GitLabProjects\\EDAF75\\project-db.sqlite");

        /* POST /reset OK */
        post("/reset", (req, res) -> db.reset(req, res));

        /* ------- /CUSTOMERS --------- */

        /* POST /customers OK */
        post("/customers", (req, res) -> db.addCustomers(req, res));

        /* GET /customers OK */
        get("/customers", (req, res) -> db.getCustomers(req, res));

        /* ------- /INGREDIENTS -------- */

        /* POST /ingredients OK */
        post("/ingredients", (req, res) -> db.addMaterials(req, res));

        /* POST /ingredients/<ingredientNameEncoded>/deliveries OK */
        post("/ingredients/:ingredient/deliveries", (req, res) -> db.addDelivery(req, res, req.params(":ingredient")));

        /* GET /ingredients OK */
        get("/ingredients", (req, res) -> db.getMaterials(req, res));

        /* --------- /COOKIES ---------- */

        /* POST /cookies OK */
        post("/cookies", (req, res) -> db.addCookie(req, res));

        /* GET /cookies OK */
        get("/cookies", (req, res) -> db.getCookies(req, res));

        /* GET /cookies/<cookie-name>/recipe OK */
        get("/cookies/:cookieName/recipe", (req, res) -> db.getCookie(req, res, req.params(":cookieName")));

        /* POST /cookies/<cookie_name>/block PROBLEM MED INLÄSNING? */
        post("/cookies/:cookieName/block/:after/:before", (req, res) -> db.block(req, res, req.params(":cookieName")));

        /* POST /cookies/<cookie_name_encoded>/unblock PROBLEM MED INLÄSNING? */
        post("/cookies/:cookieName/unblock/:after/:before", (req, res) -> db.unblock(req, res, req.params(":cookieName")));

        /* --------- /PALLETS ---------- */

        /* POST /pallets OK */
        post("/pallets", (req, res) -> db.newPallet(req, res));

        /* GET /pallets/<cookie_name>/:after/:before PROBLEM MED INLÄSNING? */
        get("/pallets", (req, res) -> db.getPallets(req, res));
    }
}

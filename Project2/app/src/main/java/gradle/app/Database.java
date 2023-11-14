package gradle.app;

import spark.Request;
import spark.Response;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import com.google.gson.*;
import java.sql.*;
import java.util.*;

/**
 * Database is an interface to the college application database, it
 * uses JDBC to connect to a SQLite3 file.
 */
public class Database {

    /**
     * The database connection.
     */
    private Connection conn;
    private Gson gson = new Gson();

    /**
     * Creates the database interface object. Connection to the
     * database is performed later.
     */
    public Database() {
        conn = null;
    }

    /**
     * Opens a connection to the database, using the specified
     * filename (if we'd used a traditional DBMS, such as PostgreSQL
     * or MariaDB, we would have specified username and passwd
     * instead).
     */
    public boolean openConnection(String filename) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + filename);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Closes the connection to the database.
     */
    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the connection to the database has been established
     *
     * @return true if the connection has been established
     */
    public boolean isConnected() {
        return conn != null;
    }

    /* ================================== */
    /* -------- our code below ---------- */
    /* ===============================*== */

    /**
     *  Method to print JSON objects.
     */
    private String restResult(Object result) {
        return gson.toJson(new ResultWrapper(result));
    }

    /**
     * Class to wrap the data from JSON.
     */
    public class ResultWrapper {
        private Object data;

        public ResultWrapper(Object data) {
            this.data = data;
        }
    }

    /**
     * Checks parameters in App.
     *
     * @param req
     * @param paramName
     * @param test
     * @param params
     * @return string represenation of the paramater.
     */
    private String checkParam(Request req, String paramName, String test, List<String> params) {
        var param = deCoder(req.queryParams(paramName));
        if (param != null) {
            params.add(param);
            return test;
        }
        return "";
    }

    /**
     * Decoder for URL-coding.
     */
    private String deCoder(String title) {
        if (title == null) {
            return null;
        }
        try {
            String decode = URLDecoder.decode(title, "UTF-8");
            return decode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    /**
     * Encoder for URL-coding.
     */
    private String enCoder(String title) {
        try {
            String encode = URLEncoder.encode(title, "UTF-8").replace("+", "%20");
            return encode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    /* ================================== */
    /* ------- endpoint methods --------- */
    /* ================================== */

     /**
      * /RESET
      * Reseting database.
      *
      * @param req
      * @param res
      * @return / for successfull reset.
      */
    public String reset(Request req, Response res) {
        var sql1 = "PRAGMA foreign_keys=OFF";
        var sql2 = "DELETE FROM customers";
        var sql3 = "DELETE FROM orders";
        var sql4 = "DELETE FROM pallets";
        var sql5 = "DELETE FROM products";
        var sql6 = "DELETE FROM orderStatus";
        var sql7 = "DELETE FROM wareHouse";
        var sql8 = "DELETE FROM reciepts";
        var sql9 = "PRAGMA foreign_keys=ON";

        try (Statement s = conn.createStatement()) {
            s.addBatch(sql1);
            s.addBatch(sql2);
            s.addBatch(sql3);
            s.addBatch(sql4);
            s.addBatch(sql5);
            s.addBatch(sql6);
            s.addBatch(sql7);
            s.addBatch(sql8);
            s.addBatch(sql9);

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(500);
            return "";
        }

        res.status(205);
        return "/";
    }

     /**
      * /CUSTOMERS
      * Add a new customers to the database.
      *
      * @param req
      * @param res
      * @return the name of the customer.
      */
    public String addCustomers(Request req, Response res) {
        res.type("application/json");
        Customer customer = gson.fromJson(req.body(), Customer.class);

        var statement =
            """
            INSERT
            INTO customers (customerName, customerAddress)
            VALUES (?, ?)
            """;

        try (var ps = conn.prepareStatement(statement)) {
            ps.setString(1, customer.name);
            ps.setString(2, customer.address);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        String name = enCoder(customer.name);
        res.status(201);
        return "{ " + "\"" + "location" + "\"" + ": " + "\"" +  "/customers/" + name + "\"" + " }";
    }

    /**
     * /CUSTOMERS
     * Get Krustys customers from table customers.
     *
     * @param req
     * @param res
     * @return all customers as JSON objects.
     */
    public String getCustomers(Request req, Response res) {
        res.type("application/json");
        var found = new ArrayList<Customer>();
        var query =
            """
            SELECT *
            FROM   customers
            """;

        try (var ps = conn.prepareStatement(query)) {
            var rs = ps.executeQuery();

            while (rs.next()) {
                found.add(Customer.fromRS(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        res.status(200);
        return restResult(found);
    }

    /**
     * Class customers to translate between JSON and SQL.
     */
    public static class Customer {
        private String name, address;

        public Customer(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public static Customer fromRS(ResultSet rs) throws SQLException {
            return new Customer(rs.getString("customerName"), rs.getString("customerAddress"));
        }
    }

     /**
      * /INGREDIENTS
      * Add materials to wareHouse.
      *
      * @param req
      * @param res
      * @return location of ingredient name.
      */
    public String addMaterials(Request req, Response res) {
        res.type("application/json");
        Ingredient ing = gson.fromJson(req.body(), Ingredient.class);

        var statement =
            """
            INSERT
            INTO wareHouse (ingredientName, unit)
            VALUES (?, ?)
            """;

        try (var ps = conn.prepareStatement(statement)) {
            ps.setString(1, ing.ingredient);
            ps.setString(2, ing.unit);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        String name = enCoder(ing.ingredient);
        res.status(201);
        return "{ " + "\"" + "location" + "\"" + ": " + "\"" + "/ingredients/" + name + "\"" + " }";
    }

    /**
     * /INGREDIENTS/DELIVERIES
     * Add ingredient with quantity amount to wareHouse.
     *
     * @param req
     * @param res
     * @param ingredient
     * @return JSON objects of delivery.
     */
    public String addDelivery(Request req, Response res, String ingredient) {
        res.type("application/json");
        Material dev = gson.fromJson(req.body(), Material.class);
        dev.addIngredient(deCoder(ingredient));

        var statement =
            """
            UPDATE wareHouse
            SET    totalAmount = (totalAmount + ?),
                   lastDelivered = ?,
                   lastDeliveredAmount = ?
            WHERE  ingredientName = ?
            """;

        try (var ps = conn.prepareStatement(statement)) {
            ps.setInt(1, dev.quantity);
            ps.setString(2, dev.deliveryTime);
            ps.setInt(3, dev.quantity);
            ps.setString(4, dev.ingredient);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        dev.quantity = getTotalAmount(dev);
        res.status(201);
        return restResult(dev);
    }

    /**
     * Finds the total amount of a material in wareHouse.
     *
     * @param dev
     * @return int value of totalAmount.
     */
    private int getTotalAmount(Material dev) {
        var found = new ArrayList<Integer>();
        var query = """
                SELECT totalAmount
                FROM   wareHouse
                WHERE  ingredientName = ?
                """;

        try (var ps = conn.prepareStatement(query)) {
            ps.setString(1, dev.ingredient);
            var rs = ps.executeQuery();

            while (rs.next()) {
                found.add(rs.getInt(1));
            }

            return found.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * /INGREDIENTS.
     * Get materials in wareHouse.
     *
     * @param req
     * @param res
     * @return JSON objects of all materials found.
     */
    public String getMaterials(Request req, Response res) {
        res.type("application/json");
        var found = new ArrayList<Material>();
        var query =
            """
            SELECT ingredientName, totalAmount, unit
            FROM   wareHouse
            """;

        try (var ps = conn.prepareStatement(query)) {
            var rs = ps.executeQuery();

            while (rs.next()) {
                found.add(Material.fromRS(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        res.status(200);
        return restResult(found);
    }

    /**
     * Class for materials in the warehouse, to translate between JSON and SQL.
     */
    public static class Material {
        private String ingredient;
        private int quantity;
        private String unit;
        private String deliveryTime;

        public Material (String ingredient, int quantity, String unit, String deliveryTime) {
            this.ingredient = ingredient;
            this.quantity = quantity;
            this.unit = unit;
            this.deliveryTime = deliveryTime;
        }

        public Material (String ingredient, int quantity, String unit) {
            this.ingredient = ingredient;
            this.quantity = quantity;
            this.unit = unit;
        }

        public Material (String deliveryTime, int quantity) {
            this.quantity = quantity;
            this.deliveryTime = deliveryTime;
        }

        public void addIngredient(String ing) {
            ingredient = ing;
        }

        public static Material fromRS(ResultSet rs) throws SQLException {
            return new Material(rs.getString("ingredientName"), rs.getInt("totalAmount"),
                rs.getString("unit"));
        }
    }

    /**
     * Class for ingredients with name and unit, to translate between JSON and SQL.
     */
    public static class Ingredient {
        private String ingredient, unit;

        public Ingredient (String ingredient, String unit) {
            this.ingredient = ingredient;
            this.unit = unit;
        }

        public static Ingredient fromRS(ResultSet rs) throws SQLException {
            return new Ingredient(rs.getString("ingredientName"), rs.getString("unit"));
        }
    }

    /**
     * /COOKIES
     * Add cookie to database.
     *
     * @param req
     * @param res
     * @return Cookie as JSON object.
     */
    public String addCookie(Request req, Response res) {
        var recipes = splitBody(req.body());

        var statement =
            """
            INSERT
            INTO products (productName)
            VALUES (?)
            """;

        try (var ps = conn.prepareStatement(statement)) {
            ps.setString(1, recipes.get(0).name);
            ps.executeUpdate();

            for (Recipe recipe : recipes) {
                addRecipes(req, res, recipe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        String name = enCoder(recipes.get(0).name);
        res.status(201);
        return "{ " + "\"" + "location" + "\"" + ": " + "\"" + "/cookies/" + name + "\"" + " }";
    }

    private ArrayList<Recipe> splitBody(String body) {
        var found = new ArrayList<Recipe>();

        // Split to find the startindex of the cookieName.
        String[] recipes1 = body.split("\\[");
        int index = 0;
        while (recipes1[0].charAt(index) != ':') {
            index++;
        }

        // Append the cookieName.
        index = index + 2;

        StringBuilder sb = new StringBuilder();
        while (recipes1[0].charAt(index) != ',') {
            sb.append(recipes1[0].charAt(index));
            index++;
        }
        sb.deleteCharAt(0);
        sb.deleteCharAt(sb.length() - 1);
        String cookie = sb.toString();

        // Split to find every recipe.
        String[] recipes2 = recipes1[1].split("},");

        for (String recipe : recipes2) {
            sb.delete(0, sb.length());
            sb.append(recipe);

            if (recipe.charAt(recipe.length() - 1) == '}') {
                String[] obj = sb.toString().split("\\]");
                found.add(gson.fromJson(obj[0], Recipe.class));
            } else {
                sb.append("}");
                found.add(gson.fromJson(sb.toString(), Recipe.class));
            }

            found.get(found.size() - 1).addName(cookie);
        }

        return found;
    }

    private String addRecipes (Request req, Response res, Recipe recipe) {
        var statement =
            """
            INSERT
            INTO reciepts (productName, ingredientName, ingredientAmount)
            VALUES (?, ?, ?)
            """;

        try (var ps = conn.prepareStatement(statement)) {
            ps.setString(1, recipe.name);
            ps.setString(2, recipe.ingredient);
            ps.setInt(3, recipe.amount);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        return "OK";
    }

    public static class Recipe {
        private String name, ingredient;
        private int amount;

        public Recipe (String ingredient, int amount) {
            this.ingredient = ingredient;
            this.amount = amount;
        }

        public void addName(String name) {
            this.name = name;
        }
    }

    /**
     * /COOKIES
     * Get all cookies in products.
     *
     * @param req
     * @param res
     * @return JSON Objects of all products.
     */
    public String getCookies(Request req, Response res) {
        var found = new ArrayList<Product>();
        var query =
            """
            WITH palletCount AS (
                SELECT productName, count(*) AS palets
                FROM   pallets
                GROUP BY productName
                )
            SELECT          productName, palets
            FROM            products
            LEFT OUTER JOIN palletCount
            USING           (productName)
            """;

        try (var ps = conn.prepareStatement(query)) {
            var rs = ps.executeQuery();

            while (rs.next()) {
                found.add(Product.fromRS(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        res.status(200);
        return restResult(found);
    }

    /**
     * /COOKIES/RECIPE
     * Get recipe for specific cookieName in products.
     *
     * @param req
     * @param res
     * @param product
     * @return String represenation of JSON objects.
     */
    public String getCookie(Request req, Response res, String product) {
        product = deCoder(product);

        var found = new ArrayList<Reciepts>();
        var query =
            """
            SELECT productName, ingredientName, ingredientAmount, unit
            FROM   reciepts
            JOIN   wareHouse
            USING  (ingredientName)
            WHERE  productName = ?
            """;

        try (var ps = conn.prepareStatement(query)) {
            ps.setString(1, product);
            var rs = ps.executeQuery();

            while (rs.next()) {
                found.add(Reciepts.fromRS(rs));
            }

            if (found.size() < 1) {
                res.status(404);
                return "No such cookie";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        res.status(200);
        return restResult(found);
    }

    /**
     * /BLOCK/cookie
     * Block pallets with cookie cookie.
     *
     * @param req
     * @param res
     * @param cookie
     * @return Empty string.
     */
    public String block(Request req, Response res, String cookie) {
        cookie = deCoder(cookie);
        var query =
            """
            UPDATE pallets
            SET    blocked = 1
            WHERE  productName = ? AND
                   1 = 1
            """;

        var params = new ArrayList<String>();
        query+= checkParam(req, "after", " AND productionDate > ?", params);
        query+= checkParam(req, "before", " AND productionDate < ?", params);

        try (var ps = conn.prepareStatement(query)) {
            var index = 0;
            for (var param : params) {
                ps.setString(++index, param);
            }

            ps.setString(1, cookie);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "ERROR";
        }

        res.status(205);
        return "";
    }

    /**
     * /UNBLOCK/cookie
     * Unblock pallets with cookie cookie.
     *
     * @param req
     * @param res
     * @param cookie
     * @return empty string.
     */
    public String unblock(Request req, Response res, String cookie) {
        cookie = deCoder(cookie);
        var query =
            """
            UPDATE pallets
            SET    blocked = true
            WHERE  productName = ? AND
                   1 = 1
            """;

        var params = new ArrayList<String>();
        query+= checkParam(req, "after", " AND productionDate > ?", params);
        query+= checkParam(req, "before", " AND productionDate < ?", params);

        try (var ps = conn.prepareStatement(query)) {
            var index = 0;
            for (var param : params) {
                ps.setString(++index, param);
            }

            ps.setString(1, cookie);
            ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
        }

        res.status(205);
        return "";
    }

    /**
     * Class for a product to translate between JSON and SQL.
     */
    public static class Product {
        private String name;
        private int pallets;

        public Product(String name, int pallets) {
            this.name = name;
            this.pallets = pallets;
        }

        public static Product fromRS(ResultSet rs) throws SQLException {
            return new Product(rs.getString("productName"), rs.getInt("palets"));
        }
    }

    /**
     * Class for a recipe to translate between JSON and SQL.
     */
    public static class Reciepts {
        private final String ingredient;
        private final int amount;
        private final String unit;

        public Reciepts (String ingredient, int amount, String unit) {
            this.ingredient = ingredient;
            this.amount = amount;
            this.unit = unit;
        }

        public static Reciepts fromRS(ResultSet rs) throws SQLException {
            return new Reciepts(rs.getString("ingredientName"), rs.getInt("ingredientAmount"),
                rs.getString("unit"));
        }
    }

    /**
     * /PALLETS
     * Post a new pallet. Update wareHouse.
     *
     * @param req
     * @param res
     * @return location of palletId.
     */
    public String newPallet(Request req, Response res) {
        Pallet pallet = gson.fromJson(req.body(), Pallet.class);
        var statement =
            """
            INSERT
            INTO    pallets(productName, productionDate)
            VALUES  (?, ?)
            """;

        try (var ps = conn.prepareStatement(statement)) {
            ps.setString(1, pallet.cookie);
            Calendar calendar = Calendar.getInstance();
            var now = calendar.getTime();
            ps.setDate(2, new java.sql.Date(now.getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(422);
            return "";
        }

        pallet.id = getLatestPalletId();
        res.status(201);
        return "{ " + "\"" + "location" + "\"" + ": " + "\"" + "/pallets/" + pallet.id + "\"" + " }";
    }

    /**
     * Get the last added pallets palletId.
     * @return last added palletId.
     */
    private String getLatestPalletId() {
        var statement =
            """
            SELECT palletId
            FROM   pallets
            WHERE  rowid = last_insert_rowid()
            """;

        try (var ps = conn.prepareStatement(statement)) {
            var rs = ps.executeQuery();
            if (rs.next()) {
                var id = rs.getString("palletId");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * /PALLETS
     * Get pallets from Pallets.
     *
     * @param req
     * @param res
     * @return Result as JSON objects.
     */
    public String getPallets(Request req, Response res) {

        var found = new ArrayList<Pallet>();
        var query =
            """
            SELECT palletId, productName, productionDate, isBlocked
            FROM   pallets
            WHERE  1 = 1
            """;

        var params = new ArrayList<String>();
        query+= checkParam(req, "cookie", " AND productName = ?", params);
        query+= checkParam(req, "after", " AND productionDate > ?", params);
        query+= checkParam(req, "before", " AND productionDate < ?", params);

        try (var ps = conn.prepareStatement(query)) {
            var index = 0;
            for (var param : params) {
                ps.setString(++index, param);
            }

            var rs = ps.executeQuery();
            while (rs.next()) {
                found.add(Pallet.fromRS(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }

        res.status(200);
        return restResult(found);
    }

    /**
     * Class for pallets to translate between JSON and SQL.
     */
    public static class Pallet {
        private String id, cookie, productionDate;
        private boolean blocked;

        public Pallet(String id, String cookie, String productionDate, boolean blocked) {
            this.id = id;
            this.cookie = cookie;
            this.productionDate = productionDate;
            this.blocked = blocked;
        }

        public Pallet (String cookie) {
            this.cookie = cookie;
        }

        public static Pallet fromRS(ResultSet rs) throws SQLException {
            return new Pallet(rs.getString("palletId"), rs.getString("productName"),
                rs.getString("productionDate"), rs.getBoolean("isBlocked"));
        }
    }
}

-- Disable foreign key checks, so the tables can
-- be deleted from in arbitrary order.

PRAGMA foreign_keys = OFF;

DELETE FROM customers;
DELETE FROM orders;
DELETE FROM pallets; 
DELETE FROM products;
DELETE FROM orderStatus; 
DELETE FROM wareHouse;
DELETE FROM reciepts;
DROP TRIGGER IF EXISTS palletCheck;

PRAGMA foreign_keys = ON;

-- Insert initial data into tables
-- Initial customers
INSERT INTO customers (customerName, customerAddress) 
VALUES ('Finkakor AB', 'Helsingborg'),
       ('Småbröd AB', 'Malmö'), 
       ('Kaffebröd AB', 'Landskorna'), 
       ('Bjudkakor AB', 'Ystad'), 
       ('Kalaskakor AB', 'Trelleborg'), 
       ('Partykakor AB', 'Kristianstad'), 
       ('Gästkakor AB', 'Hässleholm'), 
       ('Skånekakor AB', 'Perstorp');

-- Initial products
INSERT INTO products (productName) 
VALUES ('Nut ring'), 
       ('Nut cookie'), 
       ('Amneris'), 
       ('Tango'), 
       ('Almond delight'), 
       ('Berliner');

-- Total warehouse supplies 
INSERT INTO wareHouse (ingredientName, totalAmount, unit) 
VALUES ('Flour', 100000, 'g'), 
       ('Butter', 100000, 'g'), 
       ('Icing sugar', 100000, 'g'), 
       ('Roasted, chopped nuts', 100000, 'g'), 
       ('Fine-ground nuts', 100000, 'g'), 
       ('Ground, roasted nuts', 100000, 'g'), 
       ('Bread crumbs', 100000, 'g'), 
       ('Sugar', 100000, 'g'), 
       ('Egg whites', 100000, 'ml'), -- ml instead of dl
       ('Chocolate', 100000, 'g'), 
       ('Marzipan', 100000, 'g'), 
       ('Eggs', 100000, 'g'), 
       ('Potato starch', 100000, 'g'), 
       ('Wheat flour', 100000, 'g'), 
       ('Sodium bicarbonate', 100000, 'g'), 
       ('Vanilla', 100000, 'g'), 
       ('Chopped almonds', 100000, 'g'), 
       ('Cinnamon', 100000, 'g'), 
       ('Vanilla sugar', 100000, 'g');

-- Reciept of products
INSERT INTO reciepts (productName, ingredientName, ingredientAmount) 
VALUES ('Nut ring', 'Flour', 450), 
       ('Nut ring', 'Butter', 450), 
       ('Nut ring', 'Icing sugar', 190), 
       ('Nut ring', 'Roasted, chopped nuts', 225), 
       
       ('Nut cookie', 'Fine-ground nuts', 750), 
       ('Nut cookie', 'Ground, roasted nuts', 625), 
       ('Nut cookie', 'Bread crumbs', 125), 
       ('Nut cookie', 'Sugar', 375), 
       ('Nut cookie', 'Egg whites', 350), -- 350 ml instead of 3,5 dl
       ('Nut cookie', 'Chocolate', 50), 
       
       ('Amneris', 'Marzipan', 750), 
       ('Amneris', 'Butter', 250), 
       ('Amneris', 'Eggs', 250), 
       ('Amneris', 'Potato starch', 25), 
       ('Amneris', 'Wheat flour', 25), 
       
       ('Tango', 'Butter', 200), 
       ('Tango', 'Sugar', 250), 
       ('Tango', 'Flour', 300), 
       ('Tango', 'Sodium bicarbonate', 4), 
       ('Tango', 'Vanilla', 2), 
       
       ('Almond delight', 'Butter', 400), 
       ('Almond delight', 'Sugar', 270), 
       ('Almond delight', 'Chopped almonds', 279), 
       ('Almond delight', 'Flour', 400), 
       ('Almond delight', 'Cinnamon', 10), 
       
       ('Berliner', 'Flour', 350), 
       ('Berliner', 'Butter', 250), 
       ('Berliner', 'Icing sugar', 100), 
       ('Berliner', 'Eggs', 50), 
       ('Berliner', 'Vanilla sugar', 5), 
       ('Berliner', 'Chocolate', 50);

INSERT INTO pallets (productName, productionDate)
VALUES ('Nut ring', "2021-04-28"), 
       ('Nut ring', "2021-04-28"), 
       ('Nut ring', "2021-04-28"), 
       ('Nut ring', "2021-04-28"), 
       ('Nut cookie', "2021-04-28");

-- Create trigger for Storage.
CREATE TRIGGER palletCheck
BEFORE INSERT ON pallets
BEGIN
  UPDATE wareHouse
  SET    totalAmount = totalAmount - 54 * (
    SELECT ingredientAmount
    FROM   reciepts
    WHERE  productName = NEW.productName
  )
  WHERE  ingredientName IN (
    SELECT ingredientName
    FROM   reciepts
    WHERE  productName = NEW.productName
  );

  SELECT
    CASE WHEN
      (SELECT totalAmount
       FROM   wareHouse
       WHERE  ingredientName IN (
           SELECT ingredientName
           FROM   reciepts
           WHERE  productName = NEW.productName
        )
      ) < 0
    THEN
      RAISE (ROLLBACK, "Storage Limit Reached")
    END;
END;
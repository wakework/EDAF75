-- Delete the tables if they exist.
-- Disable foreign key checks, so the tables can
-- be dropped in arbitrary order.

PRAGMA foreign_keys=OFF;

DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS pallets;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS orderStatus;
DROP TABLE IF EXISTS wareHouse;
DROP TABLE IF EXISTS reciepts;

PRAGMA foreign_keys=ON;

-- Create tables
CREATE TABLE customers (
    customerName    TEXT, 
    customerAddress TEXT,
    PRIMARY KEY (customerName)
);

CREATE TABLE orders (
    orderId       INT, 
    customerName  TEXT, 
    productName   TEXT, 
    deliveryTime  TEXT,
    PRIMARY KEY (orderId), 
    FOREIGN KEY (customerName) REFERENCES customers (customerName), 
    FOREIGN KEY (productName) REFERENCES products (productName)
);

CREATE TABLE pallets (
    palletId       TEXT DEFAULT (lower(hex(randomblob(16)))), 
    productName    TEXT, 
    orderId        INT,
    liveLocation   TEXT, 
    productionDate DATE, 
    isBlocked      BOOLEAN DEFAULT 0, 
    deliveryDate   DATE, 
    deliveryTime   TIME, 
    PRIMARY KEY (palletId), 
    -- FOREIGN KEY (orderId) REFERENCES orders (orderId), 
    FOREIGN KEY (productName) REFERENCES products (productName)
);

CREATE TABLE products (
    productName  TEXT, 
    PRIMARY KEY (productName)
);

CREATE TABLE orderStatus (
    orderId       INT,
    productName   TEXT, 
    orderAmount   INT CHECK (orderAmount >= 1), -- If amount is not > 0, invalid order 
    FOREIGN KEY (orderId) REFERENCES orders (orderId), 
    FOREIGN KEY (productName) REFERENCES products (productName), 
    PRIMARY KEY (orderId, productName)
);

CREATE TABLE wareHouse (
    ingredientName      TEXT,
    totalAmount         INT DEFAULT 0, 
    unit                TEXT, 
    lastDelivered       TEXT, 
    lastDeliveredAmount INT DEFAULT 0,
    PRIMARY KEY (ingredientName)
);

CREATE TABLE reciepts (
    productName      TEXT, 
    ingredientName   TEXT, 
    ingredientAmount INT, 
    FOREIGN KEY (productName) REFERENCES products (productName), 
    FOREIGN KEY (ingredientName) REFERENCES wareHouse (ingredientName), 
    PRIMARY KEY (productName, ingredientName)
);
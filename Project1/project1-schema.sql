-- Delete the tables if they exist.
-- Disable foreign key checks, so the tables can
-- be dropped in arbitrary order.

PRAGMA foreign_keys=OFF;

DROP TABLE IF EXISTS ;
DROP TABLE IF EXISTS ;
DROP TABLE IF EXISTS ;
DROP TABLE IF EXISTS ;

PRAGMA foreign_keys=ON;

-- Create tables
CREATE TABLE customers (
    customerName  TEXT, 
    address       TEXT, 
    PRIMARY KEY (customerName)
);

CREATE TABLE orders (
    orderId       INT, 
    productName   TEXT, 
    customerName  TEXT, 
    PRIMARY KEY (orderId), 
    FOREIGN KEY (customerName) REFERENCES customers (customerName)
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
    FOREIGN KEY (orderId) REFERENCES orders (orderId), 
    FOREIGN KEY (productName) REFERENCES products (productName)
);

CREATE TABLE products (
    productName  TEXT, 
    PRIMARY KEY (productName)
);

CREATE TABLE orderStatus (
    orderId       INT,
    productName   TEXT, 
    productAmount INT, -- CHECK (productAmount >= 1) ?
    PRIMARY KEY (orderId, productName), 
    FOREIGN KEY (orderId) REFERENCES orders (orderId), 
    FOREIGN KEY (productName) REFERENCES products (productName)
);

CREATE TABLE wareHouse ();

CREATE TABLE reciepts ();
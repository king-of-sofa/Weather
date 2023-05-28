create table weather (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ip VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    latitude VARCHAR(50) NOT NULL,
    longitude VARCHAR(50) NOT NULL,
    temperature VARCHAR(50) NOT NULL
);
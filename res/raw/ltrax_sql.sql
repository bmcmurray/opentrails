CREATE TABLE 'Track'
(
'id' INTEGER PRIMARY KEY,
'title' VARCHAR(255),
'created' TIMESTAMP DEFAULT (DATETIME('now')) NOT NULL,
'modified' TIMESTAMP
);



CREATE TABLE 'TrackPoint'
(
'id' INTEGER PRIMARY KEY UNIQUE ,
'tid' INTEGER NOT NULL,
'lon' FLOAT NOT NULL,
'lat' FLOAT NOT NULL,
'timestamp' TIMESTAMP DEFAULT (DATETIME('now')) NOT NULL
);



CREATE TABLE 'Point'
(
'id' INTEGER PRIMARY KEY UNIQUE ,
'title' VARCHAR(255),
'lon' FLOAT NOT NULL,
'lat' FLOAT NOT NULL,
'timestamp' TIMESTAMP DEFAULT (DATETIME('now')) NOT NULL
);



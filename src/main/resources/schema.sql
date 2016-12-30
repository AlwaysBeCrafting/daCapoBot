CREATE TABLE IF NOT EXISTS chat_log (
    id          INTEGER     PRIMARY KEY,
	timestamp   INTEGER     NOT NULL,
	user        TEXT        COLLATE NOCASE,
	message     TEXT        COLLATE NOCASE);

CREATE TABLE IF NOT EXISTS quotes (
    id          INTEGER     PRIMARY KEY,
	timestamp   INTEGER     NOT NULL,
	quote     TEXT        COLLATE NOCASE);

CREATE TABLE IF NOT EXISTS tracks (
	id          INTEGER     PRIMARY KEY    ,
	title       TEXT        COLLATE NOCASE ,
	path        TEXT        NOT NULL       ,
	artist      TEXT        COLLATE NOCASE ,
	album       TEXT        COLLATE NOCASE);

CREATE TABLE IF NOT EXISTS vetoes (
    id          INTEGER     PRIMARY KEY     ,
    timestamp   INTEGER     NOT NULL        ,
    user        TEXT        COLLATE NOCASE  ,
    track_id    INTEGER     NOT NULL        ,

    FOREIGN KEY (track_id) REFERENCES tracks(id));

CREATE TABLE IF NOT EXISTS requests (
    id          INTEGER     PRIMARY KEY     ,
    timestamp   INTEGER     NOT NULL        ,
    user        TEXT        COLLATE NOCASE  ,
    track_id    INTEGER     NOT NULL        ,

    FOREIGN KEY (track_id) REFERENCES tracks(id));


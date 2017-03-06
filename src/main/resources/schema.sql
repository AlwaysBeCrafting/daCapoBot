CREATE TABLE IF NOT EXISTS chat_log (
    id          INTEGER     PRIMARY KEY,
	timestamp   INTEGER     NOT NULL,
	user        TEXT        COLLATE NOCASE,
	mesage     TEXT        COLLATE NOCASE);

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

CREATE TABLE IF NOT EXISTS slots (
    id          INTEGER     PRIMARY KEY     ,
    time_slot   TEXT        NOT NULL        ,
    is_bookable INTEGER     CHECK(is_bookable >= 0 AND is_bookable < 2)    ,
    title       TEXT        COLLATE NOCASE,
    slot_id     INTEGER     NOT NULL        ,

    FOREIGN KEY (slot_id) REFERENCES tracks(id));

CREATE TRIGGER book_out_slot AFTER INSERT ON slots
    BEGIN
        UPDATE slots SET is_bookable = 0 WHERE id = max(id) AND is_bookable = NULL;
    END;
-- Create tables for gamestudio database
-- Run this script after creating the 'gamestudio' database in PostgreSQL

CREATE TABLE IF NOT EXISTS score (
    id        SERIAL PRIMARY KEY,
    game      VARCHAR(64)  NOT NULL,
    player    VARCHAR(64)  NOT NULL,
    points    INTEGER      NOT NULL,
    playedon  TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS comment (
    id          SERIAL PRIMARY KEY,
    game        VARCHAR(64)  NOT NULL,
    player      VARCHAR(64)  NOT NULL,
    content     VARCHAR(1024) NOT NULL,
    commentedon TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS rating (
    id       SERIAL PRIMARY KEY,
    game     VARCHAR(64) NOT NULL,
    player   VARCHAR(64) NOT NULL,
    stars    INTEGER     NOT NULL CHECK (stars BETWEEN 1 AND 5),
    ratedon  TIMESTAMP   NOT NULL,
    UNIQUE (game, player)
);

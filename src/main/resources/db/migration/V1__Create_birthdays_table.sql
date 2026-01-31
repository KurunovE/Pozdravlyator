CREATE TABLE IF NOT EXISTS birthdays (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    photo_path VARCHAR(500)
);

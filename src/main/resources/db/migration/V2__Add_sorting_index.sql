CREATE INDEX IF NOT EXISTS idx_birthdays_month_day
    ON birthdays((EXTRACT(MONTH FROM birth_date)), (EXTRACT(DAY FROM birth_date)));

CREATE INDEX IF NOT EXISTS idx_birthdays_day_month
    ON birthdays((EXTRACT(DAY FROM birth_date)), (EXTRACT(MONTH FROM birth_date)));

CREATE INDEX IF NOT EXISTS idx_birthdays_name
    ON birthdays(name);

ALTER TABLE tasks ADD COLUMN priority_id INT REFERENCES priorities(id);

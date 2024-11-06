-- CREATE TABLE "user"(
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
--   username VARCHAR(50) NOT NULL,
--   email VARCHAR(100) NOT NULL,
--   password VARCHAR(255) NOT NULL,
--   role ENUM('PLAYER', 'ADMIN') NOT NULL,
--   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- );

-- CREATE TABLE player_details (
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
--   user_id BIGINT NOT NULL,
--   elo_rating INT,
--   first_name VARCHAR(100),
--   last_name VARCHAR(100),
--   country VARCHAR(100),
--   profile_picture VARCHAR(255),
--   total_wins INT DEFAULT 0,
--   total_losses INT DEFAULT 0,
--   total_matches INT DEFAULT 0,
--   highest_elo INT,
--   FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
-- );

-- CREATE TABLE elo_history (
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
--   player_id BIGINT NOT NULL,
--   old_elo INT NOT NULL,
--   new_elo INT NOT NULL,
--   change_reason ENUM('WIN', 'LOSS', 'DRAW') NOT NULL,
--   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--   FOREIGN KEY (player_id) REFERENCES player_details(id) ON DELETE CASCADE
-- );


-- INSERT INTO "user" (username, email, password, role) VALUES
-- ('john_doe', 'john.doe@example.com', 'hashed_password1', 'PLAYER'),
-- ('jane_doe', 'jane.doe@example.com', 'hashed_password2', 'PLAYER');

-- select * from "user";

INSERT INTO player_details (user_id, elo_rating) VALUES
(1, 1315),  
(2, 1315);
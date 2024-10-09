use chess_tms;




CREATE TABLE user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('PLAYER', 'ADMIN') NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);




CREATE TABLE player_details (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  elo_rating INT,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  country VARCHAR(100),
  profile_picture VARCHAR(255),
  total_wins INT DEFAULT 0,
  total_losses INT DEFAULT 0,
  total_matches INT DEFAULT 0,
  highest_elo INT,
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);




CREATE TABLE game_type (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  time_control_minutes INT NOT NULL
);




CREATE TABLE round_type (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  round_name VARCHAR(50) NOT NULL,
  number_of_players INT NOT NULL
);




CREATE TABLE tournament (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  created_by BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  start_date DATETIME NOT NULL,
  end_date DATETIME NOT NULL,
  min_elo INT,
  max_elo INT,
  current_players INT NOT NULL,
  max_players INT NOT NULL,
  status ENUM("EXPIRED", "LIVE", "UPCOMING", "COMPLETED") NOT NULL DEFAULT 'UPCOMING',
  time_control BIGINT NOT NULL,
  current_round BIGINT,
  winner_id BIGINT,
  FOREIGN KEY (current_round) REFERENCES round_type(id),
  FOREIGN KEY (time_control) REFERENCES game_type(id),
  FOREIGN KEY (created_by) REFERENCES user(id),
  FOREIGN KEY (winner_id) REFERENCES player_details(id)
);




CREATE TABLE tournament_player (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tournament_id BIGINT NOT NULL,
  player_id BIGINT NOT NULL,
  FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
  FOREIGN KEY (player_id) REFERENCES player_details(id) ON DELETE CASCADE
);








CREATE TABLE matches (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tournament_id BIGINT NOT NULL,
  game_type_id BIGINT NOT NULL,
  player1_id BIGINT NULL,
  player2_id BIGINT NULL,
  winner_id BIGINT NULL,
  loser_id BIGINT NULL,
  round_type_id BIGINT NOT NULL,
  next_match_id BIGINT NULL,
  status ENUM('ONGOING', 'PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
  FOREIGN KEY (player1_id) REFERENCES player_details(id),
  FOREIGN KEY (player2_id) REFERENCES player_details(id),
  FOREIGN KEY (winner_id) REFERENCES player_details(id),
  FOREIGN KEY (loser_id) REFERENCES player_details(id),
  FOREIGN KEY (round_type_id) REFERENCES round_type(id),
  FOREIGN KEY (next_match_id) REFERENCES matches(id),
  FOREIGN KEY (game_type_id) REFERENCES game_type(id)
);




CREATE TABLE elo_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  player_id BIGINT NOT NULL,
  old_elo INT NOT NULL,
  new_elo INT NOT NULL,
  change_reason ENUM('WIN', 'LOSS', 'DRAW') NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (player_id) REFERENCES player_details(id) ON DELETE CASCADE
);




CREATE TABLE leaderboard (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  player_id BIGINT NOT NULL,
  elo_rating INT NOT NULL,
  ranking INT NOT NULL,
  last_updated TIMESTAMP,
  FOREIGN KEY (player_id) REFERENCES player_details(id) ON DELETE CASCADE
);




INSERT INTO round_type (round_name, number_of_players) VALUES
('Finals', 2),
('Semi Finals', 4),
('Quarter Finals', 8),
('Top 16', 16),
('Top 32', 32),
('Top 64', 64);








INSERT INTO user (username, email, password, role, created_at, updated_at)
VALUES
('admin1', 'admin1@example.com', '$2a$10$6bZUlV1zkqD6kqFGrP1XduJzZ1tiZa8gj0W5HbXUls1p.jYtEbcJm', 'ADMIN', NOW(), NOW()),
('admin2', 'admin2@example.com', '$2a$10$CzKz4xLXlB5FY5twYsT.5euRr71h9n0icEMTzxY8qFdIW/.pM6vW.', 'ADMIN', NOW(), NOW());








-- Insert dummy data for 20 players with hashed password "pass"
INSERT INTO user (username, email, password, role, created_at, updated_at)
VALUES
('player1', 'player1@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player2', 'player2@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player3', 'player3@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player4', 'player4@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player5', 'player5@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player6', 'player6@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player7', 'player7@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player8', 'player8@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player9', 'player9@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player10', 'player10@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player11', 'player11@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player12', 'player12@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player13', 'player13@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player14', 'player14@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player15', 'player15@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player16', 'player16@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player17', 'player17@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player18', 'player18@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player19', 'player19@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player20', 'player20@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW());




-- Insert corresponding player details for each player (ID 3 to 22)
INSERT INTO player_details (user_id, elo_rating, country, first_name, last_name, profile_picture, total_wins, total_losses, total_matches, highest_elo, lowest_elo)
VALUES
(1, 1100, 'USA', 'Player', 'One', 'profile1.jpg', 10, 2, 12, 1150, 1050),
(2, 1200, 'UK', 'Player', 'Two', 'profile2.jpg', 12, 4, 16, 1220, 1100),
(3, 1300, 'Canada', 'Player', 'Three', 'profile3.jpg', 14, 3, 17, 1350, 1200),
(4, 1400, 'India', 'Player', 'Four', 'profile4.jpg', 13, 5, 18, 1420, 1300),
(5, 1250, 'Germany', 'Player', 'Five', 'profile5.jpg', 11, 3, 14, 1300, 1200),
(6, 1350, 'France', 'Player', 'Six', 'profile6.jpg', 12, 2, 14, 1400, 1250),
(7, 1450, 'Brazil', 'Player', 'Seven', 'profile7.jpg', 15, 5, 20, 1500, 1350),
(8, 1150, 'Japan', 'Player', 'Eight', 'profile8.jpg', 10, 6, 16, 1200, 1100),
(9, 1550, 'Russia', 'Player', 'Nine', 'profile9.jpg', 18, 4, 22, 1600, 1450),
(10, 1050, 'China', 'Player', 'Ten', 'profile10.jpg', 8, 10, 18, 1100, 1000),
(11, 1250, 'Spain', 'Player', 'Eleven', 'profile11.jpg', 13, 5, 18, 1300, 1150),
(12, 1350, 'Italy', 'Player', 'Twelve', 'profile12.jpg', 14, 6, 20, 1400, 1200),
(13, 1400, 'Netherlands', 'Player', 'Thirteen', 'profile13.jpg', 12, 3, 15, 1450, 1300),
(14, 1300, 'Australia', 'Player', 'Fourteen', 'profile14.jpg', 11, 4, 15, 1350, 1200),
(15, 1250, 'South Africa', 'Player', 'Fifteen', 'profile15.jpg', 12, 6, 18, 1300, 1150),
(16, 1450, 'Argentina', 'Player', 'Sixteen', 'profile16.jpg', 15, 5, 20, 1500, 1350),
(17, 1150, 'South Korea', 'Player', 'Seventeen', 'profile17.jpg', 10, 8, 18, 1200, 1100),
(18, 1500, 'Mexico', 'Player', 'Eighteen', 'profile18.jpg', 16, 4, 20, 1550, 1400),
(19, 1100, 'Egypt', 'Player', 'Nineteen', 'profile19.jpg', 9, 9, 18, 1150, 1050),
(20, 1000, 'Kenya', 'Player', 'Twenty', 'profile20.jpg', 8, 10, 18, 1050, 950);




-- Insert dummy data into game_type table
INSERT INTO game_type (name, time_control_minutes)
VALUES
('Blitz', 5),           -- Blitz chess (5 minutes per player)
('Rapid', 25),          -- Rapid chess (25 minutes per player)
('Classical', 90),      -- Classical chess (90 minutes per player)
('Bullet', 1),          -- Bullet chess (1 minute per player)
('Armageddon', 5),      -- Armageddon (Blitz tiebreak game)
('Fischer Random', 15); -- Fischer Random chess (15 minutes per player)




-- Insert dummy data into the `tournament` table
INSERT INTO tournament (created_by, name, start_date, end_date, min_elo, max_elo,current_players, total_players, status, time_control)
VALUES
(1, 'Chess Rapid Championship', '2024-10-01 10:00:00', '2024-10-02 18:00:00', 1200, 2400, 16, 16, 'UPCOMING', 1),  -- Time control ID 1 -> Rapid
(2, 'Blitz Open Tournament', '2024-11-10 09:00:00', '2024-11-10 20:00:00', 1000, 2200,20, 32, 'UPCOMING', 2),     -- Time control ID 2 -> Blitz
(1, 'Classic Masters Event', '2024-12-05 10:00:00', '2024-12-06 18:00:00', 1400, 2600,8 , 8, 'UPCOMING', 3);      -- Time control ID 3 -> Classic




-- Insert dummy data into the `tournament_player` table
-- For 'Chess Rapid Championship'
INSERT INTO tournament_player (player_id, tournament_id)
VALUES
(3, 1),  -- Player with ID 3 in tournament ID 1
(4, 1),
(5, 1),
(6, 1),
(7, 1),
(8, 1),
(9, 1),
(10, 1),
(11, 1),
(12, 1),
(13, 1),
(14, 1),
(15, 1),
(16, 1),
(17, 1),
(18, 1);  -- Total 16 players




-- For 'Blitz Open Tournament'
INSERT INTO tournament_player (player_id, tournament_id)
VALUES
(1, 2),  -- Player with ID 1 in tournament ID 2
(2, 2),
(3, 2),
(4, 2),
(5, 2),
(6, 2),
(7, 2),
(8, 2),
(9, 2),
(10, 2),
(11, 2),
(12, 2),
(13, 2),
(14, 2),
(15, 2),
(16, 2),
(17, 2),
(18, 2),
(19, 2),
(20, 2);  -- Total 20 players




-- For 'Classic Masters Event'
INSERT INTO tournament_player (player_id, tournament_id)
VALUES
(5, 3),  -- Player with ID 5 in tournament ID 3
(6, 3),
(7, 3),
(8, 3),
(9, 3),
(10, 3),
(11, 3),
(12, 3);  -- Total 8 players
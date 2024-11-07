CREATE TABLE "user" (
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
  FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
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

CREATE TABLE tournament_type(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type_name VARCHAR(50) NOT NULL
);

CREATE TABLE tournament (
  id bigint AUTO_INCREMENT PRIMARY KEY,
  created_by BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  format ENUM('ONLINE', 'HYBRID', 'PHYSICAL') NOT NULL DEFAULT 'ONLINE',
  tournament_type BIGINT NOT NULL,
  description TEXT,
  photo VARCHAR(255),
  country VARCHAR(100),
  location_address VARCHAR(255),
  location_longitude DECIMAL,
  location_latitude DECIMAL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  min_elo INT,
  max_elo INT,
  current_players INT NOT NULL,
  max_players INT NOT NULL,
  status ENUM('EXPIRED', 'LIVE', 'UPCOMING', 'COMPLETED') NOT NULL DEFAULT 'UPCOMING',
  time_control BIGINT NOT NULL,
  current_round BIGINT,
  winner_id BIGINT,
  FOREIGN KEY (current_round) REFERENCES round_type(id),
  FOREIGN KEY (tournament_type) REFERENCES tournament_type(id),
  FOREIGN KEY (time_control) REFERENCES game_type(id),
  FOREIGN KEY (created_by) REFERENCES "user"(id),
  FOREIGN KEY (winner_id) REFERENCES player_details(id)
);

CREATE TABLE tournament_player (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tournament_id BIGINT NOT NULL,
  player_id BIGINT NOT NULL,
  FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
  FOREIGN KEY (player_id) REFERENCES player_details(id) ON DELETE CASCADE
);


CREATE TABLE swiss_bracket(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    number_of_rounds int NOT NULL DEFAULT 0,
    current_round int not null DEFAULT 0,
	FOREIGN KEY (tournament_id) REFERENCES tournament(id)
);

CREATE TABLE swiss_standing (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bracket_id BIGINT NOT NULL,
    tournament_player_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    wins INT NOT NULL DEFAULT 0,
    losses INT NOT NULL DEFAULT 0,
    FOREIGN KEY (tournament_player_id) REFERENCES tournament_player(id),
	FOREIGN KEY (bracket_id) REFERENCES swiss_bracket(id),
	FOREIGN KEY (player_id) REFERENCES player_details(id)
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
  swiss_round_number INT NULL,
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

CREATE TABLE otp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);


INSERT INTO "user" (username, email, password, role) VALUES
('john_doe', 'john.doe@example.com', 'hashed_password1', 'PLAYER'),
('jane_doe', 'jane.doe@example.com', 'hashed_password2', 'PLAYER');

select * from "user";

INSERT INTO player_details (user_id, elo_rating) VALUES
(1, 1315),  
(2, 1315);
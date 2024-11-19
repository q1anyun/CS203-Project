CREATE DATABASE IF NOT EXISTS chess_tms;

use chess_tms;

--------------------------------- TABLES ---------------------------------

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
  profile_picture LONGTEXT,
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

CREATE TABLE tournament_type(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type_name VARCHAR(50) NOT NULL
);

CREATE TABLE tournament (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    format ENUM("ONLINE", "HYBRID", "PHYSICAL") NOT NULL DEFAULT 'ONLINE',
    tournament_type BIGINT NOT NULL,
    description TEXT,
    photo VARCHAR(255),
    country VARCHAR(100),
    location_address VARCHAR(255),
    location_longitude DECIMAL(9,6),
    location_latitude DECIMAL(9,6),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    min_elo INT,
    max_elo INT,
    current_players INT NOT NULL,
    max_players INT NOT NULL,
    status ENUM("EXPIRED", "LIVE", "UPCOMING", "COMPLETED") NOT NULL DEFAULT 'UPCOMING',
    time_control BIGINT NOT NULL,
    current_round BIGINT,
    winner_id BIGINT,
    FOREIGN KEY (current_round) REFERENCES round_type(id),
    FOREIGN KEY (tournament_type) REFERENCES tournament_type(id),
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

CREATE TABLE swiss_bracket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    number_of_rounds INT NOT NULL DEFAULT 0,
    current_round INT NOT NULL DEFAULT 0,
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE
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

--------------------------------- DATA ---------------------------------

-- Insert dummy data into round_type table
INSERT INTO round_type (round_name, number_of_players) VALUES
('Finals', 2),
('Semi Finals', 4),
('Quarter Finals', 8),
('Top 16', 16),
('Top 32', 32),
('Top 64', 64),
('Swiss', 64);

-- Insert dummy data into tournament_type table
INSERT INTO tournament_type (type_name) VALUES
('Knockout'),
('Swiss');

-- Insert dummy admin data into user table
INSERT INTO user (username, email, password, role, created_at, updated_at)
VALUES
('admin1', 'admin1@example.com', '$2a$10$k8TXJJX/feUw0eSYfSa5ruhMLgVToT.Wfs8FiYYTefYounkTgA3X6', 'ADMIN', NOW(), NOW()),
('admin2', 'admin2@example.com', '$2a$10$CzKz4xLXlB5FY5twYsT.5euRr71h9n0icEMTzxY8qFdIW/.pM6vW.', 'ADMIN', NOW(), NOW());

-- Insert dummy player data into user table
INSERT INTO user (username, email, password, role, created_at, updated_at)
VALUES
('player1', 'player1@example.com', '$2a$10$k8TXJJX/feUw0eSYfSa5ruhMLgVToT.Wfs8FiYYTefYounkTgA3X6', 'PLAYER', NOW(), NOW()),
('player2', 'player2@example.com', '$2a$10$7QH1gPz7JvF3kVYjThEhTuFZUIO4Y7HIGePmRoM0FdN2UdfSgPaNi', 'PLAYER', NOW(), NOW()),
('player3', 'player3@example.com', '$2a$10$k8TXJJX/feUw0eSYfSa5ruhMLgVToT.Wfs8FiYYTefYounkTgA3X6', 'PLAYER', NOW(), NOW()),
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




-- Insert dummy data into player_details table
INSERT INTO player_details (user_id, elo_rating, country, first_name, last_name, profile_picture, total_wins, total_losses, total_matches, highest_elo)
VALUES
(1, 1520, 'USA', 'Player', 'One', 'profile1.jpg', 1, 1, 2, 1540),
(2, 1380, 'UK', 'Player', 'Two', 'profile2.jpg', 0, 1, 1, 1400),
(3, 1530, 'Singapore', 'Magnus', 'Lim', 'profile3.jpg', 6, 0, 6, 1520),
(4, 1410, 'India', 'Player', 'Four', 'profile4.jpg', 1, 3, 4, 1450),
(5, 1380, 'Germany', 'Player', 'Five', 'profile5.jpg', 0, 1, 1, 1400),
(6, 1460, 'France', 'Player', 'Six', 'profile6.jpg', 4, 2, 6, 1500),
(7, 1330, 'Brazil', 'Player', 'Seven', 'profile7.jpg', 0, 1, 1, 1350),
(8, 1370, 'Japan', 'Player', 'Eight', 'profile8.jpg', 2, 2, 4, 1400),
(9, 1500, 'Russia', 'Player', 'Nine', 'profile9.jpg', 2, 2, 4, 1520),
(10, 1450, 'China', 'Player', 'Ten', 'profile10.jpg', 0, 1, 1, 1470),
(11, 500, 'Spain', 'Player', 'Eleven', 'profile11.jpg', 0, 0, 0, 500),
(12, 500, 'Italy', 'Player', 'Twelve', 'profile12.jpg', 0, 0, 0, 500),
(13, 600, 'Netherlands', 'Player', 'Thirteen', 'profile13.jpg', 0, 0, 0, 500),
(14, 800, 'Australia', 'Player', 'Fourteen', 'profile14.jpg', 0, 0, 0, 500),
(15, 540, 'South Africa', 'Player', 'Fifteen', 'profile15.jpg', 0, 0, 0, 500),
(16, 520, 'Argentina', 'Player', 'Sixteen', 'profile16.jpg', 0, 0, 0, 500),
(17, 980, 'South Korea', 'Player', 'Seventeen', 'profile17.jpg', 0, 0, 0, 500),
(18, 1100, 'Mexico', 'Player', 'Eighteen', 'profile18.jpg', 0, 0, 0, 500),
(19, 580, 'Egypt', 'Player', 'Nineteen', 'profile19.jpg', 0, 0, 0, 500),
(20, 590, 'Kenya', 'Player', 'Twenty', 'profile20.jpg', 0, 0, 0, 500);

-- Insert dummy data into user table
INSERT INTO user (id, username, email, password, role) VALUES
(101, 'player_101', 'player101@example.com', 'password101', 'PLAYER'),
(102, 'player_102', 'player102@example.com', 'password102', 'PLAYER'),
(103, 'player_103', 'player103@example.com', 'password103', 'PLAYER'),
(104, 'player_104', 'player104@example.com', 'password104', 'PLAYER'),
(105, 'player_105', 'player105@example.com', 'password105', 'PLAYER'),
(106, 'player_106', 'player106@example.com', 'password106', 'PLAYER'),
(107, 'player_107', 'player107@example.com', 'password107', 'PLAYER'),
(108, 'player_108', 'player108@example.com', 'password108', 'PLAYER'),
(109, 'player_109', 'player109@example.com', 'password109', 'PLAYER'),
(110, 'player_110', 'player110@example.com', 'password110', 'PLAYER'),
(111, 'player_111', 'player111@example.com', 'password111', 'PLAYER'),
(112, 'player_112', 'player112@example.com', 'password112', 'PLAYER'),
(113, 'player_113', 'player113@example.com', 'password113', 'PLAYER'),
(114, 'player_114', 'player114@example.com', 'password114', 'PLAYER'),
(115, 'player_115', 'player115@example.com', 'password115', 'PLAYER'),
(116, 'player_116', 'player116@example.com', 'password116', 'PLAYER'),
(117, 'player_117', 'player117@example.com', 'password117', 'PLAYER'),
(118, 'player_118', 'player118@example.com', 'password118', 'PLAYER'),
(119, 'player_119', 'player119@example.com', 'password119', 'PLAYER'),
(120, 'player_120', 'player120@example.com', 'password120', 'PLAYER'),
(121, 'player_121', 'player121@example.com', 'password121', 'PLAYER'),
(122, 'player_122', 'player122@example.com', 'password122', 'PLAYER'),
(123, 'player_123', 'player123@example.com', 'password123', 'PLAYER'),
(124, 'player_124', 'player124@example.com', 'password124', 'PLAYER'),
(125, 'player_125', 'player125@example.com', 'password125', 'PLAYER'),
(126, 'player_126', 'player126@example.com', 'password126', 'PLAYER'),
(127, 'player_127', 'player127@example.com', 'password127', 'PLAYER'),
(128, 'player_128', 'player128@example.com', 'password128', 'PLAYER'),
(129, 'player_129', 'player129@example.com', 'password129', 'PLAYER'),
(130, 'player_130', 'player130@example.com', 'password130', 'PLAYER'),
(131, 'player_131', 'player131@example.com', 'password131', 'PLAYER'),
(132, 'player_132', 'player132@example.com', 'password132', 'PLAYER'),
(133, 'player_133', 'player133@example.com', 'password133', 'PLAYER'),
(134, 'player_134', 'player134@example.com', 'password134', 'PLAYER'),
(135, 'player_135', 'player135@example.com', 'password135', 'PLAYER'),
(136, 'player_136', 'player136@example.com', 'password136', 'PLAYER'),
(137, 'player_137', 'player137@example.com', 'password137', 'PLAYER'),
(138, 'player_138', 'player138@example.com', 'password138', 'PLAYER'),
(139, 'player_139', 'player139@example.com', 'password139', 'PLAYER'),
(140, 'player_140', 'player140@example.com', 'password140', 'PLAYER'),
(141, 'player_141', 'player141@example.com', 'password141', 'PLAYER'),
(142, 'player_142', 'player142@example.com', 'password142', 'PLAYER'),
(143, 'player_143', 'player143@example.com', 'password143', 'PLAYER'),
(144, 'player_144', 'player144@example.com', 'password144', 'PLAYER'),
(145, 'player_145', 'player145@example.com', 'password145', 'PLAYER'),
(146, 'player_146', 'player146@example.com', 'password146', 'PLAYER'),
(147, 'player_147', 'player147@example.com', 'password147', 'PLAYER'),
(148, 'player_148', 'player148@example.com', 'password148', 'PLAYER'),
(149, 'player_149', 'player149@example.com', 'password149', 'PLAYER'),
(150, 'player_150', 'player150@example.com', 'password150', 'PLAYER'),
(151, 'player_151', 'player151@example.com', 'password151', 'PLAYER'),
(152, 'player_152', 'player152@example.com', 'password152', 'PLAYER'),
(153, 'player_153', 'player153@example.com', 'password153', 'PLAYER'),
(154, 'player_154', 'player154@example.com', 'password154', 'PLAYER'),
(155, 'player_155', 'player155@example.com', 'password155', 'PLAYER'),
(156, 'player_156', 'player156@example.com', 'password156', 'PLAYER'),
(157, 'player_157', 'player157@example.com', 'password157', 'PLAYER'),
(158, 'player_158', 'player158@example.com', 'password158', 'PLAYER'),
(159, 'player_159', 'player159@example.com', 'password159', 'PLAYER'),
(160, 'player_160', 'player160@example.com', 'password160', 'PLAYER'),
(161, 'player_161', 'player161@example.com', 'password161', 'PLAYER'),
(162, 'player_162', 'player162@example.com', 'password162', 'PLAYER'),
(163, 'player_163', 'player163@example.com', 'password163', 'PLAYER'),
(164, 'player_164', 'player164@example.com', 'password164', 'PLAYER'),
(165, 'player_165', 'player165@example.com', 'password165', 'PLAYER'),
(166, 'player_166', 'player166@example.com', 'password166', 'PLAYER'),
(167, 'player_167', 'player167@example.com', 'password167', 'PLAYER'),
(168, 'player_168', 'player168@example.com', 'password168', 'PLAYER'),
(169, 'player_169', 'player169@example.com', 'password169', 'PLAYER'),
(170, 'player_170', 'player170@example.com', 'password170', 'PLAYER'),
(171, 'player_171', 'player171@example.com', 'password171', 'PLAYER'),
(172, 'player_172', 'player172@example.com', 'password172', 'PLAYER'),
(173, 'player_173', 'player173@example.com', 'password173', 'PLAYER'),
(174, 'player_174', 'player174@example.com', 'password174', 'PLAYER'),
(175, 'player_175', 'player175@example.com', 'password175', 'PLAYER'),
(176, 'player_176', 'player176@example.com', 'password176', 'PLAYER'),
(177, 'player_177', 'player177@example.com', 'password177', 'PLAYER'),
(178, 'player_178', 'player178@example.com', 'password178', 'PLAYER'),
(179, 'player_179', 'player179@example.com', 'password179', 'PLAYER'),
(180, 'player_180', 'player180@example.com', 'password180', 'PLAYER'),
(181, 'player_181', 'player181@example.com', 'password181', 'PLAYER'),
(182, 'player_182', 'player182@example.com', 'password182', 'PLAYER'),
(183, 'player_183', 'player183@example.com', 'password183', 'PLAYER'),
(184, 'player_184', 'player184@example.com', 'password184', 'PLAYER'),
(185, 'player_185', 'player185@example.com', 'password185', 'PLAYER'),
(186, 'player_186', 'player186@example.com', 'password186', 'PLAYER'),
(187, 'player_187', 'player187@example.com', 'password187', 'PLAYER'),
(188, 'player_188', 'player188@example.com', 'password188', 'PLAYER'),
(189, 'player_189', 'player189@example.com', 'password189', 'PLAYER'),
(190, 'player_190', 'player190@example.com', 'password190', 'PLAYER'),
(191, 'player_191', 'player191@example.com', 'password191', 'PLAYER'),
(192, 'player_192', 'player192@example.com', 'password192', 'PLAYER'),
(193, 'player_193', 'player193@example.com', 'password193', 'PLAYER'),
(194, 'player_194', 'player194@example.com', 'password194', 'PLAYER'),
(195, 'player_195', 'player195@example.com', 'password195', 'PLAYER'),
(196, 'player_196', 'player196@example.com', 'password196', 'PLAYER'),
(197, 'player_197', 'player197@example.com', 'password197', 'PLAYER'),
(198, 'player_198', 'player198@example.com', 'password198', 'PLAYER'),
(199, 'player_199', 'player199@example.com', 'password199', 'PLAYER'),
(200, 'player_200', 'player200@example.com', 'password200', 'PLAYER');

-- Insert dummy data into player_details table
INSERT INTO player_details (user_id, elo_rating, first_name, last_name, country, profile_picture, highest_elo) VALUES
(101, 1500, 'John', 'Doe', 'USA', '../../assets/default_user.png', 1600),
(102, 1450, 'Jane', 'Smith', 'Canada', '../../assets/default_user.png', 1550),
(103, 1600, 'Alice', 'Brown', 'UK', '../../assets/default_user.png', 1700),
(104, 1550, 'Bob', 'Jones', 'Australia', '../../assets/default_user.png', 1650),
(105, 1480, 'Chris', 'Taylor', 'Germany', '../../assets/default_user.png', 1580),
(106, 1520, 'Anna', 'Wilson', 'France', '../../assets/default_user.png', 1620),
(107, 1430, 'Emily', 'Clark', 'Italy', '../../assets/default_user.png', 1530),
(108, 1490, 'David', 'Miller', 'Spain', '../../assets/default_user.png', 1590),
(109, 1470, 'Sophia', 'Moore', 'Brazil', '../../assets/default_user.png', 1570),
(110, 1540, 'James', 'Anderson', 'Russia', '../../assets/default_user.png', 1640),
(111, 1510, 'Olivia', 'Thompson', 'Japan', '../../assets/default_user.png', 1610),
(112, 1460, 'Mia', 'Taylor', 'India', '../../assets/default_user.png', 1560),
(113, 1580, 'Lucas', 'Walker', 'China', '../../assets/default_user.png', 1680),
(114, 1440, 'Liam', 'White', 'Netherlands', '../../assets/default_user.png', 1540),
(115, 1530, 'Isabella', 'Harris', 'Mexico', '../../assets/default_user.png', 1630),
(116, 1500, 'Noah', 'Green', 'South Africa', '../../assets/default_user.png', 1600),
(117, 1420, 'Ava', 'Hill', 'Sweden', '../../assets/default_user.png', 1520),
(118, 1480, 'Benjamin', 'Scott', 'New Zealand', '../../assets/default_user.png', 1580),
(119, 1550, 'Charlotte', 'Adams', 'Norway', '../../assets/default_user.png', 1650),
(120, 1450, 'Elijah', 'Baker', 'Denmark', '../../assets/default_user.png', 1550),
(121, 1530, 'William', 'Carter', 'South Korea', '../../assets/default_user.png', 1630),
(122, 1440, 'Amelia', 'Parker', 'Finland', '../../assets/default_user.png', 1540),
(123, 1490, 'Ethan', 'Evans', 'Argentina', '../../assets/default_user.png', 1590),
(124, 1520, 'Abigail', 'Mitchell', 'Nigeria', '../../assets/default_user.png', 1620),
(125, 1470, 'Sebastian', 'Roberts', 'Chile', '../../assets/default_user.png', 1570),
(126, 1500, 'Victoria', 'Turner', 'Greece', '../../assets/default_user.png', 1600),
(127, 1450, 'Matthew', 'Phillips', 'Portugal', '../../assets/default_user.png', 1550),
(128, 1560, 'Ella', 'Campbell', 'Turkey', '../../assets/default_user.png', 1660),
(129, 1410, 'Harper', 'Parker', 'Poland', '../../assets/default_user.png', 1510),
(130, 1570, 'Alexander', 'Hughes', 'Ireland', '../../assets/default_user.png', 1670),
(131, 1430, 'Mason', 'Lee', 'Belgium', '../../assets/default_user.png', 1530),
(132, 1490, 'Logan', 'Scott', 'Switzerland', '../../assets/default_user.png', 1590),
(133, 1480, 'Aiden', 'Kelly', 'Austria', '../../assets/default_user.png', 1580),
(134, 1540, 'Daniel', 'Russell', 'Peru', '../../assets/default_user.png', 1640),
(135, 1450, 'Jackson', 'Coleman', 'Israel', '../../assets/default_user.png', 1550),
(136, 1500, 'Henry', 'Cruz', 'Colombia', '../../assets/default_user.png', 1600),
(137, 1460, 'Grace', 'Peterson', 'Saudi Arabia', '../../assets/default_user.png', 1560),
(138, 1590, 'Owen', 'Bennett', 'Malaysia', '../../assets/default_user.png', 1690),
(139, 1440, 'Lily', 'Patterson', 'Indonesia', '../../assets/default_user.png', 1540),
(140, 1530, 'Samuel', 'Butler', 'Philippines', '../../assets/default_user.png', 1630),
(141, 1500, 'Victoria', 'Brooks', 'Thailand', '../../assets/default_user.png', 1600),
(142, 1420, 'Joseph', 'Reed', 'Ukraine', '../../assets/default_user.png', 1520),
(143, 1490, 'Jack', 'Hughes', 'Singapore', '../../assets/default_user.png', 1590),
(144, 1550, 'Charlie', 'Foster', 'Morocco', '../../assets/default_user.png', 1650),
(145, 1470, 'Leo', 'Sanders', 'Czech Republic', '../../assets/default_user.png', 1570),
(146, 1540, 'Aria', 'Murphy', 'Romania', '../../assets/default_user.png', 1640),
(147, 1510, 'Eleanor', 'Powell', 'Hungary', '../../assets/default_user.png', 1610),
(148, 1480, 'Nathan', 'Rivera', 'Egypt', '../../assets/default_user.png', 1580),
(149, 1600, 'Ryan', 'Price', 'Vietnam', '../../assets/default_user.png', 1700),
(150, 1460, 'Leah', 'Sullivan', 'Iran', '../../assets/default_user.png', 1560),
(151, 1570, 'Isaac', 'Stewart', 'Serbia', '../../assets/default_user.png', 1670),
(152, 1430, 'Avery', 'Griffin', 'Kenya', '../../assets/default_user.png', 1530),
(153, 1550, 'Sebastian', 'Mitchell', 'Venezuela', '../../assets/default_user.png', 1650),
(154, 1500, 'Harper', 'Bailey', 'Pakistan', '../../assets/default_user.png', 1600),
(155, 1440, 'Dylan', 'Wood', 'Bangladesh', '../../assets/default_user.png', 1540),
(156, 1510, 'Zoey', 'Barnes', 'Sri Lanka', '../../assets/default_user.png', 1610),
(157, 1490, 'Chloe', 'Morris', 'Iraq', '../../assets/default_user.png', 1590),
(158, 1520, 'Isaiah', 'Nguyen', 'Afghanistan', '../../assets/default_user.png', 1620),
(159, 1470, 'Carter', 'Rivera', 'Uzbekistan', '../../assets/default_user.png', 1570),
(160, 1530, 'Gabriel', 'Cook', 'Kazakhstan', '../../assets/default_user.png', 1630),
(161, 1460, 'Landon', 'Rogers', 'Cambodia', '../../assets/default_user.png', 1560),
(162, 1580, 'Penelope', 'Jenkins', 'Nepal', '../../assets/default_user.png', 1680),
(163, 1420, 'Hudson', 'Foster', 'Laos', '../../assets/default_user.png', 1520),
(164, 1560, 'Henry', 'Simmons', 'Mongolia', '../../assets/default_user.png', 1660),
(165, 1500, 'Lucy', 'Ward', 'Bhutan', '../../assets/default_user.png', 1600),
(166, 1540, 'Levi', 'Morris', 'Oman', '../../assets/default_user.png', 1640),
(167, 1470, 'Zoe', 'Gray', 'Jordan', '../../assets/default_user.png', 1570),
(168, 1510, 'Jack', 'Jenkins', 'Kuwait', '../../assets/default_user.png', 1610),
(169, 1490, 'Nolan', 'Garcia', 'Bahrain', '../../assets/default_user.png', 1590),
(170, 1450, 'Grace', 'Garcia', 'Syria', '../../assets/default_user.png', 1550),
(171, 1600, 'Mason', 'Alvarez', 'Yemen', '../../assets/default_user.png', 1700),
(172, 1470, 'Samuel', 'Lopez', 'Armenia', '../../assets/default_user.png', 1570),
(173, 1530, 'Scarlett', 'Gomez', 'Kyrgyzstan', '../../assets/default_user.png', 1630),
(174, 1520, 'Olivia', 'Perry', 'Tajikistan', '../../assets/default_user.png', 1620),
(175, 1490, 'Jaxon', 'Barnes', 'Turkmenistan', '../../assets/default_user.png', 1590),
(176, 1540, 'Victoria', 'Mendoza', 'Qatar', '../../assets/default_user.png', 1640),
(177, 1500, 'Liam', 'James', 'Palestine', '../../assets/default_user.png', 1600),
(178, 1450, 'Kylie', 'Nelson', 'Mali', '../../assets/default_user.png', 1550),
(179, 1570, 'Matthew', 'Alexander', 'Azerbaijan', '../../assets/default_user.png', 1670),
(180, 1510, 'Samantha', 'Wilson', 'Cyprus', '../../assets/default_user.png', 1610),
(181, 1460, 'Zachary', 'Cooper', 'Malta', '../../assets/default_user.png', 1560),
(182, 1500, 'Charlotte', 'Flores', 'Singapore', '../../assets/default_user.png', 1600),
(183, 1480, 'Abigail', 'Rivera', 'Seychelles', '../../assets/default_user.png', 1580),
(184, 1540, 'Asher', 'Curtis', 'Kosovo', '../../assets/default_user.png', 1640),
(185, 1450, 'Dylan', 'Woods', 'Montenegro', '../../assets/default_user.png', 1550),
(186, 1520, 'Zane', 'Wright', 'Macedonia', '../../assets/default_user.png', 1620),
(187, 1470, 'Hazel', 'Parker', 'Bosnia and Herzegovina', '../../assets/default_user.png', 1570),
(188, 1490, 'Sophie', 'Ferguson', 'Georgia', '../../assets/default_user.png', 1590),
(189, 1510, 'Caleb', 'Reid', 'Serbia', '../../assets/default_user.png', 1610),
(190, 1430, 'Eliza', 'Hayes', 'North Korea', '../../assets/default_user.png', 1530),
(191, 1560, 'Jack', 'Morgan', 'Bulgaria', '../../assets/default_user.png', 1660),
(192, 1550, 'Isaac', 'Foster', 'Czechia', '../../assets/default_user.png', 1650),
(193, 1500, 'Bella', 'Davis', 'Nepal', '../../assets/default_user.png', 1600),
(194, 1470, 'Nathan', 'Fletcher', 'Kosovo', '../../assets/default_user.png', 1570),
(195, 1540, 'Elijah', 'Curtis', 'Croatia', '../../assets/default_user.png', 1640),
(196, 1460, 'Emma', 'Phillips', 'Algeria', '../../assets/default_user.png', 1560),
(197, 1480, 'Aidan', 'Griffin', 'Slovenia', '../../assets/default_user.png', 1580),
(198, 1510, 'Henry', 'Morris', 'Bulgaria', '../../assets/default_user.png', 1610),
(199, 1600, 'Lillian', 'Murphy', 'Switzerland', '../../assets/default_user.png', 1700),
(200, 1550, 'Ella', 'Fletcher', 'Estonia', '../../assets/default_user.png', 1650);


-- Insert dummy data into game_type table
INSERT INTO game_type (name, time_control_minutes)
VALUES
('Blitz', 5),           -- Blitz chess (5 minutes per player)
('Rapid', 25),          -- Rapid chess (25 minutes per player)
('Classical', 90),      -- Classical chess (90 minutes per player)
('Bullet', 1),          -- Bullet chess (1 minute per player)
('Armageddon', 5),      -- Armageddon (Blitz tiebreak game)
('Fischer Random', 15); -- Fischer Random chess (15 minutes per player)


-- Insert dummy data into the tournament table
INSERT INTO tournament (created_by, name, tournament_type, description, start_date, end_date, min_elo, max_elo, current_players, max_players, status, time_control, format, country, location_address, location_longitude, location_latitude)
VALUES
-- ONLINE tournaments
(1, 'Chess Rapid Championship', 1, 'Rapid championship held entirely online.', '2024-10-01 10:00:00', '2024-10-02 18:00:00', 1200, 2400, 16, 16, 'UPCOMING', 1, 'ONLINE', NULL, NULL, NULL, NULL),
(2, 'Blitz Open Tournament', 1, 'Blitz tournament for players of all levels, online.', '2024-11-10 09:00:00', '2024-11-10 20:00:00', 1000, 2200, 20, 32, 'UPCOMING', 2, 'ONLINE', NULL, NULL, NULL, NULL),

-- PHYSICAL tournaments
(1, 'Classic Masters Event', 2, 'Prestigious classical chess event held in-person.', '2024-12-05 10:00:00', '2024-12-06 18:00:00', 1400, 2600, 8, 8, 'UPCOMING', 3, 'PHYSICAL', 'USA', '123 Grandmaster Lane, New York, NY', -73.935242, 40.730610),
(2, 'Classic Open Tournament', 1, 'Open classical tournament held in-person.', '2024-10-16 10:00:00', '2024-10-18 18:00:00', 200, 3000, 3, 4, 'UPCOMING', 3, 'PHYSICAL', 'Germany', 'Berlin Chess Arena, Berlin', 13.4050, 52.5200),

-- HYBRID tournaments
(2, 'Hybrid Chess Championship 2024', 1, 'A hybrid event with both online and in-person players.', '2024-09-15 09:00:00', '2024-09-16 18:00:00', 1500, 2700, 20, 32, 'UPCOMING', 1, 'HYBRID', 'UK', 'London Chess Hall, London', -0.127758, 51.507351);

-- Add some completed tournaments
INSERT INTO tournament (created_by, name, tournament_type, description, start_date, end_date, min_elo, max_elo, current_players, max_players, status, time_control, current_round, winner_id, format, country, location_address, location_longitude, location_latitude)
VALUES
-- ONLINE tournament
(1, 'Rapid Chess Championship 2024', 1, 'Annual rapid chess tournament held online.', '2024-08-01 10:00:00', '2024-08-02 18:00:00', 1200, 2400, 8, 8, 'COMPLETED', 1, 1, 5, 'ONLINE', NULL, NULL, NULL, NULL),

-- PHYSICAL tournament
(2, 'Blitz Grand Masters 2024', 1, 'Grandmasters-only blitz tournament held in person.', '2024-09-15 09:00:00', '2024-09-15 20:00:00', 1000, 2200, 8, 8, 'COMPLETED', 2, 1, 12, 'PHYSICAL', 'France', 'Paris Chess Club, Paris', 2.3522, 48.8566);

-- Classic Open Tournament Data
-- Insert tournament players
INSERT INTO tournament_player (tournament_id, player_id)
VALUES
(4, 1),  -- Player 1
(4, 4),  -- Player 4
(4, 5);  -- Player 5

-- Rapid Chess Championship 2024 Data
-- Insert tournament players
INSERT INTO tournament_player (tournament_id, player_id)
VALUES
(5, 3),  -- Player 3
(5, 4),  -- Player 4
(5, 5),  -- Player 5
(5, 6),  -- Player 6
(5, 7),  -- Player 7
(5, 8),  -- Player 8
(5, 9),  -- Player 9
(5, 10); -- Player 10

-- Insert matches
INSERT INTO matches (tournament_id, game_type_id, player1_id, player2_id, winner_id, loser_id, round_type_id, status, created_at, updated_at)
VALUES
(5, 1, 3, 4, 3, 4, 3, 'COMPLETED', '2024-08-01 09:00:00', '2024-08-01 09:30:00'),  -- Match 1
(5, 1, 6, 5, 6, 5, 3, 'COMPLETED', '2024-08-01 09:45:00', '2024-08-01 10:15:00'),  -- Match 2
(5, 1, 8, 7, 8, 7, 3, 'COMPLETED', '2024-08-01 10:30:00', '2024-08-01 11:00:00'),  -- Match 3
(5, 1, 9, 10, 9, 10, 3, 'COMPLETED', '2024-08-01 11:15:00', '2024-08-01 11:45:00'), -- Match 4
(5, 1, 3, 8, 3, 8, 2, 'COMPLETED', '2024-08-02 09:00:00', '2024-08-02 09:30:00'),  -- Match 5 (Semifinal 1)
(5, 1, 6, 9, 6, 9, 2, 'COMPLETED', '2024-08-02 09:45:00', '2024-08-02 10:15:00'),  -- Match 6 (Semifinal 2)
(5, 1, 3, 6, 3, 6, 1, 'COMPLETED', '2024-08-02 11:00:00', '2024-08-02 11:30:00');  -- Match 7 (Final)

-- Update next_match_id for Quarterfinals to Semifinals
UPDATE matches m
JOIN (
    SELECT m1.id as quarterfinal_id, m2.id as semifinal_id
    FROM matches m1
    JOIN matches m2 ON m1.tournament_id = m2.tournament_id
                   AND m2.round_type_id = 2  -- Semifinals
                   AND (m2.player1_id IN (m1.winner_id, m1.loser_id) OR m2.player2_id IN (m1.winner_id, m1.loser_id))
    WHERE m1.round_type_id = 3  -- Quarterfinals
) AS match_pairs ON m.id = match_pairs.quarterfinal_id
SET m.next_match_id = match_pairs.semifinal_id;

-- Update next_match_id for Semifinals to Finals
UPDATE matches m
JOIN (
    SELECT m1.id as semifinal_id, m2.id as final_id
    FROM matches m1
    JOIN matches m2 ON m1.tournament_id = m2.tournament_id
                   AND m2.round_type_id = 1  -- Finals
                   AND (m2.player1_id IN (m1.winner_id, m1.loser_id) OR m2.player2_id IN (m1.winner_id, m1.loser_id))
    WHERE m1.round_type_id = 2  -- Semifinals
) AS match_pairs ON m.id = match_pairs.semifinal_id
SET m.next_match_id = match_pairs.final_id;

-- Player 3 wins Match 1
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(3, 1400, 1420, 'WIN', '2024-08-01 09:30:00'),  -- Player 3 gains ELO
(4, 1350, 1330, 'LOSS', '2024-08-01 09:30:00'); -- Player 4 loses ELO

-- Player 6 wins Match 2
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(6, 1450, 1470, 'WIN', '2024-08-01 10:15:00'),  -- Player 6 gains ELO
(5, 1420, 1400, 'LOSS', '2024-08-01 10:15:00'); -- Player 5 loses ELO

-- Player 8 wins Match 3
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(8, 1380, 1400, 'WIN', '2024-08-01 11:00:00'),  -- Player 8 gains ELO
(7, 1370, 1350, 'LOSS', '2024-08-01 11:00:00'); -- Player 7 loses ELO

-- Player 9 wins Match 4
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(9, 1500, 1520, 'WIN', '2024-08-01 11:45:00'),  -- Player 9 gains ELO
(10, 1470, 1450, 'LOSS', '2024-08-01 11:45:00'); -- Player 10 loses ELO

-- Player 3 wins Match 5 (Semifinal 1)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(3, 1420, 1440, 'WIN', '2024-08-02 09:30:00'),  -- Player 3 gains ELO
(8, 1400, 1380, 'LOSS', '2024-08-02 09:30:00'); -- Player 8 loses ELO

-- Player 6 wins Match 6 (Semifinal 2)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(6, 1470, 1490, 'WIN', '2024-08-02 10:15:00'),  -- Player 6 gains ELO
(9, 1520, 1500, 'LOSS', '2024-08-02 10:15:00'); -- Player 9 loses ELO

-- Player 3 wins the Final (Match 7)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(3, 1440, 1460, 'WIN', '2024-08-03 11:30:00'),  -- Player 3 gains ELO (Winner)
(6, 1490, 1470, 'LOSS', '2024-08-03 11:30:00'); -- Player 6 loses ELO

-- Blitz Grand Masters 2024 Data
-- Insert tournament players
INSERT INTO tournament_player (tournament_id, player_id)
VALUES
(6, 1), (6, 2), (6, 3), (6, 4), (6, 5), (6, 6), (6, 7), (6, 8);  -- Total 8 players

-- Insert matches for tournament ID 6
INSERT INTO matches (tournament_id, game_type_id, player1_id, player2_id, winner_id, loser_id, round_type_id, status, created_at, updated_at)
VALUES
(6, 2, 1, 2, 1, 2, 3, 'COMPLETED', '2024-09-10 09:00:00', '2024-09-10 09:30:00'),  -- Match 1 (Quarterfinal)
(6, 2, 4, 3, 3, 4, 3, 'COMPLETED', '2024-09-10 09:45:00', '2024-09-10 10:15:00'),  -- Match 2 (Quarterfinal)
(6, 2, 6, 5, 6, 5, 3, 'COMPLETED', '2024-09-10 10:30:00', '2024-09-10 11:00:00'),  -- Match 3 (Quarterfinal)
(6, 2, 8, 7, 8, 7, 3, 'COMPLETED', '2024-09-10 11:15:00', '2024-09-10 11:45:00'),  -- Match 4 (Quarterfinal)
(6, 2, 3, 6, 3, 6, 2, 'COMPLETED', '2024-09-10 12:00:00', '2024-09-10 12:30:00'),  -- Match 5 (Semifinal 1)
(6, 2, 1, 8, 1, 8, 2, 'COMPLETED', '2024-09-10 12:45:00', '2024-09-10 01:15:00'),  -- Match 6 (Semifinal 2)
(6, 2, 3, 1, 3, 4, 1, 'COMPLETED', '2024-09-10 11:00:00', '2024-09-10 11:30:00');  -- Match 7 (Final)

-- Update next_match_id for Match 1 to Match 5 (Quarterfinal to Semifinal)
UPDATE matches m1
JOIN matches m2 ON m2.player1_id = 3 AND m2.player2_id = 6 AND m2.round_type_id = 2  -- Semifinal match
SET m1.next_match_id = m2.id
WHERE m1.player1_id = 1 AND m1.player2_id = 2 AND m1.round_type_id = 3;  -- Quarterfinal match

-- Update next_match_id for Match 2 to Match 6 (Quarterfinal to Semifinal)
UPDATE matches m1
JOIN matches m2 ON m2.player1_id = 1 AND m2.player2_id = 8 AND m2.round_type_id = 2  -- Semifinal match
SET m1.next_match_id = m2.id
WHERE m1.player1_id = 4 AND m1.player2_id = 3 AND m1.round_type_id = 3;  -- Quarterfinal match

-- Update next_match_id for Match 3 to Match 5 (Quarterfinal to Semifinal)
UPDATE matches m1
JOIN matches m2 ON m2.player1_id = 3 AND m2.player2_id = 6 AND m2.round_type_id = 2  -- Semifinal match
SET m1.next_match_id = m2.id
WHERE m1.player1_id = 6 AND m1.player2_id = 5 AND m1.round_type_id = 3;  -- Quarterfinal match

-- Update next_match_id for Match 4 to Match 6 (Quarterfinal to Semifinal)
UPDATE matches m1
JOIN matches m2 ON m2.player1_id = 1 AND m2.player2_id = 8 AND m2.round_type_id = 2  -- Semifinal match
SET m1.next_match_id = m2.id
WHERE m1.player1_id = 8 AND m1.player2_id = 7 AND m1.round_type_id = 3;  -- Quarterfinal match

-- Update next_match_id for Match 5 to Match 7 (Semifinal to Final)
UPDATE matches m1
JOIN matches m2 ON m2.player1_id = 3 AND m2.player2_id = 1 AND m2.round_type_id = 1  -- Final match
SET m1.next_match_id = m2.id
WHERE m1.player1_id = 3 AND m1.player2_id = 6 AND m1.round_type_id = 2;  -- Semifinal match

-- Update next_match_id for Match 6 to Match 7 (Semifinal to Final)
UPDATE matches m1
JOIN matches m2 ON m2.player1_id = 3 AND m2.player2_id = 1 AND m2.round_type_id = 1  -- Final match
SET m1.next_match_id = m2.id
WHERE m1.player1_id = 1 AND m1.player2_id = 8 AND m1.round_type_id = 2;  -- Semifinal match

-- Match 1: Player 1 wins against Player 2 (Quarterfinal)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(1, 1500, 1520, 'WIN', '2024-09-10 09:30:00'),  -- Player 1 gains ELO
(2, 1400, 1380, 'LOSS', '2024-09-10 09:30:00'); -- Player 2 loses ELO

-- Match 2: Player 3 wins against Player 4 (Quarterfinal)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(3, 1440, 1470, 'WIN', '2024-09-10 10:15:00'),  -- Player 3 gains ELO
(4, 1450, 1430, 'LOSS', '2024-09-10 10:15:00'); -- Player 4 loses ELO

-- Match 3: Player 6 wins against Player 5 (Quarterfinal)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(6, 1480, 1500, 'WIN', '2024-09-10 11:00:00'),  -- Player 6 gains ELO
(5, 1400, 1380, 'LOSS', '2024-09-10 11:00:00'); -- Player 5 loses ELO

-- Match 4: Player 8 wins against Player 7 (Quarterfinal)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(8, 1370, 1390, 'WIN', '2024-09-10 11:45:00'),  -- Player 8 gains ELO
(7, 1350, 1330, 'LOSS', '2024-09-10 11:45:00'); -- Player 7 loses ELO

-- Match 5: Player 3 wins against Player 6 (Semifinal 1)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(3, 1470, 1510, 'WIN', '2024-09-10 12:30:00'),  -- Player 3 gains ELO
(6, 1500, 1480, 'LOSS', '2024-09-10 12:30:00'); -- Player 6 loses ELO

-- Match 6: Player 1 wins against Player 8 (Semifinal 2)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(1, 1520, 1540, 'WIN', '2024-09-10 01:15:00'),  -- Player 1 gains ELO
(8, 1390, 1370, 'LOSS', '2024-09-10 01:15:00'); -- Player 8 loses ELO

-- Match 7: Player 3 wins against Player 1 (Final)
INSERT INTO elo_history (player_id, old_elo, new_elo, change_reason, created_at)
VALUES
(3, 1510, 1530, 'WIN', '2024-09-18 02:30:00'),  -- Player 3 gains ELO (Winner)
(1, 1540, 1520, 'LOSS', '2024-09-18 02:30:00'); -- Player 1 loses ELO

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


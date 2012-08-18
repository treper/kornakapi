CREATE TABLE  `taste_preferences` (
  `user_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `preference` float NOT NULL,
  PRIMARY KEY (`user_id`,`item_id`),
  KEY `user_id` (`user_id`),
  KEY `item_id` (`item_id`)
);

CREATE TABLE  `taste_candidates` (
  `label` varchar(255) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  PRIMARY KEY (`label`,`item_id`),
  KEY `label` (`label`)
);


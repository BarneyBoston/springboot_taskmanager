CREATE DATABASE IF NOT EXISTS `taskmanager_directory`;
USE `taskmanager_directory`;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(45) NOT NULL UNIQUE,
  `password` VARCHAR(68) NOT NULL,
  `email` VARCHAR(255) DEFAULT NULL,
  `role` VARCHAR(45) NOT NULL,

  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `task`;

CREATE TABLE `task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `due_date` DATE,
  `priority` VARCHAR(20),
  `status` VARCHAR(20) NOT NULL,
  `user_id` BIGINT NOT NULL,

  PRIMARY KEY (`id`),

  CONSTRAINT `FK_TASK_USER` FOREIGN KEY (`user_id`)
  REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
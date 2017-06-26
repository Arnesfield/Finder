-- phpMyAdmin SQL Dump
-- version 4.6.5.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 26, 2017 at 01:33 PM
-- Server version: 10.1.21-MariaDB
-- PHP Version: 7.1.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `android_db_finder`
--

-- --------------------------------------------------------

--
-- Table structure for table `locations`
--

CREATE TABLE `locations` (
  `id` int(24) NOT NULL,
  `user_id` int(24) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `date_time` datetime NOT NULL,
  `status` enum('0','1') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `locations`
--

INSERT INTO `locations` (`id`, `user_id`, `latitude`, `longitude`, `date_time`, `status`) VALUES
(1, 1, 0, 0, '0000-00-00 00:00:00', '0'),
(2, 2, 0, 0, '0000-00-00 00:00:00', '0'),
(3, 3, 0, 0, '0000-00-00 00:00:00', '0'),
(4, 4, 0, 0, '0000-00-00 00:00:00', '0');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(24) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `verification_code` varchar(255) NOT NULL,
  `status` enum('0','1','2') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `email`, `verification_code`, `status`) VALUES
(1, 'rylee', '$2y$10$5WueUm1sDDb.H7Z.ONh.Cul2zWwLLc8enbgrqyPfFLlBKvz1yOoEq', 'some@email.com', '5313793cc1118227da303e9a9fd75f4e', '1'),
(2, 'ninia', '$2y$10$yumDcgTHK47i4rQY8BVvEOKE3m0i8gU.o3ESTOPfx3RpdH7RyUUXO', 'samp@samp.com', '618519af170236d66e1ddb818fc06c56', '1'),
(3, 'cayle', '$2y$10$iMyaxkR1Bnt/m/lLZ/9cc.36I79mVO6RanHjHaQCH1kmXBU.2nNS6', 'some@somemail.com', '1874f001f267fb7a7068e41a884f6b01', '1'),
(4, 'charlyn', '$2y$10$BMEkGBy9JD4AUaiW5E2vbuP7weuS.6ytmXN7pO96nG/NgXL2.99X2', 'char@email.com', '9163f3934088655f49e1436ab14ed158', '1');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `locations`
--
ALTER TABLE `locations`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id` (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `locations`
--
ALTER TABLE `locations`
  MODIFY `id` int(24) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(24) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

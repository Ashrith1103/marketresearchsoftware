-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 28, 2023 at 04:13 PM
-- Server version: 10.4.27-MariaDB
-- PHP Version: 8.1.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `marketresearchsw`
--

-- --------------------------------------------------------

--
-- Table structure for table `company`
--

CREATE TABLE `company` (
  `name` varchar(25) DEFAULT NULL,
  `ID` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `company`
--

INSERT INTO `company` (`name`, `ID`) VALUES
('Harper Collins', 'BV03N4'),
('Tynor', 'LO2F03N4'),
('Samsung', 'PO03N4');

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `ID` varchar(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `companyID` varchar(25) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`ID`, `name`, `description`, `companyID`) VALUES
('MN890', 'Galaxy', '12MP front camera and notch display', 'PO03N4'),
('POLKJFG98', 'S23', 'High-end smartphone with an extraordinary camera and 5G support', 'PO03N4'),
('SLIFGH9876KJ', 'Crutches', 'Provides support to those who need it', 'LO2F03N4'),
('YYU089', 'S21', 'Mobile', 'PO03N4');

-- --------------------------------------------------------

--
-- Table structure for table `review`
--

CREATE TABLE `review` (
  `ID` varchar(20) NOT NULL,
  `PID` varchar(20) NOT NULL,
  `UserID` varchar(20) NOT NULL,
  `date` date DEFAULT NULL,
  `rating` int(11) DEFAULT NULL,
  `review` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `review`
--

INSERT INTO `review` (`ID`, `PID`, `UserID`, `date`, `rating`, `review`) VALUES
('MN890root378', 'MN890', 'Riya', '2023-04-28', 4, 'Nice front camera...better speed'),
('POLKJFG98root267', 'POLKJFG98', 'Riya', '2023-04-28', 3, ''),
('POLKJFG98root312', 'POLKJFG98', 'Riya', '2023-04-28', 3, 'Good mobile'),
('POLKJFG98root640', 'POLKJFG98', 'Riya', '2023-04-28', 4, 'Nice smartphone for regular users');

-- --------------------------------------------------------

--
-- Table structure for table `survey`
--

CREATE TABLE `survey` (
  `ID` varchar(20) NOT NULL,
  `productid` varchar(20) DEFAULT NULL,
  `q1` varchar(255) DEFAULT NULL,
  `q2` varchar(255) DEFAULT NULL,
  `q3` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `survey`
--

INSERT INTO `survey` (`ID`, `productid`, `q1`, `q2`, `q3`) VALUES
('GalaxySamsung685', 'MN890', 'Is the front camera better than Samsung Galaxy M13 ?', 'Is the Application display bar more Interactive ?', 'Apple vs Samsung ?'),
('S21Samsung37', 'YYU089', 'Do you think its better than other versions ?', 'Do you like the camera ?', 'How many years of software support do you require?'),
('S23Samsung921', 'POLKJFG98', 'How do you like our new product?', 'Any more suggestions?', 'What do you like most?'),
('SLKFJG20498R', 'POLKJFG98', 'How was your experience', 'Did you like the new UI update', 'Apple vs Samsung');

-- --------------------------------------------------------

--
-- Table structure for table `surveyresponse`
--

CREATE TABLE `surveyresponse` (
  `surveyID` varchar(20) NOT NULL,
  `responseID` varchar(20) NOT NULL,
  `A1` varchar(255) DEFAULT NULL,
  `A2` varchar(255) DEFAULT NULL,
  `A3` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `surveyresponse`
--

INSERT INTO `surveyresponse` (`surveyID`, `responseID`, `A1`, `A2`, `A3`) VALUES
('S21Samsung37', 'S21Samsung37Riya27', 'Yes', 'Not much', '5'),
('S21Samsung37', 'S21Samsung37Riya29', 'Relatively better', 'No', '5');

-- --------------------------------------------------------

--
-- Table structure for table `userlogin`
--

CREATE TABLE `userlogin` (
  `username` varchar(25) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `company` varchar(25) DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `accesslevel` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `userlogin`
--

INSERT INTO `userlogin` (`username`, `password`, `company`, `role`, `accesslevel`) VALUES
('demo', 'qwerty', '', 'Customer', 0),
('Renita Kurian', 'Password', 'Samsung', 'Market Researcher', 1),
('Richa', 'LastPass', 'Samsung', 'Company Executive', 1),
('Rimi', '12345612', '', 'Customer', 0),
('Riya', '1234', '', 'Customer', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `company`
--
ALTER TABLE `company`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `companyID` (`companyID`);

--
-- Indexes for table `review`
--
ALTER TABLE `review`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `PID` (`PID`),
  ADD KEY `UserID` (`UserID`);

--
-- Indexes for table `survey`
--
ALTER TABLE `survey`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `productid` (`productid`);

--
-- Indexes for table `surveyresponse`
--
ALTER TABLE `surveyresponse`
  ADD PRIMARY KEY (`responseID`),
  ADD KEY `surveyID` (`surveyID`);

--
-- Indexes for table `userlogin`
--
ALTER TABLE `userlogin`
  ADD PRIMARY KEY (`username`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`companyID`) REFERENCES `company` (`ID`);

--
-- Constraints for table `review`
--
ALTER TABLE `review`
  ADD CONSTRAINT `review_ibfk_1` FOREIGN KEY (`PID`) REFERENCES `products` (`ID`),
  ADD CONSTRAINT `review_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `userlogin` (`username`);

--
-- Constraints for table `survey`
--
ALTER TABLE `survey`
  ADD CONSTRAINT `survey_ibfk_1` FOREIGN KEY (`productid`) REFERENCES `products` (`ID`);

--
-- Constraints for table `surveyresponse`
--
ALTER TABLE `surveyresponse`
  ADD CONSTRAINT `surveyresponse_ibfk_1` FOREIGN KEY (`surveyID`) REFERENCES `survey` (`ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

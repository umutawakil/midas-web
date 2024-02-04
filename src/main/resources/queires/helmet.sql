CREATE DATABASE  IF NOT EXISTS `helmet` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `helmet`;
-- MySQL dump 10.13  Distrib 8.0.33, for macos13 (arm64)
--
-- Host: 127.0.0.1    Database: helmet
-- ------------------------------------------------------
-- Server version	8.0.33

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `subscriber`
--

DROP TABLE IF EXISTS `subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriber` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email_address` varchar(300) NOT NULL,
  `confirmed` tinyint(1) NOT NULL,
  `time_zone_offset` varchar(45) NOT NULL,
  `confirmation_token` varchar(300) NOT NULL,
  `creation_timestamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_update_timestamp` varchar(45) DEFAULT 'ON UPDATE CURRENT_TIMESTAMP()',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `v_stock_info`
--

DROP TABLE IF EXISTS `v_stock_info`;
/*!50001 DROP VIEW IF EXISTS `v_stock_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_stock_info` AS SELECT 
 1 AS `ticker`,
 1 AS `current_price`,
 1 AS `window_delta`,
 1 AS `min_delta`,
 1 AS `max_delta`,
 1 AS `time_window`,
 1 AS `profit_margin`,
 1 AS `gross_profit_margin`,
 1 AS `price_equity`,
 1 AS `asset_liability`,
 1 AS `debt_percentage`,
 1 AS `cfo_working_capital`,
 1 AS `sec_sector_code`,
 1 AS `otc`,
 1 AS `name`,
 1 AS `min_price`,
 1 AS `max_price`,
 1 AS `volume_delta`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_unsupported_ticker`
--

DROP TABLE IF EXISTS `v_unsupported_ticker`;
/*!50001 DROP VIEW IF EXISTS `v_unsupported_ticker`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_unsupported_ticker` AS SELECT 
 1 AS `ticker`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_stock_info`
--

/*!50001 DROP VIEW IF EXISTS `v_stock_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`midas`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_stock_info` AS select `s`.`ticker` AS `ticker`,`s`.`current_price` AS `current_price`,round(`s`.`window_delta`,2) AS `window_delta`,round(`s`.`min_delta`,2) AS `min_delta`,round(`s`.`max_delta`,2) AS `max_delta`,`s`.`time_window` AS `time_window`,round(`midas`.`vf`.`profit_margin`,2) AS `profit_margin`,`midas`.`vf`.`gross_profit_margin` AS `gross_profit_margin`,`midas`.`vf`.`price_equity` AS `price_equity`,`midas`.`vf`.`asset_liability` AS `asset_liability`,round(`midas`.`vf`.`debt_percentage`,2) AS `debt_percentage`,round(`midas`.`vf`.`cfo_working_capital`,2) AS `cfo_working_capital`,`f`.`sec_sector_code` AS `sec_sector_code`,`f`.`otc` AS `otc`,`f`.`name` AS `name`,`s`.`min_price` AS `min_price`,`s`.`max_price` AS `max_price`,round(`s`.`volume_delta`,2) AS `volume_delta` from ((`midas`.`statistics` `s` join `midas`.`financials` `f` on((`s`.`ticker` = `f`.`ticker`))) join `midas`.`v_financials` `vf` on((`s`.`ticker` = `midas`.`vf`.`ticker`))) where ((`f`.`ticker` is null) or ((`f`.`quarter_number` = 0) and (`midas`.`vf`.`quarter_number` = 0))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_unsupported_ticker`
--

/*!50001 DROP VIEW IF EXISTS `v_unsupported_ticker`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`midas`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_unsupported_ticker` AS select `midas`.`unsupported_ticker`.`ticker` AS `ticker` from `midas`.`unsupported_ticker` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-02-03 23:54:30

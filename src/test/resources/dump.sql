-- MySQL dump 10.13  Distrib 5.7.21, for macos10.13 (x86_64)
--
-- Host: localhost    Database: timesheet
-- ------------------------------------------------------
-- Server version	5.7.21

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `gong_si`
--

DROP TABLE IF EXISTS `gong_si`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gong_si` (
  `id` bigint(20) NOT NULL,
  `jie_suan_ri` date DEFAULT NULL,
  `ming_cheng` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_l6gayoyacuow7ck718xngdpbu` (`ming_cheng`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gong_si`
--

LOCK TABLES `gong_si` WRITE;
/*!40000 ALTER TABLE `gong_si` DISABLE KEYS */;
INSERT INTO `gong_si` VALUES (5,NULL,'g1'),(6,NULL,'g2'),(7,NULL,'g3');
/*!40000 ALTER TABLE `gong_si` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gong_zuo_ji_lu`
--

DROP TABLE IF EXISTS `gong_zuo_ji_lu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gong_zuo_ji_lu` (
  `id` bigint(20) NOT NULL,
  `bei_zhu` varchar(255) DEFAULT NULL,
  `jie_shu` datetime(6) DEFAULT NULL,
  `kai_shi` datetime(6) DEFAULT NULL,
  `xiang_mu_id` bigint(20) NOT NULL,
  `yong_hu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK25fbabe3xf5p60bbhttj3y25l` (`xiang_mu_id`),
  KEY `FKs6ghwceii3j6omw83pxmhb2e2` (`yong_hu_id`),
  CONSTRAINT `FK25fbabe3xf5p60bbhttj3y25l` FOREIGN KEY (`xiang_mu_id`) REFERENCES `xiang_mu` (`id`),
  CONSTRAINT `FKs6ghwceii3j6omw83pxmhb2e2` FOREIGN KEY (`yong_hu_id`) REFERENCES `yong_hu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gong_zuo_ji_lu`
--

LOCK TABLES `gong_zuo_ji_lu` WRITE;
/*!40000 ALTER TABLE `gong_zuo_ji_lu` DISABLE KEYS */;
INSERT INTO `gong_zuo_ji_lu` VALUES (12,'testWorkNote','2000-01-01 11:01:00.000000','2000-01-01 10:01:00.000000',8,2);
/*!40000 ALTER TABLE `gong_zuo_ji_lu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (13),(13),(13),(13),(13);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xiang_mu`
--

DROP TABLE IF EXISTS `xiang_mu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `xiang_mu` (
  `id` bigint(20) NOT NULL,
  `ming_cheng` varchar(255) DEFAULT NULL,
  `gong_si_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ntyonloblc92sexd9y1nkltci` (`ming_cheng`),
  KEY `FK3stt4kprgjd367win7t3c1c2d` (`gong_si_id`),
  CONSTRAINT `FK3stt4kprgjd367win7t3c1c2d` FOREIGN KEY (`gong_si_id`) REFERENCES `gong_si` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xiang_mu`
--

LOCK TABLES `xiang_mu` WRITE;
/*!40000 ALTER TABLE `xiang_mu` DISABLE KEYS */;
INSERT INTO `xiang_mu` VALUES (8,'g1x1',5),(9,'g1x2',5),(10,'g2x1',6);
/*!40000 ALTER TABLE `xiang_mu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xiang_mu_ji_fei_biao_zhuns`
--

DROP TABLE IF EXISTS `xiang_mu_ji_fei_biao_zhuns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `xiang_mu_ji_fei_biao_zhuns` (
  `xiang_mu_id` bigint(20) NOT NULL,
  `kai_shi` date DEFAULT NULL,
  `xiao_shi_fei_yong` decimal(19,2) DEFAULT NULL,
  `yong_hu_id` bigint(20) DEFAULT NULL,
  KEY `FK6cew9drqjfoyhi1d1qqvidt3x` (`yong_hu_id`),
  KEY `FKqy2g2w594y7g24bbo8eypa463` (`xiang_mu_id`),
  CONSTRAINT `FK6cew9drqjfoyhi1d1qqvidt3x` FOREIGN KEY (`yong_hu_id`) REFERENCES `yong_hu` (`id`),
  CONSTRAINT `FKqy2g2w594y7g24bbo8eypa463` FOREIGN KEY (`xiang_mu_id`) REFERENCES `xiang_mu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xiang_mu_ji_fei_biao_zhuns`
--

LOCK TABLES `xiang_mu_ji_fei_biao_zhuns` WRITE;
/*!40000 ALTER TABLE `xiang_mu_ji_fei_biao_zhuns` DISABLE KEYS */;
INSERT INTO `xiang_mu_ji_fei_biao_zhuns` VALUES (8,'1900-01-01',2.00,2),(8,'2000-01-01',4.00,2),(8,'1900-01-01',2.00,3),(8,'2000-01-01',4.00,3);
/*!40000 ALTER TABLE `xiang_mu_ji_fei_biao_zhuns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yong_hu`
--

DROP TABLE IF EXISTS `yong_hu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yong_hu` (
  `id` bigint(20) NOT NULL,
  `jia_mi_mi_ma` varchar(255) DEFAULT NULL,
  `xiao_shi_fei_yong` decimal(19,2) DEFAULT NULL,
  `yong_hu_ming` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_k9rj7pnj11oettwsubc550i0l` (`yong_hu_ming`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yong_hu`
--

LOCK TABLES `yong_hu` WRITE;
/*!40000 ALTER TABLE `yong_hu` DISABLE KEYS */;
INSERT INTO `yong_hu` VALUES (1,'$2a$10$k/wpqr2SOfflV/m.oShep.WTBYJS0H.GcLsBg3g4.wExgsMtG/5Ee',500.00,'Admin'),(2,'$2a$10$iSOB541DCcA//OsptqfP..bk3jsgDpLpeeyMgfuGVZaLOlch0SEpC',2.00,'y1'),(3,'$2a$10$sgi4QnrTMAJ0L.TN9nNEJOSPgieq6vVfGk0JOG5BSHgRjVVfXcaie',2.00,'y2'),(4,'$2a$10$6AS/7JnuyYdzhbMYFNRAi.t868ID.HNRxGiti.l/KTsV.t/zAtZqm',2.00,'y3');
/*!40000 ALTER TABLE `yong_hu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yong_hu_roles`
--

DROP TABLE IF EXISTS `yong_hu_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `yong_hu_roles` (
  `yong_hu_id` bigint(20) NOT NULL,
  `roles` varchar(255) DEFAULT NULL,
  KEY `FK6kul1eyyw3fe32ropxf1do76` (`yong_hu_id`),
  CONSTRAINT `FK6kul1eyyw3fe32ropxf1do76` FOREIGN KEY (`yong_hu_id`) REFERENCES `yong_hu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yong_hu_roles`
--

LOCK TABLES `yong_hu_roles` WRITE;
/*!40000 ALTER TABLE `yong_hu_roles` DISABLE KEYS */;
INSERT INTO `yong_hu_roles` VALUES (1,'ADMIN'),(2,'USER'),(3,'USER'),(4,'USER');
/*!40000 ALTER TABLE `yong_hu_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zhi_fu`
--

DROP TABLE IF EXISTS `zhi_fu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zhi_fu` (
  `id` bigint(20) NOT NULL,
  `bei_zhu` varchar(255) DEFAULT NULL,
  `jinge` decimal(19,2) DEFAULT NULL,
  `ri_qi` date DEFAULT NULL,
  `gong_si_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3opq961l8thtltgoaakj27kvr` (`gong_si_id`),
  CONSTRAINT `FK3opq961l8thtltgoaakj27kvr` FOREIGN KEY (`gong_si_id`) REFERENCES `gong_si` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zhi_fu`
--

LOCK TABLES `zhi_fu` WRITE;
/*!40000 ALTER TABLE `zhi_fu` DISABLE KEYS */;
INSERT INTO `zhi_fu` VALUES (11,'testNote',100.00,'2000-01-01',5);
/*!40000 ALTER TABLE `zhi_fu` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-04-19 16:28:46

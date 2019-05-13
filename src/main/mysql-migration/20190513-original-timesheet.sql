/*
 Navicat Premium Data Transfer

 Source Server         : magicfish
 Source Server Type    : MySQL
 Source Server Version : 80011
 Source Host           : 106.75.61.198:3306
 Source Schema         : timesheet

 Target Server Type    : MySQL
 Target Server Version : 80011
 File Encoding         : 65001

 Date: 12/05/2019 16:11:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gong_si
-- ----------------------------
DROP TABLE IF EXISTS `gong_si`;
CREATE TABLE `gong_si`  (
  `id` bigint(20) NOT NULL,
  `jie_suan_ri` date NULL DEFAULT NULL,
  `ming_cheng` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_l6gayoyacuow7ck718xngdpbu`(`ming_cheng`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gong_zuo_ji_lu
-- ----------------------------
DROP TABLE IF EXISTS `gong_zuo_ji_lu`;
CREATE TABLE `gong_zuo_ji_lu`  (
  `id` bigint(20) NOT NULL,
  `bei_zhu` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `jie_shu` datetime(6) NULL DEFAULT NULL,
  `kai_shi` datetime(6) NULL DEFAULT NULL,
  `xiang_mu_id` bigint(20) NOT NULL,
  `yong_hu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK25fbabe3xf5p60bbhttj3y25l`(`xiang_mu_id`) USING BTREE,
  INDEX `FKs6ghwceii3j6omw83pxmhb2e2`(`yong_hu_id`) USING BTREE,
  CONSTRAINT `FK25fbabe3xf5p60bbhttj3y25l` FOREIGN KEY (`xiang_mu_id`) REFERENCES `xiang_mu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKs6ghwceii3j6omw83pxmhb2e2` FOREIGN KEY (`yong_hu_id`) REFERENCES `yong_hu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hibernate_sequence
-- ----------------------------
DROP TABLE IF EXISTS `hibernate_sequence`;
CREATE TABLE `hibernate_sequence`  (
  `next_val` bigint(20) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for xiang_mu
-- ----------------------------
DROP TABLE IF EXISTS `xiang_mu`;
CREATE TABLE `xiang_mu`  (
  `id` bigint(20) NOT NULL,
  `ming_cheng` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `gong_si_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_ntyonloblc92sexd9y1nkltci`(`ming_cheng`) USING BTREE,
  INDEX `FK3stt4kprgjd367win7t3c1c2d`(`gong_si_id`) USING BTREE,
  CONSTRAINT `FK3stt4kprgjd367win7t3c1c2d` FOREIGN KEY (`gong_si_id`) REFERENCES `gong_si` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for xiang_mu_ji_fei_biao_zhuns
-- ----------------------------
DROP TABLE IF EXISTS `xiang_mu_ji_fei_biao_zhuns`;
CREATE TABLE `xiang_mu_ji_fei_biao_zhuns`  (
  `xiang_mu_id` bigint(20) NOT NULL,
  `kai_shi` date NULL DEFAULT NULL,
  `xiao_shi_fei_yong` decimal(19, 2) NULL DEFAULT NULL,
  `yong_hu_id` bigint(20) NULL DEFAULT NULL,
  INDEX `FK6cew9drqjfoyhi1d1qqvidt3x`(`yong_hu_id`) USING BTREE,
  INDEX `FKqy2g2w594y7g24bbo8eypa463`(`xiang_mu_id`) USING BTREE,
  CONSTRAINT `FK6cew9drqjfoyhi1d1qqvidt3x` FOREIGN KEY (`yong_hu_id`) REFERENCES `yong_hu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqy2g2w594y7g24bbo8eypa463` FOREIGN KEY (`xiang_mu_id`) REFERENCES `xiang_mu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for yong_hu
-- ----------------------------
DROP TABLE IF EXISTS `yong_hu`;
CREATE TABLE `yong_hu`  (
  `id` bigint(20) NOT NULL,
  `jia_mi_mi_ma` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `xiao_shi_fei_yong` decimal(19, 2) NULL DEFAULT NULL,
  `yong_hu_ming` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_k9rj7pnj11oettwsubc550i0l`(`yong_hu_ming`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for yong_hu_roles
-- ----------------------------
DROP TABLE IF EXISTS `yong_hu_roles`;
CREATE TABLE `yong_hu_roles`  (
  `yong_hu_id` bigint(20) NOT NULL,
  `roles` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  INDEX `FK6kul1eyyw3fe32ropxf1do76`(`yong_hu_id`) USING BTREE,
  CONSTRAINT `FK6kul1eyyw3fe32ropxf1do76` FOREIGN KEY (`yong_hu_id`) REFERENCES `yong_hu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for zhi_fu
-- ----------------------------
DROP TABLE IF EXISTS `zhi_fu`;
CREATE TABLE `zhi_fu`  (
  `id` bigint(20) NOT NULL,
  `bei_zhu` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `jine` decimal(19, 2) NULL DEFAULT NULL,
  `ri_qi` date NULL DEFAULT NULL,
  `gong_si_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK3opq961l8thtltgoaakj27kvr`(`gong_si_id`) USING BTREE,
  CONSTRAINT `FK3opq961l8thtltgoaakj27kvr` FOREIGN KEY (`gong_si_id`) REFERENCES `gong_si` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

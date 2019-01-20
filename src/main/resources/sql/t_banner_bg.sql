/*
Navicat MySQL Data Transfer

Source Server         : 79~
Source Server Version : 50552
Source Host           : 192.168.7.79:3306
Source Database       : bq_cms_v21

Target Server Type    : MYSQL
Target Server Version : 50552
File Encoding         : 65001

Date: 2017-08-03 11:22:43
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `t_banner_bg`
-- ----------------------------
DROP TABLE IF EXISTS `t_banner_bg`;
CREATE TABLE `t_banner_bg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `thumb_url` varchar(128) NOT NULL,
  `status` int(1) NOT NULL DEFAULT '1',
  `ios_show` int(1) DEFAULT '1' COMMENT '1 ios显示',
  `android_show` int(1) DEFAULT '1' COMMENT '1 android显示',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `valid` int(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

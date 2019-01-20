/*
Navicat MySQL Data Transfer

Source Server         : 79~
Source Server Version : 50552
Source Host           : 192.168.7.79:3306
Source Database       : bq_cms_v21

Target Server Type    : MYSQL
Target Server Version : 50552
File Encoding         : 65001

Date: 2017-07-20 10:49:22
*/

SET FOREIGN_KEY_CHECKS=0;


DROP TABLE IF EXISTS `t_app_version`;
DROP TABLE IF EXISTS `t_article`;
DROP TABLE IF EXISTS `t_article_praise_rec`;
DROP TABLE IF EXISTS `t_article_stat`;
DROP TABLE IF EXISTS `t_article_type`;
DROP TABLE IF EXISTS `t_banner`;
DROP TABLE IF EXISTS `t_card`;
DROP TABLE IF EXISTS `t_category`;
DROP TABLE IF EXISTS `t_category_stat`;
DROP TABLE IF EXISTS `t_comment`;
DROP TABLE IF EXISTS `t_comment_praise_rec`;
DROP TABLE IF EXISTS `t_comment_stat`;
DROP TABLE IF EXISTS `t_feedback`;
DROP TABLE IF EXISTS `t_sensitive`;
DROP TABLE IF EXISTS `t_user_collection`;
DROP TABLE IF EXISTS `t_user_follow`;
DROP TABLE IF EXISTS `uc_user`;

-- ----------------------------
-- Table structure for `t_app_version`
-- ----------------------------
DROP TABLE IF EXISTS `t_app_version`;
CREATE TABLE `t_app_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `channel_code` varchar(64) DEFAULT NULL COMMENT '渠道号',
  `channel_name` varchar(64) DEFAULT NULL COMMENT '渠道名称',
  `name` varchar(64) DEFAULT NULL,
  `mpackage` varchar(128) DEFAULT NULL,
  `version` varchar(64) NOT NULL COMMENT '版本号',
  `version_code` varchar(32) DEFAULT NULL,
  `apk_url` varchar(128) DEFAULT NULL COMMENT 'apk地址',
  `apk_size` bigint(20) DEFAULT NULL,
  `intro` text NOT NULL COMMENT '版本描述',
  `status` int(1) DEFAULT '1' COMMENT '1 ',
  `forced_update` int(11) DEFAULT '0' COMMENT '更新类型 0 非强制更新  1强制更新',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `valid` int(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_banner`
-- ----------------------------
DROP TABLE IF EXISTS `t_banner`;
CREATE TABLE `t_banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'banner名称',
  `name` varchar(64) DEFAULT NULL,
  `title` varchar(64) DEFAULT NULL COMMENT 'Banner标题',
  `banner_type` varchar(32) DEFAULT NULL COMMENT 'banner类型 外链  帖子 圈子',
  `thumb_url` varchar(128) NOT NULL COMMENT 'banner图片',
  `remark` text COMMENT '备注内容 用于保存 banner类型下的具体内容',
  `pos` varchar(32) DEFAULT 'index' COMMENT 'banner位置 ，默认index（首页）',
  `idx` int(5) DEFAULT NULL COMMENT '排序',
  `status` int(11) DEFAULT '1' COMMENT '状态 1上架 ，0下架',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `valid` tinyint(4) DEFAULT '1' COMMENT '有效性 1 有效 0 无效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_comment`
-- ----------------------------
DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL COMMENT '评论帖子',
  `fparent_id` bigint(20) DEFAULT NULL COMMENT '第一级评论',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上一级id',
  `user_id` varchar(255) NOT NULL COMMENT '评论人',
  `content` text,
  `replies` int(11) DEFAULT '0' COMMENT '回复数',
  `likes` int(11) DEFAULT '0' COMMENT '点赞数',
  `factory_name` varchar(64) DEFAULT NULL,
  `device_model` varchar(64) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `valid` int(1) NOT NULL DEFAULT '1' COMMENT '1 有效 0 无效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_comment_likes`
-- ----------------------------
DROP TABLE IF EXISTS `t_comment_likes`;
CREATE TABLE `t_comment_likes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment_id` bigint(20) NOT NULL COMMENT '评论id',
  `user_id` varchar(64) NOT NULL COMMENT '用户id',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_feedback`
-- ----------------------------
DROP TABLE IF EXISTS `t_feedback`;
CREATE TABLE `t_feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `mobile` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_feedback
-- ----------------------------

-- ----------------------------
-- Table structure for `t_group`
-- ----------------------------
DROP TABLE IF EXISTS `t_group`;
CREATE TABLE `t_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `plate_id` bigint(20) DEFAULT '0' COMMENT '上级id 默认0',
  `name` varchar(64) NOT NULL DEFAULT 'plate' COMMENT '板块或圈子名称',
  `thumb_url` varchar(128) DEFAULT NULL COMMENT '缩略图',
  `idx` int(11) DEFAULT '0',
  `status` int(1) DEFAULT '1' COMMENT '状态 1 上架  0下架',
  `follows` int(10) NOT NULL DEFAULT '0' COMMENT '关注数',
  `tag_id` bigint(20) NOT NULL COMMENT '标签id（圈子名称对应的标签id）',
  `posts` int(10) NOT NULL DEFAULT '0' COMMENT '帖子数',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `valid` int(1) DEFAULT '1' COMMENT '有效性 1有效 0无效',
  PRIMARY KEY (`id`),
  KEY `GROUP_PLATE_INDEX` (`plate_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_group_follows`
-- ----------------------------
DROP TABLE IF EXISTS `t_group_follows`;
CREATE TABLE `t_group_follows` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL,
  `user_id` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_msg_read_at`
-- ----------------------------
DROP TABLE IF EXISTS `t_msg_read_at`;
CREATE TABLE `t_msg_read_at` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL,
  `read_at` datetime NOT NULL,
  `type` int(11) NOT NULL DEFAULT '1' COMMENT '1 回复查看时间 2 私信查询时间  3 系统消息查看时间  ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_plate`
-- ----------------------------
DROP TABLE IF EXISTS `t_plate`;
CREATE TABLE `t_plate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL DEFAULT 'plate' COMMENT '板块或圈子名称',
  `thumb_url` varchar(128) DEFAULT NULL COMMENT '缩略图',
  `idx` int(11) DEFAULT '0',
  `status` int(1) DEFAULT '1' COMMENT '状态 1 上架  0下架',
  `groups` int(10) NOT NULL DEFAULT '0' COMMENT '圈子数',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `valid` int(1) DEFAULT '1' COMMENT '有效性 1有效 0无效',
  PRIMARY KEY (`id`),
  KEY `PLATE_NAME_INDEX` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_post`
-- ----------------------------
DROP TABLE IF EXISTS `t_post`;
CREATE TABLE `t_post` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL COMMENT '帖子的圈子id',
  `type_id` bigint(20) DEFAULT NULL,
  `title` varchar(128) DEFAULT NULL COMMENT '标题',
  `intro` varchar(255) DEFAULT NULL COMMENT '简介（帖子用）',
  `user_id` varchar(64) NOT NULL COMMENT '文章作者id',
  `content` mediumtext COMMENT '内容（16m最多容量）',
  `thumb_url` text COMMENT '因为有可能是9张图的地址，所以设置为text',
  `comments` int(11) DEFAULT '0' COMMENT '评论数',
  `likes` int(11) DEFAULT '0' COMMENT '点赞数',
  `idx` int(11) DEFAULT '0' COMMENT '手工排序',
  `top` int(1) DEFAULT '0' COMMENT '置顶 1是 0不是',
  `is_sys` int(1) DEFAULT '0' COMMENT '0用户贴 1系统贴',
  `status` int(1) DEFAULT '1' COMMENT '1 上架 0 下架',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `valid` int(1) DEFAULT '1' COMMENT '数据有效性  1 正常数据  0 删除数据',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_post_follows`
-- ----------------------------
DROP TABLE IF EXISTS `t_post_follows`;
CREATE TABLE `t_post_follows` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_post_follows
-- ----------------------------

-- ----------------------------
-- Table structure for `t_post_likes`
-- ----------------------------
DROP TABLE IF EXISTS `t_post_likes`;
CREATE TABLE `t_post_likes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `user_id` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `t_post_recommend`
-- ----------------------------
DROP TABLE IF EXISTS `t_post_recommend`;
CREATE TABLE `t_post_recommend` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `idx` int(10) DEFAULT '0',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_post_tag`
-- ----------------------------
DROP TABLE IF EXISTS `t_post_tag`;
CREATE TABLE `t_post_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL COMMENT '标签名称',
  `sys` int(1) NOT NULL DEFAULT '0' COMMENT '0非系统1系统标签',
  `thumb_url` varchar(255) DEFAULT NULL COMMENT '系统标签图标',
  `idx` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `valid` int(1) DEFAULT '1' COMMENT '1存在 0删除',
  PRIMARY KEY (`id`),
  KEY `POST_TAG_NAME_INDEX` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_post_tags`
-- ----------------------------
DROP TABLE IF EXISTS `t_post_tags`;
CREATE TABLE `t_post_tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `tag_id` bigint(20) NOT NULL COMMENT '标签id',
  `status` int(1) NOT NULL DEFAULT '1' COMMENT '标签状态',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `POST_TAGS_PID_INDEX` (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_post_type`
-- ----------------------------
DROP TABLE IF EXISTS `t_post_type`;
CREATE TABLE `t_post_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL COMMENT '类型名称',
  `thumb_url` varchar(255) DEFAULT NULL COMMENT '类型图标',
  `idx` int(11) DEFAULT NULL,
  `status` int(1) NOT NULL DEFAULT '0' COMMENT '上下架状态',
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `valid` int(1) DEFAULT '1' COMMENT '1存在 0删除',
  PRIMARY KEY (`id`),
  KEY `POST_TAG_NAME_INDEX` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_search_hot_words`
-- ----------------------------
DROP TABLE IF EXISTS `t_search_hot_words`;
CREATE TABLE `t_search_hot_words` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `text` varchar(32) NOT NULL COMMENT '热词',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT '搜索的次数',
  `idx` int(11) DEFAULT '0' COMMENT '排序',
  `created_at` datetime DEFAULT NULL COMMENT '首次创建时间',
  `updated_at` datetime DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `t_sensitive`
-- ----------------------------
DROP TABLE IF EXISTS `t_sensitive`;
CREATE TABLE `t_sensitive` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `text` varchar(64) NOT NULL COMMENT '敏感词',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `t_sys_msg`
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_msg`;
CREATE TABLE `t_sys_msg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(128) DEFAULT NULL,
  `content` text NOT NULL,
  `status` int(1) DEFAULT '0' COMMENT '1发送 0 为发送',
  `created_at` datetime NOT NULL,
  `valid` int(11) DEFAULT '1' COMMENT '1有效 0删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL COMMENT '用户id 来源 uc',
  `user_name` varchar(32) DEFAULT NULL,
  `mobile` varchar(20) DEFAULT NULL,
  `email` varchar(64) DEFAULT NULL,
  `age` int(3) DEFAULT '0',
  `gender` char(255) DEFAULT 'N' COMMENT ' F M N',
  `avatar_url` varchar(128) DEFAULT NULL,
  `follows` int(11) DEFAULT '0' COMMENT '关注数（人）',
  `followeds` int(11) DEFAULT '0' COMMENT '被关注数（人）',
  `groups` int(11) DEFAULT '0' COMMENT '关注的圈子数',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `status` int(1) DEFAULT '1',
  `valid` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `USER_ID_INDEX` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `t_user_card`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_card`;
CREATE TABLE `t_user_card` (
  `user_id` varchar(64) NOT NULL,
  `name` varchar(64) DEFAULT NULL COMMENT '姓名',
  `card` varchar(20) NOT NULL COMMENT '身份证号',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_user_follows`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_follows`;
CREATE TABLE `t_user_follows` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL,
  `followed_id` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `t_user_private_msg`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_private_msg`;
CREATE TABLE `t_user_private_msg` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_user` varchar(64) NOT NULL COMMENT '私信发送人',
  `to_user` varchar(64) NOT NULL COMMENT '私信接收人',
  `content` text NOT NULL COMMENT '内容',
  `msg_tag` varchar(64) DEFAULT NULL COMMENT '由两个用户的id组成，按升序排列',
  `read` int(1) DEFAULT '0' COMMENT '阅读标志 0未读 1已读',
  `created_at` datetime NOT NULL COMMENT '发送时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=428 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `t_user_shields`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_shields`;
CREATE TABLE `t_user_shields` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) NOT NULL COMMENT '用户id',
  `shield_id` varchar(255) NOT NULL COMMENT '屏蔽的用户id',
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `t_user_tags`
-- ----------------------------
DROP TABLE IF EXISTS `t_user_tags`;
CREATE TABLE `t_user_tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tag_id` bigint(20) NOT NULL COMMENT '用户标签id',
  `user_id` varchar(64) NOT NULL COMMENT '用户id',
  `count` int(11) NOT NULL DEFAULT '0' COMMENT '查看次数',
  `jpush_msg_id` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

package com.bingqiong.bq.model;

import com.bingqiong.bq.model.category.Group;
import com.bingqiong.bq.model.category.GroupFollows;
import com.bingqiong.bq.model.category.Plate;
import com.bingqiong.bq.model.comm.*;
import com.bingqiong.bq.model.comment.Comment;
import com.bingqiong.bq.model.comment.CommentLike;
import com.bingqiong.bq.model.msg.MsgReadAt;
import com.bingqiong.bq.model.msg.PmFrom;
import com.bingqiong.bq.model.msg.PmValid;
import com.bingqiong.bq.model.msg.PrivateMsg;
import com.bingqiong.bq.model.post.*;
import com.bingqiong.bq.model.user.*;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Created by hunsy on 2017/6/22.
 */
public class _MappingKit {

    public static void mapping(ActiveRecordPlugin arp) {

        //系统相关
        arp.addMapping(Banner.TABLE_BANNER, Banner.class);
        arp.addMapping(BannerBg.TABLE_BANNER_BG, BannerBg.class);
        arp.addMapping(SearchHotWord.TABLE_HOT_WORD, SearchHotWord.class);
        arp.addMapping(Sensitive.TABLE_SENSITIVE, Sensitive.class);
        arp.addMapping(FeedBack.TABLE_FEEDBACK, FeedBack.class);
        arp.addMapping(SysMsg.TABLE_SYS_MSG, SysMsg.class);
        arp.addMapping(AppVersion.TABLE_APP_VERSION, AppVersion.class);


        //分类相关表
        arp.addMapping(Plate.TABLE_PLATE, Plate.class);
        arp.addMapping(Group.TABLE_GROUP, Group.class);
        arp.addMapping(GroupFollows.TABLE_GROUP_FOLLOWS, GroupFollows.class);

        //帖子相关表
        arp.addMapping(PostType.TABLE_POST_TYPE, PostType.class);
        arp.addMapping(Post.TABLE_POST, Post.class);
        arp.addMapping(PostTag.TABLE_POST_TAG, PostTag.class);
        arp.addMapping(PostTags.TABLE_POST_TAGS, PostTags.class);
        arp.addMapping(PostLike.TABLE_POST_LIKE, PostLike.class);
        arp.addMapping(PostRecommend.TABLE_POST_RECOMMEND, PostRecommend.class);

        //评论相关表
        arp.addMapping(Comment.TABLE_COMMENT, Comment.class);
        arp.addMapping(CommentLike.TABLE_COMMENT_LIKE, CommentLike.class);

        //用户相关边表
        arp.addMapping(User.TABLE_USER, User.class);
        arp.addMapping(UserCard.TABLE_USER_CARD, UserCard.class);
        arp.addMapping(UserFollows.TABLE_USER_FOLLOWS, UserFollows.class);
//        arp.addMapping(UserTag.TABLE_USER_TAG, UserTag.class);
        arp.addMapping(UserTags.TABLE_USER_TAGS, UserTags.class);
        arp.addMapping(UserShields.TABLE_USER_SHIELDS, UserShields.class);
        arp.addMapping(PrivateMsg.TABLE_PRIVATE_MSG, PrivateMsg.class);
        arp.addMapping(MsgReadAt.TABLE_READ_AT, MsgReadAt.class);
        arp.addMapping(UserCard.TABLE_USER_CARD, UserCard.class);
        arp.addMapping(PmFrom.TABLE_PM_FROM, PmFrom.class);
        arp.addMapping(PmValid.TABLE_PM_VALID, PmValid.class);
    }

}

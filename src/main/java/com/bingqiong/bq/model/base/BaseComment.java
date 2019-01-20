package com.bingqiong.bq.model.base;

import com.bingqiong.bq.model.Comment;

import java.util.Date;

/**
 * Created by hunsy on 2017/4/28.
 */
@SuppressWarnings({"serial"})
public class BaseComment extends BaseModel<Comment> {
    private Long id;
    private Long article_id;
    private Long parent_id;
    private Long fparent_id;
    private String content;
    private String user_avatar;
    private String user_id;
    private String user_name;
    private String reply_user_name;
    private String reply_user_id;
    private String reply_user_avatar;
    private String factory_name;
    private String device_model;
    private int status = 1;
    private Date created_at;
    private Date updated_at;
    private int valid = 1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticle_id() {
        return article_id;
    }

    public void setArticle_id(Long article_id) {
        this.article_id = article_id;
    }

    public Long getParent_id() {
        return parent_id;
    }

    public void setParent_id(Long parent_id) {
        this.parent_id = parent_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFactory_name() {
        return factory_name;
    }

    public void setFactory_name(String factory_name) {
        this.factory_name = factory_name;
    }

    public String getDevice_model() {
        return device_model;
    }

    public void setDevice_model(String device_model) {
        this.device_model = device_model;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public String getReply_user_id() {
        return reply_user_id;
    }

    public void setReply_user_id(String reply_user_id) {
        this.reply_user_id = reply_user_id;
    }

    public String getReply_user_name() {
        return reply_user_name;
    }

    public void setReply_user_name(String reply_user_name) {
        this.reply_user_name = reply_user_name;
    }

    public Long getFparent_id() {
        return fparent_id;
    }

    public void setFparent_id(Long fparent_id) {
        this.fparent_id = fparent_id;
    }

    public String getReply_user_avatar() {
        return reply_user_avatar;
    }

    public void setReply_user_avatar(String reply_user_avatar) {
        this.reply_user_avatar = reply_user_avatar;
    }

    public String getUser_avatar() {
        return user_avatar;
    }

    public void setUser_avatar(String user_avatar) {
        this.user_avatar = user_avatar;
    }
}

package com.bingqiong.bq.model.base;

import com.bingqiong.bq.model.Card;
import com.jfinal.plugin.activerecord.Model;

import java.util.Date;

/**
 * Created by hunsy on 2017/5/5.
 */
@SuppressWarnings({"serial"})
public class BaseCard extends Model<Card> {

    private String uid;

    private String name;

    private String mobile;

    private String card;

    private Date created_at;

    private String client_id;

    private String bu_id;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getBu_id() {
        return bu_id;
    }

    public void setBu_id(String bu_id) {
        this.bu_id = bu_id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}

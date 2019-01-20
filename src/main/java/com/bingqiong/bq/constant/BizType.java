package com.bingqiong.bq.constant;

/**
 * **********************************************************
 *  内容摘要	：<p>
 *
 *  作者	：94841
 *  创建时间	：2016年4月15日 上午10:19:58 
 *  当前版本号：v1.0
 *  历史记录	:
 *  	日期	: 2016年4月15日 上午10:19:58 	修改人：niuzan
 *  	描述	:
 ***********************************************************
 */
public enum BizType 
{
	login(0),
	register(1),
	getUserInfo(2),
	resetPassword(3),
	modifyPassword(4),
	bind(5),
	idcard(6),
	getCode(10);
	
	private Integer bizType;
	
	private BizType(Integer bizType) 
	{
		this.bizType = bizType;
	}

	public Integer getBizType() {
		return bizType;
	}

	public void setBizType(Integer bizType) {
		this.bizType = bizType;
	}

	public static final BizType getFromKey(String bizType) 
	{
        for (BizType e : BizType.values()) 
        {
            if (e.getBizType() == Integer.parseInt(bizType))
            {
                return e;
            }
        }
        return null;
    }
}

package com.drzk.online.vo;

public class SendHead
{
	private String method;// 方法名
	private String replyTopic;// 回复主题
	private String parkingNo;// 车场编号
	public String getMethod()
	{
		return method;
	}
	public void setMethod(String method)
	{
		this.method = method;
	}
	public String getReplyTopic()
	{
		return replyTopic;
	}
	public void setReplyTopic(String replyTopic)
	{
		this.replyTopic = replyTopic;
	}
	public String getParkingNo()
	{
		return parkingNo;
	}
	public void setParkingNo(String parkingNo)
	{
		this.parkingNo = parkingNo;
	}
}

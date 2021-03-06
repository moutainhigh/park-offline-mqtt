package com.drzk.online.impl;

import com.drzk.online.service.OfMqttService;
import com.drzk.online.vo.MqttPayloadVo;
import com.drzk.service.impl.ClientMQTT;
import com.drzk.utils.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class OfMqttServiceImpl<T> implements OfMqttService<T>{
	private static Logger logger=LoggerFactory.getLogger(OfMqttServiceImpl.class);

	public static IMqttClient client = null;

	/**
	 * 云端MQTT连接
	 * @return
	 */
	public synchronized static IMqttClient getConnection(){
		if (client == null) {
			try {
				//String clientId = GlobalPark.properties.getProperty("UUID");		//发布统一用车场配置的UUID
				String clientId = UUID.randomUUID().toString();
				client = ClientMQTT.getConnect(clientId);
			}catch (MqttException e) {
				logger.error("云端MQTT连接异常:",e);
			}
		}
		return client;
	}

	/**
	 * mqtt 发送消息
	 *
	 * @param topic      主题
	 * @param content    消息内容
	 * @param timeout    等待超时时间(单位:秒)
	 * @return 如果有消息回复,回复的信息保存在MqttMessageVO中的topic和message中,同时status值为1
	 * @notice 如果没有消息回复,MqttMessageVO并不为null,只是status,topic与message中没值.<br>
	 *         所以不能用MqttMessageVO==null来判断是否有回复.应该用status是否为0判断<br>
	 *         另外,如果等待时间超过timeout,MqttMessageVO也不为null.
	 */
	public void sendMessage(String topic, String content, int timeout) {
		try {
			IMqttClient client =getConnection();
			logger.debug("线上发布语句:"+content);
			MqttMessage sendMessage = new MqttMessage(content.getBytes("utf-8"));
			sendMessage.setQos(0);
			client.publish(topic, sendMessage);
			System.out.println("publish sucess:"+topic+"=>" + sendMessage);
		} catch (Exception e) {
			logger.debug("线上发布异常:",e);
		}
	}

	public  MqttPayloadVo<T> sendMessage(String topic,String body,String replyTopic, int timeout) {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		IMqttClient client = null;
		MqttPayloadVo<T> returnMessage = new MqttPayloadVo<T>();
		//如果replyTopic为空,表示不需要等待
		boolean isWait = !StringUtils.isNullOrEempty(replyTopic);
		try {
			client = getConnection();
			logger.debug("线上发布订阅语句:"+body);
			//client.connect();
			if (isWait) {
				client.subscribe(replyTopic, 0);
				client.setCallback(new MqttCallback() {

					@Override
					public void messageArrived(String topic, MqttMessage message) throws Exception {
						countDownLatch.countDown();
					}

					@Override
					public void connectionLost(Throwable cause) {
						// TODO Auto-generated method stub

					}

					@Override
					public void deliveryComplete(IMqttDeliveryToken token) {
						// TODO Auto-generated method stub

					}
				});
			}

			MqttMessage sendMessage = new MqttMessage(body.getBytes());
			sendMessage.setQos(0);
			client.publish(topic, sendMessage);
			logger.debug("publish sucess:"+sendMessage);
			if(isWait) {
				countDownLatch.await(timeout, TimeUnit.SECONDS);
				client.unsubscribe(replyTopic);
			}

//			client.disconnect();
//			client.close();

		} catch (Exception e) {
			logger.debug("线上同步回调的发布异常:",e);
		}
		return returnMessage;
	}

}
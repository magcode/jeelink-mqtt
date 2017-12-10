package org.magcode.jeemq;

import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

public class MqttPublisher implements Runnable {
	private SerialPortReader reader;
	private MqttClient mqttClient;
	private String topic;

	public MqttPublisher(SerialPortReader aReader, MqttClient aMqttClient, String aTopic) {
		this.reader = aReader;
		this.mqttClient = aMqttClient;
		this.topic = aTopic;
	}

	public void run() {
		@SuppressWarnings("rawtypes")
		HashMap<String, Reading> map = reader.getReadings();
		Gson gson = new Gson();
		String json = gson.toJson(map);
		MqttMessage message = new MqttMessage();
		message.setPayload(json.getBytes());
		try {
			this.mqttClient.publish(this.topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
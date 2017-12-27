package org.magcode.jeemq;

import java.util.HashMap;

import java.util.Map.Entry;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.magcode.jeemq.lacrosse.LaCrosseTemperatureReading;
/**
 * @author magcode
 * 
 * Publishes node information every xx seconds
 *
 */
public class MqttNodePublisher implements Runnable {
	private SerialPortReader reader;
	private MqttClient mqttClient;
	private String topic;

	public MqttNodePublisher(SerialPortReader aReader, MqttClient aMqttClient, String aTopic) {
		this.reader = aReader;
		this.mqttClient = aMqttClient;
		this.topic = aTopic;
	}

	public void run() {
		@SuppressWarnings("rawtypes")
		HashMap<String, Reading> map = reader.getReadings();

		try {
			for (Entry<String, Reading> entry : map.entrySet()) {
				String key = entry.getKey();
				Reading value = entry.getValue();
				// maybe there are better ways
				if (value instanceof LaCrosseTemperatureReading) {
					LaCrosseTemperatureReading tempRead = (LaCrosseTemperatureReading) value;
					String topic = this.topic + "/" + tempRead.getSensorId();
					MqttMessage message = new MqttMessage();
					message.setPayload(Float.toString(tempRead.getTemperature()).getBytes());
					message.setRetained(true);
					this.mqttClient.publish(topic + "/temperature", message);
					
					message.setPayload(Integer.toString(tempRead.getHum()).getBytes());
					this.mqttClient.publish(topic + "/humidity", message);
					
					message.setPayload(Boolean.toString(tempRead.isbatLow()).getBytes());
					this.mqttClient.publish(topic + "/batterylow", message);					
				}
			}
		} catch (MqttPersistenceException e) {
			System.out.println(e);
		} catch (MqttException e) {
			System.out.println(e);
		}
	}
}
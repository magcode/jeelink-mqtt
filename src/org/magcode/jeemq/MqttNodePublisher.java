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
 *         Publishes node information every xx seconds
 *
 */
public class MqttNodePublisher implements Runnable {
	private SerialPortReader reader;
	private MqttClient mqttClient;
	private String topic;
	// we hold a copy of data to be published. This allows us to compare with recent
	// data and publish only if data has changed.
	private HashMap<String, Reading<?>> publishData;

	public MqttNodePublisher(SerialPortReader aReader, MqttClient aMqttClient, String aTopic) {
		this.reader = aReader;
		this.mqttClient = aMqttClient;
		this.topic = aTopic;
		this.publishData = new HashMap<String, Reading<?>>();
	}

	public void run() {

		updateLocalMap();

		try {
			for (Entry<String, Reading<?>> entry : this.publishData.entrySet()) {
				Reading<?> value = entry.getValue();
				// maybe there are better ways
				if (value instanceof LaCrosseTemperatureReading) {
					LaCrosseTemperatureReading tempRead = (LaCrosseTemperatureReading) value;
					if (tempRead.hasChanged()) {
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
			}
		} catch (MqttPersistenceException e) {
			System.out.println(e);
		} catch (MqttException e) {
			System.out.println(e);
		}
	}

	private void updateLocalMap() {
		HashMap<String, Reading<?>> map = reader.getReadings();
		for (Entry<String, Reading<?>> entry : map.entrySet()) {
			String key = entry.getKey();
			Reading<?> value = entry.getValue();
			if (!this.publishData.containsKey(key)) {
				value.setChanged(true);
			} else {
				Reading<?> oldData = this.publishData.get(key);
				if (!oldData.equals(value)) {
					value.setChanged(true);
				} else {
					value.setChanged(false);
				}
			}
			this.publishData.put(key, value);
		}
	}
}
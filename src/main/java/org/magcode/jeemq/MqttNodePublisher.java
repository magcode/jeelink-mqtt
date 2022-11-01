package org.magcode.jeemq;

import java.util.HashMap;

import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.magcode.jeemq.ec3k.Ec3kReading;
import org.magcode.jeemq.lacrosse.LaCrosseTemperatureReading;

/**
 * @author magcode
 * 
 *         Publishes node information every xx seconds
 *
 */
public class MqttNodePublisher implements Runnable {
	private SerialPortReaderJSC reader;
	private MqttClient mqttClient;
	private String topic;
	private static final long publishAtLeastEveryMS = 15*60*1000;
	// we hold a copy of data to be published. This allows us to compare with recent
	// data and publish only if data has changed.
	private HashMap<String, Reading<?>> publishData;
	private static Logger logger = LogManager.getLogger(MqttNodePublisher.class);

	public MqttNodePublisher(SerialPortReaderJSC aReader, MqttClient aMqttClient, String aTopic) {
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
					logger.debug(tempRead.getSensorId() + " LastPublished: " + tempRead.getLastPublished());
					long diff = System.currentTimeMillis() - tempRead.getLastPublished();
					if (tempRead.hasChanged() || diff > publishAtLeastEveryMS) {
						String topic = this.topic + "/" + tempRead.getSensorId();
						MqttMessage message = new MqttMessage();
						message.setPayload(Float.toString(tempRead.getTemperature()).getBytes());
						message.setRetained(true);
						this.mqttClient.publish(topic + "/temperature", message);

						message.setPayload(Integer.toString(tempRead.getHum()).getBytes());
						this.mqttClient.publish(topic + "/humidity", message);

						message.setPayload(Boolean.toString(tempRead.isbatLow()).getBytes());
						this.mqttClient.publish(topic + "/batterylow", message);

						tempRead.setLastPublished(System.currentTimeMillis());
						this.publishData.put(tempRead.getSensorId(), tempRead);
					}
				} else if (value instanceof Ec3kReading) {
					Ec3kReading powerRead = (Ec3kReading) value;
					logger.info("LastPublished:" + powerRead.getLastPublished());
					long diff = System.currentTimeMillis() - powerRead.getLastPublished();
					if (powerRead.hasChanged() || diff > publishAtLeastEveryMS) {
						String topic = this.topic + "/" + powerRead.getSensorId();
						MqttMessage message = new MqttMessage();
						message.setPayload(Float.toString(powerRead.getcurPow()).getBytes());
						message.setRetained(true);
						this.mqttClient.publish(topic + "/currentpower", message);

						message.setPayload(Long.toString(powerRead.getenergy()).getBytes());
						this.mqttClient.publish(topic + "/energy", message);

						message.setPayload(Long.toString(powerRead.gettimeOn()).getBytes());
						this.mqttClient.publish(topic + "/timeon", message);

						message.setPayload(Long.toString(powerRead.gettimeTot()).getBytes());
						this.mqttClient.publish(topic + "/timetotal", message);

						message.setPayload(Float.toString(powerRead.getmaxPow()).getBytes());
						this.mqttClient.publish(topic + "/maxpower", message);

						powerRead.setLastPublished(System.currentTimeMillis());
						this.publishData.put(powerRead.getSensorId(), powerRead);
					}

				}
			}
		} catch (MqttException e) {
			logger.error("MQTT Problem", e);
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
				value.setLastPublished(oldData.getLastPublished());
			}
			this.publishData.put(key, value);
		}
	}
}
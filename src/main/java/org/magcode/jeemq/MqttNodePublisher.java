package org.magcode.jeemq;

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
 */
public class MqttNodePublisher {
	private MqttClient mqttClient;
	private String topic;
	private static Logger logger = LogManager.getLogger(MqttNodePublisher.class);

	public MqttNodePublisher(MqttClient aMqttClient, String aTopic) {
		this.mqttClient = aMqttClient;
		this.topic = aTopic;
	}

	public void publish(Reading<?> read) {
		try {
			if (read instanceof LaCrosseTemperatureReading) {
				LaCrosseTemperatureReading tempRead = (LaCrosseTemperatureReading) read;

				String topic = this.topic + "/" + tempRead.getSensorId();
				MqttMessage message = new MqttMessage();
				message.setPayload(Float.toString(tempRead.getTemperature()).getBytes());
				message.setRetained(false);
				this.mqttClient.publish(topic + "/temperature", message);

				message.setPayload(Integer.toString(tempRead.getHum()).getBytes());
				this.mqttClient.publish(topic + "/humidity", message);

				message.setPayload(Boolean.toString(tempRead.isbatLow()).getBytes());
				this.mqttClient.publish(topic + "/batterylow", message);

			} else if (read instanceof Ec3kReading) {
				Ec3kReading powerRead = (Ec3kReading) read;

				String topic = this.topic + "/" + powerRead.getSensorId();
				MqttMessage message = new MqttMessage();
				message.setPayload(Float.toString(powerRead.getcurPow()).getBytes());
				message.setRetained(false);
				this.mqttClient.publish(topic + "/currentpower", message);

				message.setPayload(Long.toString(powerRead.getenergy()).getBytes());
				this.mqttClient.publish(topic + "/energy", message);

				message.setPayload(Long.toString(powerRead.gettimeOn()).getBytes());
				this.mqttClient.publish(topic + "/timeon", message);

				message.setPayload(Long.toString(powerRead.gettimeTot()).getBytes());
				this.mqttClient.publish(topic + "/timetotal", message);

				message.setPayload(Float.toString(powerRead.getmaxPow()).getBytes());
				this.mqttClient.publish(topic + "/maxpower", message);

			}
		} catch (MqttException e) {
			logger.warn("MQTT Exception", e);
		}
	}
}
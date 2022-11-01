package org.magcode.jeemq;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @author magcode
 * 
 *         Publishes device information every xx seconds
 *
 */
public class MqttDevicePublisher implements Runnable {
	private SerialPortReaderJSC reader;
	private MqttClient mqttClient;
	private String topic;
	private String sketchName;
	private static final int kickAfter = 30 * 60 * 1000;
	private static Logger logger = LogManager.getLogger(MqttDevicePublisher.class);

	public MqttDevicePublisher(SerialPortReaderJSC aReader, MqttClient aMqttClient, String aTopic, String sketchName) {
		this.reader = aReader;
		this.mqttClient = aMqttClient;
		this.topic = aTopic;
		this.sketchName = sketchName;
	}

	public void run() {
		HashMap<String, Reading<?>> map = reader.getReadings();

		try {
			RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
			long uptime = rb.getUptime() / 1000;

			String hostName = InetAddress.getLocalHost().getHostName();
			String ip = InetAddress.getLocalHost().getHostAddress();

			String name = "Jeelink MQTT Gateway on " + hostName + " " + this.sketchName + " sketch";

			ArrayList<String> nodes = new ArrayList<String>();

			for (Entry<String, Reading<?>> entry : map.entrySet()) {
				String key = entry.getKey();
				Reading<?> value = entry.getValue();
				Long lastSeen = value.getLastSeen();
				if (System.currentTimeMillis() > lastSeen + kickAfter) {
					logger.info("Removing node {} because has not seen since ", key, new Date(lastSeen));
					map.remove(key);
				} else {
					nodes.add(key);
				}
			}

			MqttMessage message = new MqttMessage();
			message.setPayload(Long.toString(uptime).getBytes());
			message.setRetained(true);
			this.mqttClient.publish(topic + "/$stats/uptime", message);

			message.setPayload(String.join(",", nodes).getBytes());
			this.mqttClient.publish(topic + "/$nodes", message);

			if (nodes.isEmpty()) {
				logger.warn("No nodes found or all nodes lost or communication issue. Setting state to 'alert')");
				message.setPayload("alert".getBytes());
			} else {
				message.setPayload("ready".getBytes());
			}
			this.mqttClient.publish(topic + "/$state", message);

			message.setPayload("2.1.0".getBytes());
			this.mqttClient.publish(topic + "/$homie", message);

			message.setPayload(name.getBytes());
			this.mqttClient.publish(topic + "/$name", message);

			message.setPayload("1.0.0".getBytes());
			this.mqttClient.publish(topic + "/$version", message);

			message.setPayload(ip.getBytes());
			this.mqttClient.publish(topic + "/$localip", message);

		} catch (MqttException e) {
			logger.error("MQTT problem", e);
		} catch (UnknownHostException e) {
			logger.error("Host evaluation problem", e);
		}
	}
}
package org.magcode.jeemq;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

/**
 * @author magcode
 * 
 *         Publishes device information every xx seconds
 *
 */
public class MqttDevicePublisher implements Runnable {
	private SerialPortReader reader;
	private MqttClient mqttClient;
	private String topic;
	private String sketchName;
	private static final int kickAfter = 60 * 60 * 1000;

	public MqttDevicePublisher(SerialPortReader aReader, MqttClient aMqttClient, String aTopic, String sketchName) {
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

			message.setPayload("ready".getBytes());
			this.mqttClient.publish(topic + "/$state", message);

			message.setPayload("2.1.0".getBytes());
			this.mqttClient.publish(topic + "/$homie", message);

			message.setPayload(name.getBytes());
			this.mqttClient.publish(topic + "/$name", message);

			message.setPayload("1.0.0".getBytes());
			this.mqttClient.publish(topic + "/$version", message);

			message.setPayload(ip.getBytes());
			this.mqttClient.publish(topic + "/$localip", message);

		} catch (MqttPersistenceException e) {
			System.out.println(e);
		} catch (MqttException e) {
			System.out.println(e);
		} catch (UnknownHostException e) {
			System.out.println(e);
		}
	}
}
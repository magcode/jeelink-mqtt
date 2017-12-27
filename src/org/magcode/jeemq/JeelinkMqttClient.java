package org.magcode.jeemq;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * @author magcode
 * 
 *         MQTT Gateway for Jeelink sketches EC3K and LACROSSE
 *
 */
public class JeelinkMqttClient {
	private static String mqttServer;
	private static int sched = 60;
	private static String topic;
	private static MqttClient mqttClient;
	private static SerialPortReader reader;
	private static jssc.SerialPort serialPort;
	private static String serialPortName;
	private static String sketchName;
	public static final String SKETCH_EC3K = "EC3K";
	public static final String SKETCH_LACR = "LACR";
	private static final int deviceRefresh = 60;

	public static void main(String[] args) throws Exception {
		if (StringUtils.isBlank(args[0]) || StringUtils.isBlank(args[1]) || StringUtils.isBlank(args[2])
				|| StringUtils.isBlank(args[3])) {
			System.out.println("Missing arguments");
			return;
		}
		// args
		mqttServer = args[0];
		topic = args[1];
		serialPortName = args[2];
		sketchName = args[3];

		// check for optional arg
		if (args.length > 4) {
			String schedule = args[4];
			sched = Integer.parseInt(schedule);
		}

		System.out.println("Jeelink MQTT Client starting in " + sketchName + " mode.");

		// connect to MQTT broker
		startMQTTClient();

		// start serial
		startSerialListener();

		// start mqtt node publisher
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable nodePublisher = new MqttNodePublisher(reader, mqttClient, topic);
		ScheduledFuture<?> future = executor.scheduleAtFixedRate(nodePublisher, 2, sched, TimeUnit.SECONDS);

		// start mqtt device publisher
		Runnable devicePublisher = new MqttDevicePublisher(reader, mqttClient, topic, sketchName);
		ScheduledFuture<?> devicePublisherFuture = executor.scheduleAtFixedRate(devicePublisher, 2, deviceRefresh,
				TimeUnit.MINUTES);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					MqttMessage message = new MqttMessage();
					message.setPayload("disconnected".getBytes());
					message.setRetained(true);
					mqttClient.publish(topic + "/$state", message);

					mqttClient.disconnect();
					System.out.println("Disconnected from MQTT server");

					future.cancel(true);
					devicePublisherFuture.cancel(true);
					System.out.println("Closing COM Port " + serialPortName);
					serialPort.closePort();
				} catch (MqttException | SerialPortException e) {
					System.out.println(e);
				}
			}
		});
	}

	private static void startMQTTClient() throws MqttException {
		System.out.println("Starting MQTT Client ...");
		mqttClient = new MqttClient(mqttServer, "client-for-jeelink-" + sketchName);
		mqttClient.connect();
		System.out.println("Connected to MQTT broker.");
	}

	private static void startSerialListener() {
		System.out.println("Opening COM Port " + serialPortName);
		serialPort = new SerialPort(serialPortName);
		try {
			serialPort.openPort();
			serialPort.setParams(57600, 8, 1, 0);
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;
			serialPort.setEventsMask(mask);
			reader = new SerialPortReader(serialPort, sketchName);
			serialPort.addEventListener(reader);
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}
	}
}
package org.magcode.jeemq;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import jssc.SerialPort;
import jssc.SerialPortException;

//OK 22 20 231 1 85 71 254 1 85 71 254 0 2 209 181 1 57 2 4 17 12
//OK 9 12 1 4 208 59
//OK 9 18 1 4 213 48
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
		// start mqtt publisher
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable exporter = new MqttPublisher(reader, mqttClient, topic);
		ScheduledFuture<?> future = executor.scheduleAtFixedRate(exporter, 2, sched, TimeUnit.SECONDS);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					mqttClient.disconnect();
					System.out.println("Disconnected from MQTT server");

					future.cancel(true);
					System.out.println("Closing COM Port " + serialPortName);
					serialPort.closePort();
				} catch (MqttException | SerialPortException e) {
					e.printStackTrace();
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
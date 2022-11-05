package org.magcode.jeemq;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * @author magcode
 * 
 *         MQTT Gateway for Jeelink sketches EC3K and LACROSSE
 *
 */
public class JeelinkMqttClient {
	private static String mqttServer;
	private static String topic;
	private static MqttClient mqttClient;
	private static SerialPortReaderJSC reader;
	// private static jssc.SerialPort serialPort;
	private static String serialPortName;
	private static String sketchName = "";
	public static final String SKETCH_EC3K = "EC3K";
	public static final String SKETCH_LACR = "LACR";
	private static String logLevel = "INFO";
	private static MqttNodePublisher nodePublisher;
	// private static Logger logger = LogManager.getLogger(JeelinkMqttClient.class);
	private static Logger logger;

	public static void main(String[] args) throws Exception {
		System.setProperty("logFilename", sketchName);
		logger = LogManager.getLogger(JeelinkMqttClient.class);

		readProps();
		reConfigureLogger();

		logger.info("Jeelink MQTT Client starting in {} mode.", sketchName);

		// connect to MQTT broker
		startMQTTClient();
		nodePublisher = new MqttNodePublisher(mqttClient, topic);
		// start serial
		startSerialListener();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Logger logger2 = LogManager.getLogger("shutdown");
				try {
					MqttMessage message = new MqttMessage();
					message.setPayload("disconnected".getBytes());
					mqttClient.publish(topic + "/$state", message);
					mqttClient.disconnect();
					logger2.info("Disconnected from MQTT server");
					reader.stop();
					((LifeCycle) LogManager.getContext()).stop();
				} catch (MqttException e) {
					logger2.error("Error during shutdown", e);
				}
			}
		});
	}

	private static void startMQTTClient() throws MqttException {
		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Failed to get hostname", e);
		}
		mqttClient = new MqttClient(mqttServer, "client-for-jeelink-" + sketchName + "-on-" + hostName,
				new MemoryPersistence());
		logger.info("Starting MQTT Client ...");
		MqttConnectOptions connOpt = new MqttConnectOptions();
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
		connOpt.setAutomaticReconnect(true);

		// set last will according to Homie spec
		connOpt.setWill(new MqttTopic(topic + "/$state", null), "lost".getBytes(), 1, true);

		mqttClient.connect(connOpt);
		logger.info("Connected to MQTT broker {}", mqttServer);
		logger.info("Publishing to {}", topic);
	}

	private static void startSerialListener() {
		logger.info("Opening COM Port {}", serialPortName);
		reader = new SerialPortReaderJSC(nodePublisher, serialPortName, sketchName);
	}

	/**
	 * Reconfigures log4j2 and changes the filename. that might be helpful when
	 * running multiple instances.
	 */
	private static void reConfigureLogger() {
		System.setProperty("logFilename", "-" + sketchName);
		org.apache.logging.log4j.core.LoggerContext ctx = (org.apache.logging.log4j.core.LoggerContext) LogManager
				.getContext(false);
		ctx.reconfigure();
		Configurator.setRootLevel(Level.forName(logLevel, 0));
	}

	private static void readProps() {
		Properties props = new Properties();
		InputStream input = null;

		try {
			File jarPath = new File(
					JeelinkMqttClient.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			String propertiesPath = jarPath.getParentFile().getAbsolutePath();
			String filePath = propertiesPath + "/jeelink.properties";
			logger.info("Loading properties from {}", filePath);

			input = new FileInputStream(filePath);
			props.load(input);
			mqttServer = props.getProperty("mqttServer", "tcp://localhost");
			topic = props.getProperty("topic", "home/jeelink");
			sketchName = props.getProperty("sketchName", "");
			serialPortName = props.getProperty("serialPortName", "");
			logLevel = props.getProperty("logLevel", "INFO");
		} catch (IOException ex) {
			logger.error("Cannot read properties", ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Failed to close file", e);
				}
			}
		}
	}
}
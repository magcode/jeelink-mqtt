package org.magcode.jeemq;

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcode.jeemq.ec3k.Ec3kReadingConverter;
import org.magcode.jeemq.lacrosse.LaCrosseTemperatureReadingConverter;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class SerialPortReaderJSC implements SerialPortMessageListener {
	private SerialPort serialPort;
	private String sketchName;
	private MqttNodePublisher nodePublisher;
	private static Logger logger = LogManager.getLogger(SerialPortReaderJSC.class);

	public SerialPortReaderJSC(MqttNodePublisher nodePublisher, String portName, String aSketchName) {
		this.serialPort = SerialPort.getCommPort(portName);

		this.serialPort.setComPortParameters(57600, 8, 1, 0);
		this.serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DTR_ENABLED + SerialPort.FLOW_CONTROL_CTS_ENABLED
				+ SerialPort.FLOW_CONTROL_RTS_ENABLED);
		this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
		this.serialPort.openPort();

		this.sketchName = aSketchName;

		this.serialPort.addDataListener(this);
		this.nodePublisher = nodePublisher;
	}

	public void stop() {
		logger.info("Closing COM Port {}", serialPort.getDescriptivePortName());
		this.serialPort.closePort();
	}

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		byte[] delimitedMessage = event.getReceivedData();
		Reading<?> read = null;
		String buffer = "";
		try {
			buffer = new String(delimitedMessage, "ASCII").trim();

			if (JeelinkMqttClient.SKETCH_LACR.equals(this.sketchName)) {
				LaCrosseTemperatureReadingConverter lrc = new LaCrosseTemperatureReadingConverter();
				read = lrc.createReading(buffer);
			} else if (JeelinkMqttClient.SKETCH_EC3K.equals(this.sketchName)) {
				Ec3kReadingConverter erc = new Ec3kReadingConverter();
				read = erc.createReading(buffer);
			}
			if (read != null) {
				logger.trace("New data for node {}: {}", read.getSensorId(), read.toString());
				nodePublisher.publish(read);
			}
		} catch (UnsupportedEncodingException e) {
			logger.warn("Could not convert data to ASCI");
		}

	}

	@Override
	public boolean delimiterIndicatesEndOfMessage() {
		return true;
	}

	@Override
	public byte[] getMessageDelimiter() {
		return new byte[] { (byte) 0xA };
	}

}
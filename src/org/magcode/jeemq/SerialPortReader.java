package org.magcode.jeemq;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.magcode.jeemq.ec3k.Ec3kReadingConverter;
import org.magcode.jeemq.lacrosse.LaCrosseTemperatureReadingConverter;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SerialPortReader implements SerialPortEventListener {
	private SerialPort serialPort;
	private HashMap<String, Reading<?>> currentReadings;
	private String sketchName;
	private String buffer = "";

	public SerialPortReader(SerialPort aSerialPort, String aSketchName) {
		this.serialPort = aSerialPort;
		currentReadings = new HashMap<String, Reading<?>>();
		this.sketchName = aSketchName;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {

		if (event.isRXCHAR()) {
			try {
				String data = serialPort.readString(event.getEventValue());
				if (!StringUtils.contains(data, "\n")) {
					buffer = data;
				} else {
					buffer = buffer + data;
					buffer = buffer.trim();
					Reading<?> read = null;
					if (JeelinkMqttClient.SKETCH_LACR.equals(this.sketchName)) {
						LaCrosseTemperatureReadingConverter lrc = new LaCrosseTemperatureReadingConverter();
						read = lrc.createReading(buffer);
					} else if (JeelinkMqttClient.SKETCH_EC3K.equals(this.sketchName)) {
						Ec3kReadingConverter erc = new Ec3kReadingConverter();
						read = erc.createReading(buffer);
					}
					if (read != null) {
						currentReadings.put(read.getSensorId(), read);
					}
					buffer = "";
				}
			} catch (SerialPortException ex) {
				System.out.println(ex);
			}
		}
	}

	public HashMap<String, Reading<?>> getReadings() {
		return currentReadings;
	}
}
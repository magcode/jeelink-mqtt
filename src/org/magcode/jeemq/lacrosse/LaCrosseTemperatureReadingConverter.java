package org.magcode.jeemq.lacrosse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LaCrosseTemperatureReadingConverter {
	private static final Pattern LINE_P = Pattern
			.compile("OK\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");

	public LaCrosseTemperatureReading createReading(String inputLine) {

		if (inputLine != null) {
			Matcher matcher = LINE_P.matcher(inputLine);
			if (matcher.matches()) {
				// Format
				//
				// OK 9 56 1 4 156 37 (ID = 56 T: 18.0 H: 37 no NewBatt)
				// OK 9 49 1 4 182 54 (ID = 49 T: 20.6 H: 54 no NewBatt)
				// OK 9 55 129 4 192 56 (ID = 55 T: 21.6 H: 56 WITH NewBatt)
				// OK 9 ID XXX XXX XXX XXX
				// | | | | | | |
				// | | | | | | --- Humidity incl. WeakBatteryFlag
				// | | | | | |------ Temp * 10 + 1000 LSB
				// | | | | |---------- Temp * 10 + 1000 MSB
				// | | | |-------------- Sensor type (1 or 2) +128 if NewBatteryFlag
				// | | |----------------- Sensor ID
				// | |------------------- fix "9"
				// |---------------------- fix "OK"
				// logger.trace("Creating reading from: {}", inputLine);

				int sensorId = Integer.parseInt(matcher.group(2));
				int int3 = Integer.parseInt(matcher.group(3));

				int batteryNewInt = (int3 & 0x80) >> 7;
				// int type = (int3 & 0x70) >> 7;

				float temperature = (float) (Integer.parseInt(matcher.group(4)) * 256
						+ Integer.parseInt(matcher.group(5)) - 1000) / 10;
				int humidity = Integer.parseInt(matcher.group(6)) & 0x7f;
				if (humidity > 100) {
					humidity = 0;
				}
				int batteryLowInt = (Integer.parseInt(matcher.group(6)) & 0x80) >> 7;

				boolean batteryLow = batteryLowInt == 1;
				boolean batteryNew = batteryNewInt == 1;

				return new LaCrosseTemperatureReading("" + sensorId, temperature, humidity, batteryNew, batteryLow);
			}
		}

		return null;
	}
}
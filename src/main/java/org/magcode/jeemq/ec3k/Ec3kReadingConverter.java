package org.magcode.jeemq.ec3k;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ec3kReadingConverter {
	private static final Pattern LINE_P = Pattern
			.compile("OK\\s+22\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)"
					+ "\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)"
					+ "\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)");

	public Ec3kReading createReading(String inputLine) {
		if (inputLine != null) {
			Matcher matcher = LINE_P.matcher(inputLine);
			if (matcher.matches()) {
				/*
				 * OK 22 188 129 0 209 209 102 0 174 89 187 0 1 123 102 0 0 10 117 2 0 (ID =
				 * BC81)
				 */
				long id1 = Long.parseLong(matcher.group(1));
				long id2 = Long.parseLong(matcher.group(2));
				String id = String.format("%02X%02X", id1, id2);

				long secTot1 = Long.parseLong(matcher.group(3));
				long secTot2 = Long.parseLong(matcher.group(4));
				long secTot3 = Long.parseLong(matcher.group(5));
				long secTot4 = Long.parseLong(matcher.group(6));
				long secondsTotal = (secTot1 << 24) + (secTot2 << 16) + (secTot3 << 8) + secTot4;

				long secOn1 = Long.parseLong(matcher.group(7));
				long secOn2 = Long.parseLong(matcher.group(8));
				long secOn3 = Long.parseLong(matcher.group(9));
				long secOn4 = Long.parseLong(matcher.group(10));
				long secondsOn = (secOn1 << 24) + (secOn2 << 16) + (secOn3 << 8) + secOn4;

				long con1 = Long.parseLong(matcher.group(11));
				long con2 = Long.parseLong(matcher.group(12));
				long con3 = Long.parseLong(matcher.group(13));
				long con4 = Long.parseLong(matcher.group(14));
				long consumptionTotal = (con1 << 24) + (con2 << 16) + (con3 << 8) + con4;

				long cur1 = Long.parseLong(matcher.group(15));
				long cur2 = Long.parseLong(matcher.group(16));
				float currentWatt = ((cur1 << 8) + cur2) / 10f;

				long max1 = Long.parseLong(matcher.group(17));
				long max2 = Long.parseLong(matcher.group(18));
				float maxWatt = ((max1 << 8) + max2) / 10f;

				int resets = Integer.parseInt(matcher.group(19));
				return new Ec3kReading(id, currentWatt, maxWatt, consumptionTotal, secondsOn, secondsTotal, resets);
			}
		}

		return null;
	}
}
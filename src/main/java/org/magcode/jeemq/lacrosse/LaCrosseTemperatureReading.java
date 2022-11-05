package org.magcode.jeemq.lacrosse;

import org.magcode.jeemq.Reading;

public class LaCrosseTemperatureReading implements Reading<LaCrosseTemperatureReading> {
	private String sensorId;
	private float temp;
	private int hum;
	private boolean batNew;
	private boolean batLow;

	public LaCrosseTemperatureReading(String sensorId, float temp, int humidity, boolean batNew, boolean batLow) {
		this.sensorId = sensorId;
		this.temp = temp;
		this.hum = humidity;
		this.batNew = batNew;
		this.batLow = batLow;
	}

	@Override
	public String getSensorId() {
		return sensorId;
	}

	public float getTemperature() {
		return temp;
	}

	public int getHum() {
		return hum;
	}

	public boolean isbatLow() {
		return batLow;
	}

	@Override
	public String toString() {
		return "sensorId=" + sensorId + ": temp=" + temp + ", hum=" + hum + ", batLow=" + batLow + ", batNew=" + batNew;
	}

	public boolean isbatNew() {
		return batNew;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LaCrosseTemperatureReading) {
			LaCrosseTemperatureReading old = (LaCrosseTemperatureReading) obj;
			return old.isbatLow() == this.isbatLow() && old.isbatNew() == this.isbatNew()
					&& old.getTemperature() == this.getTemperature() && old.getHum() == this.getHum();
		}
		return false;
	}

}
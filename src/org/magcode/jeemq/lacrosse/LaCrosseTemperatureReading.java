package org.magcode.jeemq.lacrosse;

import org.magcode.jeemq.Reading;

public class LaCrosseTemperatureReading implements Reading<LaCrosseTemperatureReading> {
	private String sensorId;
	// private int sensorType;
	private float temp;
	private int hum;
	private boolean batNew;
	private boolean batLow;
	private String batLowOH;

	public LaCrosseTemperatureReading(int sensorId, int sensorType, float temp, int humidity, boolean batNew,
			boolean batLow) {
		this(String.valueOf(sensorId), sensorType, temp, humidity, batNew, batLow);
	}

	public LaCrosseTemperatureReading(String sensorId, int sensorType, float temp, int humidity, boolean batNew,
			boolean batLow) {
		this.sensorId = sensorId;
		// this.sensorType = sensorType;
		this.temp = temp;
		this.hum = humidity;
		// this.batNew = batNew;
		this.batLow = batLow;
		this.batLowOH = this.batLow ? "ON" : "OFF";
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

	public String isbatLowOH() {
		return batLowOH;
	}

	@Override
	public String toString() {
		return "sensorId=" + sensorId + ": temp=" + temp + ", hum=" + hum + ", batLow=" + batLow + ", batNew=" + batNew;
	}

	public boolean isbatNew() {
		return batNew;
	}
}
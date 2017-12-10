package org.magcode.jeemq.ec3k;

import org.magcode.jeemq.Reading;

public class Ec3kReading implements Reading<Ec3kReading> {
	private float curPow;
	private float maxPow;
	private long energy;
	private long timeOn;
	private long timeTot;
	private String sensorId;

	public Ec3kReading(String sensorId, float curPow, float maxPow, long energy, long timeOn, long timeTot,
			int resets) {
		this.curPow = curPow;
		this.maxPow = maxPow;
		this.energy = energy;
		this.timeOn = timeOn;
		this.timeTot = timeTot;
		this.sensorId = sensorId;
	}

	@Override
	public String toString() {
		return "sensorId=" + sensorId + ": currWatt=" + curPow + ", maxPow=" + maxPow + ", consumption=" + energy
				+ ", timeOn=" + timeOn + ", timeTot=" + timeTot;
	}

	public float getcurPow() {
		return curPow;
	}

	@Override
	public String getSensorId() {
		return sensorId;
	}

	public float getmaxPow() {
		return maxPow;
	}

	public long getenergy() {
		return energy;
	}

	public long gettimeOn() {
		return timeOn;
	}

	public long gettimeTot() {
		return timeTot;
	}
}

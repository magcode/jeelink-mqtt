package org.magcode.jeemq.ec3k;

import org.magcode.jeemq.Reading;

public class Ec3kReading implements Reading<Ec3kReading> {
	private float curPow;
	private float maxPow;
	private long energy;
	private long timeOn;
	private long timeTot;
	private String sensorId;
	private long lastSeen;
	private long lastPublished;
	private boolean hasChanged;

	public Ec3kReading(String sensorId, float curPow, float maxPow, long energy, long timeOn, long timeTot,
			int resets) {
		this.curPow = curPow;
		this.maxPow = maxPow;
		this.energy = energy;
		this.timeOn = timeOn;
		this.timeTot = timeTot;
		this.sensorId = sensorId;
		this.lastSeen = System.currentTimeMillis();
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

	@Override
	public Long getLastSeen() {
		return lastSeen;
	}

	@Override
	public Boolean hasChanged() {
		return hasChanged;
	}

	@Override
	public void setChanged(boolean changed) {
		this.hasChanged = changed;

	}

	@Override
	public Long getLastPublished() {
		return lastPublished;
	}

	@Override
	public void setLastPublished(long published) {
		this.lastPublished=published;		
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Ec3kReading) {
			Ec3kReading old = (Ec3kReading) obj;
			return old.getcurPow() == this.getcurPow() && old.getenergy() == this.getenergy()
					&& old.gettimeOn() == this.gettimeOn() && old.gettimeTot() == this.gettimeTot()
					&& old.getmaxPow() == this.getmaxPow();
		}
		return false;
	}
}

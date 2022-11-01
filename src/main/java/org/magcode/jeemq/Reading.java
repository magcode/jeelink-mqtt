package org.magcode.jeemq;

public interface Reading<R extends Reading<?>> {
	String getSensorId();
	Long getLastSeen();
	Boolean hasChanged();
	void setChanged(boolean changed);
	Long getLastPublished();
	void setLastPublished(long published);
}
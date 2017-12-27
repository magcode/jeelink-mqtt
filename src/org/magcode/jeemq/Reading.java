package org.magcode.jeemq;

public interface Reading<R extends Reading<?>> {
	String getSensorId();
	Long getLastSeen();
}
package org.juxtapose.streamline.laboration.netty;

import java.util.Date;

public class UnixTime {
	private final int value;

	public UnixTime(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return new Date(value * 1000L).toString();
	}
}

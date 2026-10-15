package dev.linhvu.inspector;

import java.util.List;
import java.util.TreeMap;

public final class Timeline {

	public record Row(Event event, long ingestSeq, boolean arrivedLate) {}

	public enum Result { ADDED, DUPLICATE }

	private final String producerId;
	private final int capacity;
	private final TreeMap<Long, Row> rows = new TreeMap<>(); // sourceSeq, Row
	private long received;

	public Timeline(String producerId, int capacity) {
		if (producerId == null || capacity < 1 || capacity > 10000) {
			throw new IllegalArgumentException("Invalid timeline configuration");
		}
		this.producerId = producerId;
		this.capacity = capacity;
	}

	public synchronized Result accept(Event event) {
		if (!producerId.equals(event.producerId())) {
			throw new IllegalArgumentException("Different producer instance");
		}

		Row previous = rows.get(event.sourceSeq());
		if (previous != null) {
			if (!previous.event().equals(event)) {
				throw new IllegalArgumentException("Conflicting event identity: " + event.eventId());
			}
			return Result.DUPLICATE;
		}

		if (rows.size() == capacity) throw new IllegalStateException("Timeline capacity reached");

		long arrival = ++received;
		boolean late = !rows.isEmpty() && event.sourceSeq() < rows.lastKey();
		rows.put(event.sourceSeq(), new Row(event, arrival, late));

		return Result.ADDED;
	}

	public synchronized List<Row> snapshot() {
		return List.copyOf(rows.values());
	}
}

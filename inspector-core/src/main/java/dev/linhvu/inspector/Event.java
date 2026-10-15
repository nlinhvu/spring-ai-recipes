package dev.linhvu.inspector;

import java.time.Instant;
import java.util.Objects;

public record Event(String producerId, long sourceSeq, Instant occurredAt, String operationId, String parentOperationId, Kind kind, String summary) {

	public enum Kind {
		START,
		SNAPSHOT,
		END,
		ERROR,
		CANCEL
	}

	public Event {
		requireId(producerId, "producerId");
		requireId(operationId, "operationId");
		if (parentOperationId != null) requireId(parentOperationId, "parentOperationId");
		if (sourceSeq < 1) throw new IllegalArgumentException("sourceSeq must be positive");
		Objects.requireNonNull(occurredAt, "occurredAt");
		Objects.requireNonNull(kind, "kind");
		Objects.requireNonNull(summary, "summary");

		if (summary.length() > 2000) throw new IllegalArgumentException("summary too long");
	}

	private void requireId(String value, String name) {
		if (value == null || !value.matches("[A-Za-z0-9_-]{1,80}")) {
			throw new IllegalArgumentException("Invalid " + name);
		}
	}

	public String eventId() {
		return producerId + ":" + sourceSeq;
	}
}

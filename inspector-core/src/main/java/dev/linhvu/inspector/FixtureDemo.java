package dev.linhvu.inspector;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public class FixtureDemo {

	private static Event event(long seq, String time, String operation, String parent, Event.Kind kind, String summary) {
		return new Event("fixture-1", seq, Instant.parse("2026-10-15T10:00:00." + time + "Z"), operation, parent, kind, summary);
	}

	public static void main(String[] args) throws Exception {
		String scenario = args.length == 0 ? "normal" : args[0];
		if (!List.of("normal", "conflict", "capacity", "restart").contains(scenario)) {
			throw new IllegalArgumentException("Unknown scenario: " + scenario);
		}
		Timeline timeline = new Timeline("fixture-1", scenario.equals("capacity") ? 1 : 256);
		Event a = event(1, "100", "call-a", null, Event.Kind.START, "ChatClient started");
		Event b = event(2, "110", "call-a", null, Event.Kind.SNAPSHOT, "History contains tool messages; <script>alert('history')</script>");
		Event c = event(3, "120", "tool-a", "call-a", Event.Kind.START, "Tool started");
		Event d = event(4, "120", "call-b", null, Event.Kind.START, "Concurrent call started");
		Event e = event(5, "090", "tool-a", "call-a", Event.Kind.END, "Tool completed");
		Event f = event(6, "140", "call-a", null, Event.Kind.END, "ChatClient completed");
		for (Event ev : List.of(a, c, b, c, d, f, e)) {
			System.out.println(ev.eventId() + " " + timeline.accept(ev));
		}
		if (scenario.equals("conflict")) {
			timeline.accept(event(3, "120", "tool-a", "call-a", Event.Kind.ERROR, "Different data"));
		}
		if (scenario.equals("restart")) {
			timeline.accept(new Event("fixture-2", 1, a.occurredAt(), "call-a", null,
					Event.Kind.START, "New process instance"));
		}
		Path output = Path.of("inspector-core/build/inspector-t1/timeline.html");
		Files.createDirectories(output.getParent());
		Files.writeString(output, render(timeline.snapshot()), StandardCharsets.UTF_8);
		System.out.println("Rows: " + timeline.snapshot().size());
		System.out.println("Report: " + output.toAbsolutePath());
	}

	private static String escape(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}

	static String render(List<Timeline.Row> rows) {
		StringBuilder html = new StringBuilder("""
                <!DOCTYPE html>
                <html lang="en"><head><meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Inspector T1 fixture</title>
                <style>
                body{font:16px/1.5 system-ui;margin:24px;color:#182c29;background:#f5f7f5}
                .scroll{overflow:auto}table{border-collapse:collapse;width:100%;background:white}
                th,td{border-bottom:1px solid #bdcbc5;padding:12px;text-align:left;vertical-align:top}
                td{min-width:100px}code{overflow-wrap:anywhere}summary{cursor:pointer}
                :focus-visible{outline:3px solid #086b51;outline-offset:3px}
                </style></head><body><main><h1>Inspector T1 fixture</h1>
                <p>Order: producer capture sequence. Synthetic data. No live capture.</p>
                <p>START without terminal is incomplete, not success. Snapshot content is history, not new execution.</p>
                <section class="scroll" tabindex="0" aria-label="Event timeline">
                <table><thead><tr><th scope="col">Capture</th><th scope="col">Time (UTC)</th>
                <th scope="col">Ingest</th><th scope="col">Operation / parent</th>
                <th scope="col">Kind</th><th scope="col">Diagnostics</th><th scope="col">Payload</th>
                </tr></thead><tbody>
                """);
		long expected = 1;
		Instant previousTime = null;
		for (Timeline.Row row : rows) {
			Event ev = row.event();
			String flags = row.arrivedLate() ? "late arrival; " : "";
			if (ev.sourceSeq() != expected) flags += "sequence gap before this row; ";
			if (previousTime != null && ev.occurredAt().isBefore(previousTime)) flags += "timestamp decreased; ";
			expected = ev.sourceSeq() + 1;
			previousTime = ev.occurredAt();
			html.append("<tr><td>").append(ev.sourceSeq()).append("<br><code>")
					.append(escape(ev.eventId())).append("</code></td><td>")
					.append(ev.occurredAt()).append("</td><td>").append(row.ingestSeq())
					.append("</td><td>").append(escape(ev.operationId())).append(" / ")
					.append(escape(ev.parentOperationId() == null ? "none" : ev.parentOperationId()))
					.append("</td><td>").append(ev.kind()).append("</td><td>").append(escape(flags))
					.append("</td><td><details><summary>Show payload</summary><code>")
					.append(escape(ev.summary())).append("</code></details></td></tr>");
		}
		return html.append("</tbody></table></section></main></body></html>").toString();
	}

}

package dev.linhvu.inspector.client;

import java.util.function.Consumer;

public final class NativeTestClients {
	private NativeTestClients() { }
	public static InspectorClient recording(Consumer<String> transport) { return new InspectorClient(transport); }
}

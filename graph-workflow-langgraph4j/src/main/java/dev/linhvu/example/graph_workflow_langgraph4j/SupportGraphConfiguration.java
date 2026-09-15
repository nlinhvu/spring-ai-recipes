package dev.linhvu.example.graph_workflow_langgraph4j;

import java.util.Map;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphDefinition;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupportGraphConfiguration {

	static final String CLASSIFY = "classify";
	static final String BILLING = "billing";
	static final String TECHNICAL = "technical";

	@Bean
	CompiledGraph<SupportState> supportGraph(ChatClient chatClient) throws GraphStateException {
		ClassifySupportRequestNode classifySupportRequestNode = new ClassifySupportRequestNode(chatClient);
		BillingSupportNode billingSupportNode = new BillingSupportNode(chatClient);
		TechnicalSupportNode technicalSupportNode = new TechnicalSupportNode(chatClient);

		StateGraph<SupportState> stateGraph = new StateGraph<>(Map.of(), initData -> new SupportState(initData))
				.addNode(CLASSIFY, AsyncNodeAction.node_async(classifySupportRequestNode))
				.addNode(BILLING, AsyncNodeAction.node_async(billingSupportNode))
				.addNode(TECHNICAL, AsyncNodeAction.node_async(technicalSupportNode))
				.addEdge(GraphDefinition.START, CLASSIFY)
				.addConditionalEdges(
						CLASSIFY,
						AsyncEdgeAction.edge_async(state -> {
							String category = state.getCategory();
							return switch (category) {
								case BILLING -> BILLING;
								case TECHNICAL -> TECHNICAL;
								default -> TECHNICAL;
							};
						}),
						Map.of(BILLING, BILLING, TECHNICAL, TECHNICAL)
				)
				.addEdge(BILLING, GraphDefinition.END)
				.addEdge(TECHNICAL, GraphDefinition.END);

		return stateGraph.compile();
	}
}

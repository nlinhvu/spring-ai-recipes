package dev.linhvu.example.graph_workflow_langgraph4j;

import java.util.Map;

import org.bsc.langgraph4j.state.AgentState;

public class SupportState extends AgentState {

	public static final String USER_QUESTION = "user_question";
	private static final String CATEGORY = "category";
	private static final String RESOLUTION = "resolution";

	public SupportState(Map<String, Object> initData) {
		super(initData);
	}

	public String getUserQuestion() {
		return (String) value(USER_QUESTION).orElse("");
	}

	public String getCategory() {
		return (String) value(CATEGORY).orElse("");
	}

	public String getResolution() {
		return (String) value(RESOLUTION).orElse("");
	}
}

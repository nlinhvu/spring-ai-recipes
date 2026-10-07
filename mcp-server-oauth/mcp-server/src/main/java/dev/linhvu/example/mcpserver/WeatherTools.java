package dev.linhvu.example.mcpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

	// “hint” attributes being specified via the annotations attribute. These are optional, but provide some useful hints to the client. In this case:
	//
	// This is not an open-world tool, meaning that it is completely self-contained and does use an external APIs. If we were to implement this tool to use a weather API, then we should set it to true or just leave it off (it defaults to true ).
	// This tool does not change or delete anything. It only fetches weather information. Therefore it is not a destructive tool.
	// As a consequence of being non-destructive, this tool is considered idempotent. But if this were a destructive tool, you’d want to consider the idempotency of the tool and set the idempotentHint attribute accordingly.
	@PreAuthorize("hasAuthority('SCOPE_meteorology')")
	@McpTool(name = "get-weather-for-zipcode",
			description = "Get weather for a given zipcode",
			annotations = @McpTool.McpAnnotations(
					openWorldHint = false,
					destructiveHint = false,
					idempotentHint = true))
	public Weather getWeatherForZipcode(
			@McpToolParam(description = "The zipcode to get the weather for") String zipcode) {
		return new Weather(zipcode, "Raining cats and dogs", "81.5F");
	}

	public record Weather(
			String zipcode,
			String conditions,
			String temperature) {
	}

}

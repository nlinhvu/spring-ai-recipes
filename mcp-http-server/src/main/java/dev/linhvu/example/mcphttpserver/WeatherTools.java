package dev.linhvu.example.mcphttpserver;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

	@McpTool(name = "get-weather-for-zipcode",
			description = "Get weather for a given zipcode")
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

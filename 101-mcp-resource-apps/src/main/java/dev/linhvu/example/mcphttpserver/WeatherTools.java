package dev.linhvu.example.mcphttpserver;

import java.util.Map;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.MetaProvider;
import org.springframework.stereotype.Service;

@Service
public class WeatherTools {

	@McpTool(name = "get-weather-for-zipcode",
			description = "Get weather for a given zipcode", metaProvider = AppsMetaProvider.class)
	public Weather getWeatherForZipcode(
			@McpToolParam(description = "The zipcode to get the weather for") String zipcode) {
		return new Weather(zipcode, "Raining cats and dogs", "81.5F");
	}

	public static final class AppsMetaProvider implements MetaProvider {

		@Override
		public Map<String, Object> getMeta() {
			return Map.of(
					"ui", Map.of("resourceUri", "ui://weather/weather.html"),
					"ui/resourceUri", "ui://weather/weather.html"
			);
		}
	}

	public record Weather(
			String zipcode,
			String conditions,
			String temperature) {
	}

}

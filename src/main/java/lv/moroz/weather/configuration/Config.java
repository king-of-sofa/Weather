package lv.moroz.weather.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	@ConfigurationProperties(prefix = "weather-api")
	public OpenWeatherProperties openWeatherProperties() {
		return new OpenWeatherProperties();
	}
}
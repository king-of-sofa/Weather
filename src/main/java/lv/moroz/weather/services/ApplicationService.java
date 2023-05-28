package lv.moroz.weather.services;

import lombok.extern.slf4j.Slf4j;
import lv.moroz.weather.configuration.OpenWeatherProperties;
import lv.moroz.weather.dto.LocationRecord;
import lv.moroz.weather.dto.WeatherRecord;
import lv.moroz.weather.model.Weather;
import lv.moroz.weather.repository.WeatherRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@CacheConfig(cacheNames = "weather")
public class ApplicationService {
	private final WeatherRepository weatherRepository;
	private final RestTemplate restTemplate;

	private final CacheManager cacheManager;

	private final OpenWeatherProperties openWeatherProperties;

	public ApplicationService(WeatherRepository weatherRepository, RestTemplate restTemplate, CacheManager cacheManager, OpenWeatherProperties openWeatherProperties) {
		this.weatherRepository = weatherRepository;
		this.restTemplate = restTemplate;
		this.cacheManager = cacheManager;
		this.openWeatherProperties = openWeatherProperties;
	}

	public LocationRecord getLocation(String ipAddress) {
		log.info("Getting location for ip: {}", ipAddress);
		validateIpAddress(ipAddress);

		String url = String.format("https://ipapi.co/%s/json/", ipAddress);
		log.info("Requesting location from: " + url);

		try {
			LocationRecord location = restTemplate.getForObject(url, LocationRecord.class);
			log.info("Received {}", location);
			return location;
		} catch (Exception e) {
			log.error("Could not get location: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public WeatherRecord getWeather(LocationRecord location) {
		log.info("Getting weather for location: {}", location);

		String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s", location.latitude(), location.longitude(), openWeatherProperties.getKey(), openWeatherProperties.getUnits());
		log.info("Requesting weather from: {}", url);
		try {
			WeatherRecord weatherRecord = restTemplate.getForObject(url, WeatherRecord.class);
			log.info("Received {}", weatherRecord);
			return weatherRecord;
		} catch (Exception e) {
			log.error("Could not get weather: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	public Weather fetchCurrentWeather(String ipAddress) {
		LocationRecord location = getLocation(ipAddress);
		WeatherRecord weatherRecord = getWeather(location);
		try {
			Weather weatherModel = Weather.builder()
					.ip(location.ip())
					.city(location.city())
					.latitude(location.latitude())
					.longitude(location.longitude())
					.temperature(weatherRecord.main().get("temp").toString())
					.build();
			evictAllCaches();
			log.info("Saving weather: {}", weatherModel);
			return weatherRepository.save(weatherModel);
		} catch (Exception e) {
			log.error("Could not save weather: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@Cacheable
	public List<Weather> fetchWeatherHistoryByIp(String ipAddress) {
		validateIpAddress(ipAddress);
		log.info("Getting weather history for IP address: {}", ipAddress);
		List<Weather> weatherHistory = weatherRepository.findByIp(ipAddress);

		if (weatherHistory.isEmpty()) {
			log.warn("No weather history found for IP address: {}", ipAddress);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return weatherHistory;
	}

	@Cacheable
	public List<Weather> fetchWeatherHistoryByCoordinates(String latitude, String longitude) {
		log.info("Getting weather history for latitude: {} and longitude: {}", latitude, longitude);
		List<Weather> weatherHistory = weatherRepository.findByCoordinates(latitude, longitude);

		if (weatherHistory.isEmpty()) {
			log.warn("No weather history found for latitude: {} and longitude: {}", latitude, longitude);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return weatherHistory;
	}

	private void evictAllCaches() {
		cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
	}

	private void validateIpAddress(String ipAddress) {
		Pattern pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		Matcher matcher = pattern.matcher(ipAddress);
		if (!matcher.matches()) {
			log.error("Invalid IP address: {}", ipAddress);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}
	}
}

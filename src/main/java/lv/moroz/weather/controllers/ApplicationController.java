package lv.moroz.weather.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lv.moroz.weather.model.Weather;
import lv.moroz.weather.services.ApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/weather")
public class ApplicationController {
	private final ApplicationService applicationService;

	public ApplicationController(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@GetMapping(value = {"/", "/{ipAddress}"}, produces = "application/json")
	public Weather getWeather(HttpServletRequest httpServletRequest, @PathVariable(name ="ipAddress", required = false) String ipAddress) {
		if (ipAddress == null) {
			ipAddress = httpServletRequest.getRemoteAddr();
		}
		log.info("Received weather request for ip: {}", ipAddress);
		return applicationService.fetchCurrentWeather(ipAddress);
	}

	@GetMapping(value = {"/history/ip/", "/history/ip/{ipAddress}"}, produces = "application/json")
	public List<Weather> getWeatherHistory(HttpServletRequest httpServletRequest, @PathVariable(name ="ipAddress", required = false) String ipAddress) {
		if (ipAddress == null) {
			ipAddress = httpServletRequest.getRemoteAddr();
		}
		log.info("Received weather history request for ip: {}", ipAddress);
		return applicationService.fetchWeatherHistoryByIp(ipAddress);
	}

	@GetMapping(value = {"/history/coordinates/{latitude}/{longitude}"}, produces = "application/json")
	public List<Weather> getWeatherHistory(@PathVariable String latitude, @PathVariable String longitude) {
		log.info("Received weather history request for coordinates: {}, {}", latitude, longitude);
		return applicationService.fetchWeatherHistoryByCoordinates(latitude, longitude);
	}

}

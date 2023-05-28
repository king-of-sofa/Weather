package lv.moroz.weather;

import lv.moroz.weather.configuration.OpenWeatherProperties;
import lv.moroz.weather.dto.LocationRecord;
import lv.moroz.weather.dto.WeatherRecord;
import lv.moroz.weather.services.ApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class WeatherUnitTests {

    @Mock
    RestTemplate restTemplate;

    @Mock
    OpenWeatherProperties openWeatherProperties;

    @InjectMocks
    ApplicationService applicationService;

    @Test
    @DisplayName("getLocation method test")
    void getLocation() {
        // Given
        String ip = "212.3.199.54";
        LocationRecord locationRecord = new LocationRecord("212.3.199.54", "Riga", "56.946", "24.105");

        // When
        when(restTemplate.getForObject(anyString(), eq(LocationRecord.class))).thenReturn(locationRecord);
        applicationService.getLocation(ip);

        // Then
        verify(restTemplate).getForObject(anyString(), eq(LocationRecord.class));
    }

    @Test
    @DisplayName("getLocation method test with invalid IP address")
    void getLocationInvalidIpAddress() {
        // Given
        String ip = "600.600.600.600";

        // When
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> applicationService.getLocation(ip));

        // Then
        assertEquals("400 BAD_REQUEST", exception.getMessage());
    }

    @Test
    @DisplayName("getLocation method test with unavailable service")
    void getLocationServiceNotAvailable() {
        // Given
        String ip = "212.3.199.54";

        // When
        when(restTemplate.getForObject(anyString(), eq(LocationRecord.class))).thenThrow(new RestClientException("Any exception"));

        // Then
        Exception exception = assertThrows(Exception.class, () -> applicationService.getLocation(ip));
        assertEquals("500 INTERNAL_SERVER_ERROR", exception.getMessage());
    }

    @Test
    @DisplayName("getWeather method test")
    void getWeather() {
        // Given
        LocationRecord locationRecord = new LocationRecord("212.3.199.54", "Riga", "56.946", "24.105");
        WeatherRecord weatherRecord = new WeatherRecord("Riga", Map.of("temp", "10.0"));

        // When
        when(openWeatherProperties.getKey()).thenReturn("key");
        when(openWeatherProperties.getUnits()).thenReturn("metric");
        when(restTemplate.getForObject(anyString(), eq(WeatherRecord.class))).thenReturn(weatherRecord);
        applicationService.getWeather(locationRecord);

        // Then
        verify(restTemplate).getForObject(anyString(), eq(WeatherRecord.class));
    }

    @Test
    @DisplayName("getWeather method test with unavailable service")
    void getWeatherServiceNotAvailable() {
        // Given
        LocationRecord locationRecord = new LocationRecord("103.188.59.255", "New York", "44.684", "73.450");

        // When
        when(openWeatherProperties.getKey()).thenReturn("key");
        when(openWeatherProperties.getUnits()).thenReturn("metric");
        when(restTemplate.getForObject(anyString(), eq(WeatherRecord.class))).thenThrow(new RestClientException("Any exception"));

        // Then
        Exception exception = assertThrows(Exception.class, () -> applicationService.getWeather(locationRecord));
        assertEquals("500 INTERNAL_SERVER_ERROR", exception.getMessage());
    }
}
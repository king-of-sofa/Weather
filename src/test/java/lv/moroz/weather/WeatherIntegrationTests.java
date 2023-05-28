package lv.moroz.weather;

import lv.moroz.weather.configuration.OpenWeatherProperties;
import lv.moroz.weather.dto.LocationRecord;
import lv.moroz.weather.dto.WeatherRecord;
import lv.moroz.weather.model.Weather;
import lv.moroz.weather.repository.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherRepository weatherRepository;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private OpenWeatherProperties openWeatherProperties;

    @Test
    @DisplayName("/weather/ endpoint test")
    void fetchCurrentWeather() throws Exception {
        // Given
        String ip = "212.3.199.54";
        LocationRecord locationRecord = new LocationRecord(ip, "Riga", "56.946", "24.105");
        WeatherRecord weatherRecord = new WeatherRecord("Riga", Map.of("temp", "10.0"));
        Weather weather = Weather.builder()
                .ip(locationRecord.ip())
                .city(weatherRecord.city())
                .latitude(locationRecord.latitude())
                .longitude(locationRecord.longitude())
                .temperature(weatherRecord.main().get("temp").toString())
                .build();

        // When
        when(restTemplate.getForObject(anyString(), eq(LocationRecord.class))).thenReturn(locationRecord);
        when(openWeatherProperties.getKey()).thenReturn("key");
        when(openWeatherProperties.getUnits()).thenReturn("metric");
        when(restTemplate.getForObject(anyString(), eq(WeatherRecord.class))).thenReturn(weatherRecord);
        when(weatherRepository.save(weather)).thenReturn(weather);

        // Then
        mockMvc.perform(get("/weather/{ip}", ip)).
                andDo(print()).
                andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());

        verify(weatherRepository, times(1)).save(weather);
    }

    @Test
    @DisplayName("/weather/ endpoint test with invalid IP address")
    void fetchCurrentWeatherInvalidIp() throws Exception {
        // Given
        String ip = "600.600.600.600";

        // Then
        mockMvc.perform(get("/weather/{ip}", ip)).
                andDo(print()).
                andExpect(status().isBadRequest());
        verify(weatherRepository, times(0)).save(any());
    }


    @Test
    @DisplayName("/weather/ endpoint test with unavailable service")
    void fetchCurrentWeatherServiceUnavailable() throws Exception {
        // Given
        String ip = "212.3.199.54";
        LocationRecord locationRecord = new LocationRecord(ip, "Riga", "56.946", "24.105");

        // When
        when(restTemplate.getForObject(anyString(), eq(LocationRecord.class))).thenReturn(locationRecord);
        when(openWeatherProperties.getKey()).thenReturn("key");
        when(openWeatherProperties.getUnits()).thenReturn("metric");
        when(restTemplate.getForObject(anyString(), eq(WeatherRecord.class))).thenThrow(new RestClientException("Any exception"));

        // Then
        mockMvc.perform(get("/weather/{ip}", ip)).
                andDo(print()).
                andExpect(status().isInternalServerError());
        verify(weatherRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("/weather/history/ip/ endpoint test")
    void fetchWeatherHistoryByIp() throws Exception {
        // Given
        String ip = "212.3.199.54";
        Weather weather = Weather.builder()
                .id(1L)
                .ip(ip)
                .city("Riga")
                .latitude("56.946")
                .longitude("24.105")
                .temperature("10.0")
                .build();

        // When
        when(weatherRepository.findByIp(ip)).thenReturn(List.of(weather));

        // Then
        mockMvc.perform(get("/weather/history/ip/{ip}", ip)).
                andDo(print()).
                andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        verify(weatherRepository, times(1)).findByIp(ip);
    }

    @Test
    @DisplayName("/weather/history/ip/ endpoint test with invalid IP address")
    void fetchWeatherHistoryByIpInvalidIp() throws Exception {
        // Given
        String ip = "600.600.600.600";

        // Then
        mockMvc.perform(get("/weather/history/ip/{ip}", ip)).
                andDo(print()).
                andExpect(status().isBadRequest());
        verify(weatherRepository, times(0)).findByIp(ip);
    }

    @Test
    @DisplayName("/weather/history/ip/ endpoint test with unknown IP address")
    void fetchWeatherHistoryByIpUnknownIp() throws Exception {
        // Given
        String ip = "212.3.199.53";

        // When
        when(weatherRepository.findByIp(ip)).thenReturn(List.of());

        // Then
        mockMvc.perform(get("/weather/history/ip/{ip}", ip)).
                andDo(print()).
                andExpect(status().isNotFound());
        verify(weatherRepository, times(1)).findByIp(ip);
    }

    @Test
    @DisplayName("/weather/history/coordinates/ endpoint test")
    void fetchWeatherHistoryByCoordinates() throws Exception {
        // Given
        String latitude = "56.946";
        String longitude = "24.105";
        Weather weather = Weather.builder()
                .id(1L)
                .ip("212.3.199.54")
                .city("Riga")
                .latitude("56.946")
                .longitude("24.105")
                .temperature("10.0")
                .build();

        // When
        when(weatherRepository.findByCoordinates(latitude, longitude)).thenReturn(List.of(weather));

        // Then
        mockMvc.perform(get("/weather/history/coordinates/{latitude}/{longitude}", latitude, longitude)).
                andDo(print()).
                andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        verify(weatherRepository, times(1)).findByCoordinates(latitude, longitude);
    }

    @Test
    @DisplayName("/weather/history/coordinates/ endpoint test with unknown coordinates")
    void fetchWeatherHistoryByCoordinatesUnknownCoordinates() throws Exception {
        // Given
        String latitude = "56.946";
        String longitude = "24.105";

        // When
        when(weatherRepository.findByCoordinates(latitude, longitude)).thenReturn(List.of());

        // Then
        mockMvc.perform(get("/weather/history/coordinates/{latitude}/{longitude}", latitude, longitude)).
                andDo(print()).
                andExpect(status().isNotFound());
        verify(weatherRepository, times(1)).findByCoordinates(latitude, longitude);
    }

    @Test
    @DisplayName("/weather/history/ip/ cache endpoint test")
    void fetchWeatherHistoryCacheByIp() throws Exception {
        // Given
        String ip = "212.3.199.52";
        Weather weather = Weather.builder()
                .id(1L)
                .ip(ip)
                .city("Riga")
                .latitude("56.946")
                .longitude("24.105")
                .temperature("10.0")
                .build();

        // When
        when(weatherRepository.findByIp(ip)).thenReturn(List.of(weather));

        // Then
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(get("/weather/history/ip/{ip}", ip)).
                    andDo(print()).
                    andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)).
                    andExpect(status().isOk());
        }
        verify(weatherRepository, times(1)).findByIp(ip);
    }
}
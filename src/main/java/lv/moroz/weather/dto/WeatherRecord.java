package lv.moroz.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherRecord(
        @NonNull
        @JsonProperty("name")
        String city,

        @NonNull
        @JsonProperty("main") Map<String, Object> main) {

}

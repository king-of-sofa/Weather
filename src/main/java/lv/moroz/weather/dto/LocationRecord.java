package lv.moroz.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocationRecord(@NonNull
                             String ip,
                             @NonNull
                             String city,
                             @NonNull
                             String latitude,
                             @NonNull
                             String longitude) {
}

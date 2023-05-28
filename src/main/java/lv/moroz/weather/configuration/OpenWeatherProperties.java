package lv.moroz.weather.configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OpenWeatherProperties {
    private String key;
    private String units;
}

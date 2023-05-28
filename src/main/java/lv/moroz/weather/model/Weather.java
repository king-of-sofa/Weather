package lv.moroz.weather.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
public class Weather {

	@Id
	private long id;

	private String ip;

	private String city;

	private String latitude;

	private String longitude;

	private String temperature;
}

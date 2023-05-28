package lv.moroz.weather.repository;

import lv.moroz.weather.model.Weather;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherRepository extends CrudRepository<Weather, Long> {

	@Query("SELECT * FROM weather WHERE ip = :ip")
	List<Weather> findByIp(String ip);

	@Query("SELECT * FROM weather WHERE latitude = :latitude AND longitude = :longitude")
	List<Weather> findByCoordinates(String latitude, String longitude);
}

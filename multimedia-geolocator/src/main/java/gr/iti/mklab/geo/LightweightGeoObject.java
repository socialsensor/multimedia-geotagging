package gr.iti.mklab.geo;

/**
 * A lightweight version of a Geonames object (for memory efficiency reasons)
 * @author papadop
 *
 */
public class LightweightGeoObject {

	protected int geonamesId;
	protected double lat, lon;
	protected String name;
	protected String countryCode;
	
	
	public LightweightGeoObject(GeoObject city){
		this(city.getId(), city.getLat(), city.getLon(), city.getName(), city.getCountryCode());
	}
	
	public LightweightGeoObject(int gnId, double lat, double lon, String name, String countryCode){
		this.geonamesId = gnId;
		this.lat = lat;
		this.lon = lon;
		this.name = name;
		this.countryCode = countryCode;
	}
	

	@Override
	public String toString() {
		return "LightweightCity [geonamesId=" + geonamesId + ", name=" + name + 
				", countryCode=" + countryCode + ", lat=" + lat + ", lon=" + lon + "]";
	}

	
	public int getGeonamesId() {
		return geonamesId;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public String getName() {
		return name;
	}

	public String getCountryCode() {
		return countryCode;
	}
	
	
}

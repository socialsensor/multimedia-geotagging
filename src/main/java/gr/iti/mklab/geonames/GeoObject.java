package gr.iti.mklab.geonames;

/**
 * A direct mapping of the Geonames object as described in: http://download.geonames.org/export/dump/readme.txt (main geoname table)
 * @author papadop
 *
 */
public class GeoObject {

	
	protected int id;
	protected String name;
	protected String asciiName;
	protected String[] alternateNames;
	protected double lat, lon;
	protected char featureClass;
	protected String featureCode;
	protected String countryCode;
	protected String[] alternateCountryCodes;
	
	protected String[] adminCodes; // from 1 to 4
	protected long population;
	protected int elevation;
	protected int dem; // digital elevation model
	protected String timezone;
	protected String modificationDate;
	
	

	// constructor using the line format of the geonames files
	public GeoObject(String line){
		String[] parts = line.split("\t");
		
		this.id = Integer.parseInt(parts[0]);
		this.name = parts[1];
		this.asciiName = parts[2];
		
		this.alternateNames = parts[3].split(",");
		this.lat = Double.parseDouble(parts[4]);
		this.lon = Double.parseDouble(parts[5]);
		
		if (parts[6].length() > 0) {
			this.featureClass = parts[6].charAt(0);
		} 
		
		this.featureCode = parts[7];
		this.countryCode = parts[8];
		if (parts[9].trim().length() < 1) {
			this.alternateCountryCodes = new String[0];
		} else {
			this.alternateCountryCodes = parts[9].split(",");
		}
		
		this.adminCodes = new String[4];
		this.adminCodes[0] = parts[10];
		this.adminCodes[1] = parts[11];
		this.adminCodes[2] = parts[12];
		this.adminCodes[3] = parts[13];
		
		this.population = Long.parseLong(parts[14]);
		
		if (parts[15].trim().length() > 0) {
			this.elevation = Integer.parseInt(parts[15]);
		} else {
			this.elevation = 0;
		}
		
		if (parts[16].trim().length() > 0) {
			this.dem = Integer.parseInt(parts[16]);
		} else {
			this.dem = 0;
		}
		
		this.timezone = parts[17];
		this.modificationDate = parts[18];
		
	}

	
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAsciiName() {
		return asciiName;
	}
	
	public String[] getAlternateNames() {
		return alternateNames;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public char getFeatureClass() {
		return featureClass;
	}

	public String getFeatureCode() {
		return featureCode;
	}

	public String getCountryCode() {
		return countryCode;
	}
	

	public String[] getAlternateCountryCodes() {
		return alternateCountryCodes;
	}

	public String[] getAdminCodes() {
		return adminCodes;
	}

	public long getPopulation() {
		return population;
	}

	public int getElevation() {
		return elevation;
	}

	public int getDem() {
		return dem;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getModificationDate() {
		return modificationDate;
	}
	
}

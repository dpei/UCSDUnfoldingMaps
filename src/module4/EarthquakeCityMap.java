package module4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import module4.EarthquakeMarker;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author_modified Dong Pei
 * @date: July 17, 2015
 * @last_modified: May. 3. 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = true;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";
		
		// WHEN TAKING THIS QUIZ: Uncomment the next line
		//earthquakesURL = "quiz1.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	// helper method to draw key in GUI
	// TODO: Update this method as appropriate
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		rect(25, 50, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 60);
		text("Size ~ Magnitude", 50, 165);
		// Three shapes represent city and earthquakes
		fill(color(255, 0, 0));
		triangle(50, 75, 45, 85, 55, 85);
		fill(color(255,255,255));
		ellipse(50, 105, 10, 10);
		fill(color(255,255,255));
		rect(45, 125, 10, 10);
		// Three color represent the depth of earthquakes
		
		fill(color(255, 255, 0));
		ellipse(50, 185, 10, 10);
		fill(color(0, 0, 255));
		ellipse(50, 215, 10, 10);
		fill(color(255, 0, 0));
		ellipse(50, 245, 10, 10);
		
		
		
		// Add keys
		fill(0, 0, 0);
		text("City marker", 75, 80);
		text("Land quake", 75, 100);
		text("Ocean quake", 75, 125);
		text("Shallow", 75, 185);
		text("Intermediate", 75, 215);
		text("Deep", 75, 245);
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		for (Marker country : countryMarkers){
			if (isInCountry(earthquake, country)){ 
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes() 
	{
		// Initiate the ocean earthquake number
		int oceanNumber = quakeMarkers.size();
		// Loop over each country and get the number of earthquakes in that
		// country
		for (Marker country : countryMarkers){
			String countryName = country.getStringProperty("name");
			
			int quakeNumber = 0;
			// Loop over each earthquake to see if it belongs to that country
			for (Marker earthquake : quakeMarkers){
				
				// Cast the Marker object earthquake into EarthquakeMarker to
				// specify its class at runtime.
				EarthquakeMarker eqMarker = (EarthquakeMarker)earthquake;
				if (eqMarker.isOnLand() && countryName.equals(eqMarker.getStringProperty("country"))) {
						quakeNumber++;
				}
			}
			
			if (quakeNumber > 0){
				System.out.println("The country "+countryName+" has "+quakeNumber+" earthquakes.");
				oceanNumber=oceanNumber-quakeNumber;
			}
		}
		System.out.println("The ocean has "+oceanNumber+" earthquakes.");
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake 
	// feature if it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}

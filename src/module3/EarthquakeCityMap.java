package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import processing.core.PApplet;
import processing.core.PShape;
//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * author modify Dong Pei
 * Date: July 17, 2015
 * last modified: Apr. 26. 2017
 * */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = true;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	SimplePointMarker earthquakeMarker;
	
	//feed with magnitude 2.5+ Earthquakes
	// TODO import error "White spaces are required between publicId and systemId"


	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	//
		
	/* (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			earthquakesURL = "2.5_week.atom";
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();
	    
	    //Use provided parser to collect properties for each earthquake
	    //PointFeatures have a getLocation method
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    // Loop through the PointFeature objects and add each point into map.
	    for (int i = 0; i< earthquakes.size(); i++){
	    	// For each PointFeature inside the Earthquakes list, we created a maker
	    	PointFeature f = earthquakes.get(i);
	    	SimplePointMarker marker = createMarker(f);
	    	Object magObj = f.getProperty("magnitude");
	    	float mag = Float.parseFloat(magObj.toString());
	    	
	    	if (mag <= 4.0 ){
	    		// Blue color
	    		marker.setColor(color(0,0,225));
	    		marker.setRadius((float) 5.0);
	    	}
	    	else if (mag >= 5.0){
	    		// Red color
	    		marker.setColor(color(225,0,0));
	    		marker.setRadius((float) 15.0);
	    	}
	    	else {
	    		// Yellow color
	    		marker.setColor(color(255,225,0));
	    		marker.setRadius((float) 10.0);
	    	}
	    	map.addMarker(marker);
	    }
	    
	    // These print statements show you (1) all of the relevant properties 
	    // in the features, and (2) how to get one property and use it
	    if (earthquakes.size() > 0) {
	    	PointFeature f = earthquakes.get(0);
	    	System.out.println(f.getProperties());
	    	Object magObj = f.getProperty("magnitude");
	    	float mag = Float.parseFloat(magObj.toString());
	    	// PointFeatures also have a getLocation method
	    }
	    
	    // Here is an example of how to use Processing's color method to generate 
	    // an int that represents the color yellow.  
	    int yellow = color(255, 255, 0);
	    
	    //TODO: Add code here as appropriate
	}
		
	// A suggested helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature feature)
	{
		// finish implementing and use this method, if it helps.
		return new SimplePointMarker(feature.getLocation());
	}
	
	public void draw() {
	    background(10);
	    map.draw();	
	    addKey();
	}


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() 
	{	
		// Remember you can use Processing's graphics methods here
		PShape square;
		// Draw a piece of rectangle
		square = createShape(RECT, 0, 0,150,250);
		shape(square, 25, 50);
		
		textSize(12);
		fill(0, 0, 0);
		
		text("Earthquake Key", 50, 100);
		text("5.0+ Magnitude", 65, 155);
		text("4.0+ Magnitude", 60, 215); 
		text("Below 4.0", 60, 265); 
		
		// Draw three dot with different radiusbackgroundImg.resize
		fill(255,0,0);
		ellipse(50, 150, 15, 15);
		fill(255,255,0);
		ellipse(50, 210, 10, 10);
		fill(0,0,255);
		ellipse(50, 260, 5, 5);
		// Reset background color to white
		fill(255,255,255);
		// Add three notes next to the three dots.

	}
}

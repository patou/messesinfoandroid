package cef.messesinfo.maps;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import cef.messesinfo.R;
import cef.messesinfo.provider.Church;

import com.google.android.maps.GeoPoint;

import de.android1.overlaymanager.ManagedOverlayItem;

/**
 * Church point
 * @author desaintsteban.p
 *
 */
public class ChurchPt extends ManagedOverlayItem {
    private Map<String, String> data;

    /**
     * Create a new Church marker
     * @param data
     */
    public ChurchPt(Map<String, String> data) {
	super(createGeoPt(data), buildTitle(data), buildSnippet(data));
	this.setData(data);
    }

    /**
     * Build the snippet of the marker
     * @param data
     * @return
     */
    private static String buildSnippet(Map<String, String> data) {
	String snippet = data.get(Church.COMMUNITY) + "\n" + data.get(Church.ZIPCODE) + " " + data.get(Church.CITY);
	String next_mass = data.get(Church.NEXT_MASS);
	if (next_mass != null) {
	    try {
		Date date_next_mass = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(next_mass.substring(0, 16));
		snippet += "\nProchaine messe : " + new SimpleDateFormat("EEE d 'à' HH'h'mm").format(date_next_mass);//TODO Translate this string
	    } catch (ParseException e) {
		e.printStackTrace();
	    }
	}
	return snippet;
    }

    /**
     * Build the title of the marker
     * @param data
     * @return
     */
    private static String buildTitle(Map<String, String> data) {
	return data.get(Church.NAME);
    }

    /**
     * Create a new Geo point
     * @param data
     * @return
     */
    public static GeoPoint createGeoPt(Map<String, String> data) {
	return createGeoPt(data.get(Church.LAT), data.get(Church.LNG));
    }
    
    /**
     * Create a new Geo point
     * @param latitude
     * @param longitude
     * @return
     */
    public static GeoPoint createGeoPt(String latitude, String longitude) {
	int lat = (int) (Double.parseDouble(latitude) * 1E6);
	int lon = (int) (Double.parseDouble(longitude) * 1E6);
	GeoPoint point = new GeoPoint(lat, lon);
	return point;
    }

    public void setData(Map<String, String> data) {
	this.data = data;
    }

    public Map<String, String> getData() {
	return data;
    }
}

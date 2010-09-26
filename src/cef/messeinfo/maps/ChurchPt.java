package cef.messeinfo.maps;

import java.util.Map;

import cef.messeinfo.provider.Church;

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
	return data.get(Church.ADRESSE) + " " + data.get(Church.COMMUNE);
    }

    /**
     * Build the title of the marker
     * @param data
     * @return
     */
    private static String buildTitle(Map<String, String> data) {
	return data.get(Church.NOM);
    }

    /**
     * Create a new Geo point
     * @param data
     * @return
     */
    public static GeoPoint createGeoPt(Map<String, String> data) {
	return createGeoPt(data.get(Church.LAT), data.get(Church.LON));
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

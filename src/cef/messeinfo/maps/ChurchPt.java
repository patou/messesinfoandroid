package cef.messeinfo.maps;

import java.util.Map;

import cef.messeinfo.provider.Church;

import com.google.android.maps.GeoPoint;

import de.android1.overlaymanager.ManagedOverlayItem;


public class ChurchPt extends ManagedOverlayItem {
    private Map<String,String> data;
    
    public ChurchPt(Map<String,String> data) {
        super(createGeoPt(data), buildTitle(data), buildSnippet(data));
        this.setData(data);
    }

    private static String buildSnippet(Map<String, String> data) {
        return data.get(Church.ADRESSE) + " " + data.get(Church.COMMUNE);
    }

    private static String buildTitle(Map<String, String> data) {
        return data.get(Church.NOM);
    }

    private static GeoPoint createGeoPt(Map<String, String> data) {
        int lat = (int) (Double.parseDouble(data.get(Church.LAT)) * 1E6);
        int lon = (int) (Double.parseDouble(data.get(Church.LON)) * 1E6);
        GeoPoint point = new GeoPoint(lat,lon);
        return point;
    }

	public void setData(Map<String,String> data) {
		this.data = data;
	}

	public Map<String,String> getData() {
		return data;
	}
}

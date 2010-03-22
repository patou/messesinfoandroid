package cef.messeinfo.maps;

import java.util.ArrayList;
import java.util.Map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;

public class ChurchItemizedOverlay extends ManagedOverlay {

	private ArrayList<Map<String, String>> mOverlaysItem = new ArrayList<Map<String, String>>();
	
	public ChurchItemizedOverlay(OverlayManager manager, String name, Drawable drawable) {
		super(manager, name, drawable);
	}

	public void addChurchItem(Map<String, String> item) {
        mOverlaysItem.add(item);
        populate();
	}

	public ManagedOverlayItem createItem(Map<String, String> item) {
		int lat = (int) (Double.parseDouble(item.get("lat")) * 1E6);
        int lon = (int) (Double.parseDouble(item.get("lon")) * 1E6);
        GeoPoint point = new GeoPoint(lat,lon);
        ManagedOverlayItem overlayitem = new ManagedOverlayItem(point, item.get("nom"), item.get("paroisse") + " " + item.get("commune"));
        return overlayitem;
	}

    public Map<String, String> getChurchItem(int index) {
        return mOverlaysItem.get(index);
    }
}

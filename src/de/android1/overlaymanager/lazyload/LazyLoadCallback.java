package de.android1.overlaymanager.lazyload;

import java.util.List;

import com.google.android.maps.GeoPoint;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;


public interface LazyLoadCallback {

	public List<ManagedOverlayItem> lazyload(GeoPoint topLeft, GeoPoint bottomRight, ManagedOverlay overlay) throws LazyLoadException;

}



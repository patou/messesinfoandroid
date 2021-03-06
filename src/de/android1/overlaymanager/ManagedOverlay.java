package de.android1.overlaymanager;

import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import de.android1.overlaymanager.lazyload.*;

public class ManagedOverlay extends ItemizedOverlay<ManagedOverlayItem> {

    private static final java.lang.String LOG_TAG = "Maps_ManagedOverlay";

    protected String name;
    private ArrayList<ManagedOverlayItem> items = new ArrayList<ManagedOverlayItem>();
    protected MarkerRenderer customMarkerRenderer;
    protected Drawable defaultMarker;
    protected ManagedOverlayGestureDetector gs;
    protected OverlayManager manager;

    protected LazyLoadManager lazyLoadManager;

    protected boolean isLazyLoadEnabled = false;

    protected boolean longPressFinished = false;
    protected boolean zoomFinished = false;

    protected int lastZoomlevel = -1;

    private Rect touchableBounds = new Rect();
    private int minTouchableWidth = 48;
    private int minTouchableHeight = 48;

    protected ManagedOverlay(OverlayManager manager, String name, Drawable defaultMarker) {
        super(defaultMarker);
        if (defaultMarker.getBounds().isEmpty())
            boundCenterBottom(defaultMarker);
        this.defaultMarker = defaultMarker;
        this.name = name;
        this.manager = manager;
        initGestureDetect();
    }

    protected ManagedOverlay(OverlayManager manager, Drawable defaultMarker) {
        this(manager, null, defaultMarker);
    }

    protected ManagedOverlay(Drawable defaultMarker) {
        this(null, null, defaultMarker);
    }

    private void initGestureDetect() {
        ManagedOverlayGestureDetector.ManagedGestureListener managedGestureListener = new ManagedOverlayGestureDetector.ManagedGestureListener(this);
        this.gs = new ManagedOverlayGestureDetector(manager.ctx, managedGestureListener, this);
        managedGestureListener.setDetector(gs);
    }

    public void invokeLazyLoad(long delay) {
        if (isLazyLoadEnabled) {
            Log.d(LOG_TAG, "invokeLazyLoad(" + delay + ")");
            getLazyLoadManager().call(delay);
        }
    }


    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (longPressFinished) {
            longPressFinished = false;
            this.gs.invokeLongPressFinished();
        }
        if (zoomFinished) {
            zoomFinished = false;
            this.gs.invokeZoomFinished();
        }
        super.draw(canvas, mapView, false);
        if (lastZoomlevel == -1)
            lastZoomlevel = mapView.getZoomLevel();
        if (mapView.getZoomLevel() != lastZoomlevel) {
            this.gs.invokeZoomEvent(lastZoomlevel, mapView.getZoomLevel());
            lastZoomlevel = mapView.getZoomLevel();
        }
    }

    protected void init() {
        invokeLazyLoad(0);
    }

    public void add(ManagedOverlayItem item) {
        item.setOverlay(this);
        items.add(item);
        setLastFocusedIndex(-1);
        populate();
    }

    public void addAll(List<ManagedOverlayItem> items) {
        this.items.clear();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                ManagedOverlayItem managedOverlayItem = items.get(i);
                managedOverlayItem.setOverlay(this);
                this.items.add(managedOverlayItem);
            }
            setLastFocusedIndex(-1);
            populate();
        } else {
            setLastFocusedIndex(-1);
            populate();
        }
    }

    public ManagedOverlayItem createItem(GeoPoint p) {
        ManagedOverlayItem item = new ManagedOverlayItem.Builder(p).create();
        add(item);
        return item;
    }
    
    public ManagedOverlayItem createItem(GeoPoint p,String titel) {
        ManagedOverlayItem item = new ManagedOverlayItem.Builder(p).name(titel).create();
        add(item);
        return item;
    }
    public ManagedOverlayItem createItem(GeoPoint p,String titel,String snippet) {
        ManagedOverlayItem item = new ManagedOverlayItem.Builder(p).name(titel).snippet(snippet).create();
        add(item);
        return item;
    }

    public ManagedOverlayItem.Builder itemBuilder() {
        return new ManagedOverlayItem.Builder();
    }

    public void remove(ManagedOverlayItem item) {
        items.remove(item);
        setLastFocusedIndex(-1);
        populate();
    }

    public void remove(int index) {
        items.remove(index);
        setLastFocusedIndex(-1);
        populate();
    }

    @Override
    protected ManagedOverlayItem createItem(int i) {
        return items.get(i);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        this.gs.onTap(p);
        return super.onTap(p, mapView);

    }

    @Override
    protected boolean onTap(int index) {
        return this.gs.onTap(index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        return this.gs.onTouchEvent(event);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event, MapView mapView) {
        return super.onTrackballEvent(event, mapView);
    }

    @Override
    protected boolean hitTest(ManagedOverlayItem managedOverlayItem, Drawable marker, int hitX, int hitY) {
        Rect bounds = marker.getBounds();

        int width = bounds.width();
        int height = bounds.height();
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();

        int touchWidth = Math.max(minTouchableWidth, width);
        int touchLeft = centerX - touchWidth / 2;
        int touchHeight = Math.max(minTouchableHeight, height);
        int touchTop = centerY - touchHeight / 2;

        touchableBounds.set(touchLeft, touchTop, touchLeft + touchWidth,
                touchTop + touchHeight);

        return touchableBounds.contains(hitX, hitY);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLazyLoadEnabled() {
        return isLazyLoadEnabled;
    }

    public LazyLoadAnimation enableLazyLoadAnimation(ImageView target) {
        return getLazyLoadManager().enableLazyLoadAnimation(target);
    }

    public Drawable getDefaultMarker() {
        return defaultMarker;
    }

    public LazyLoadAnimation getLazyLoadAnimation() {
        if (isLazyLoadEnabled()) {
            return getLazyLoadManager().getLazyLoadAnimation();
        } else {
            return null;
        }
    }

    public LazyLoadCallback getLazyLoadCallback() {
        if (isLazyLoadEnabled)
            return getLazyLoadManager().getLazyLoadCallback();
        else
            return null;
    }

    public void setLazyLoadCallback(LazyLoadCallback lazyLoadCallback) {
        if (lazyLoadCallback != null) {
            isLazyLoadEnabled = true;
            if (this.gs.getOverlayOnGestureListener() == null)
                this.gs.setOverlayOnGestureListener(new DummyListenerListener());
            getLazyLoadManager().setLazyLoadCallback(lazyLoadCallback);
        }
    }

    public void setOnOverlayGestureListener(ManagedOverlayGestureDetector.OnOverlayGestureListener listener) {
        this.gs.setOverlayOnGestureListener(listener);
    }

    public void setOnGestureListener(ManagedOverlayGestureDetector.OnGestureListener listener) {
        this.gs.setOnGestureListener(listener);
    }

    public OverlayManager getManager() {
        return manager;
    }

    public int getZoomlevel() {
        return lastZoomlevel;
    }

    public MarkerRenderer getCustomMarkerRenderer() {
        return customMarkerRenderer;
    }

    public void setCustomMarkerRenderer(MarkerRenderer customMarkerRenderer) {
        this.customMarkerRenderer = customMarkerRenderer;
    }

    public LazyLoadListener getLazyLoadListener() throws LazyLoadException {
        if (isLazyLoadEnabled)
            return getLazyLoadManager().getLazyLoadListener();
        else
            return null;
    }

    public void setLazyLoadListener(LazyLoadListener lazyLoadListener) {
        if (isLazyLoadEnabled)
            getLazyLoadManager().setLazyLoadListener(lazyLoadListener);
    }

    public List<ManagedOverlayItem> getOverlayItems() {
        return items;
    }

    public int getMinTouchableWidth() {
        return minTouchableWidth;
    }

    public void setMinTouchableWidth(int minTouchableWidth) {
        this.minTouchableWidth = minTouchableWidth;
    }

    public int getMinTouchableHeight() {
        return minTouchableHeight;
    }

    public void setMinTouchableHeight(int minTouchableHeight) {
        this.minTouchableHeight = minTouchableHeight;
    }

    public MapView getMapView() {
        return getManager().getMapView();
    }

    protected LazyLoadManager getLazyLoadManager() {
        if (lazyLoadManager == null) {
            lazyLoadManager = new LazyLoadManager(this);
        }
        return lazyLoadManager;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        super.setOnFocusChangeListener(listener);
    }

    public static abstract class OnFocusChangeListener implements ItemizedOverlay.OnFocusChangeListener {
        public abstract void onFocusChanged(ManagedOverlay overlay, ManagedOverlayItem overlayItem);

        public void onFocusChanged(com.google.android.maps.ItemizedOverlay itemizedOverlay, com.google.android.maps.OverlayItem overlayItem) {
            this.onFocusChanged((ManagedOverlay) itemizedOverlay, (ManagedOverlayItem) overlayItem);
        }
    }
    
    public void close() {
    	getLazyLoadManager().close();
    }
    
    public void start() {
    	getLazyLoadManager().start();
    }
}

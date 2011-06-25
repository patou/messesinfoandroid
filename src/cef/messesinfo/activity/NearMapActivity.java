package cef.messesinfo.activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.client.Server;
import cef.messesinfo.maps.ChurchItemizedOverlay;
import cef.messesinfo.maps.ChurchPt;
import cef.messesinfo.provider.Church;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import de.android1.FixedMyLocationOverlay;
import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.lazyload.LazyLoadCallback;
import de.android1.overlaymanager.lazyload.LazyLoadException;
import de.android1.overlaymanager.lazyload.LazyLoadListener;

public class NearMapActivity extends MapActivity {
    private static final int INITIAL_ZOOM = 15;
    private static final int MENU_MY_POSITION = 0;
    private static final int MENU_LIST = 1;
    private static final int MENU_DETAIL = 0;
    private static final int MENU_SCHEDULE = 1;
    private static final int MENU_CENTER = 2;
    private MapView mapView;
    private Location mLocation;
    private ChurchItemizedOverlay mOverlay;
    OverlayManager overlayManager;
    private MyLocationOverlay myLocationOverlay;
    private List<Map<String, String>> listChurch;

    private Server server;
    private LinearLayout panel;
    private Boolean load = true;
    protected Map<String, String> selectedItem;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
	context.startActivity(new Intent(context, NearMapActivity.class));
    }

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context, String latitude, String longitude) {
	context.startActivity(new Intent(context, NearMapActivity.class).putExtra(Church.LAT, latitude).putExtra(Church.LNG, longitude));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.near_map);
	initMap();
	server = new Server(getString(R.string.server_url));
	
	//registerForContextMenu(panel);
    }
    
    @Override
    public void onStart() {
	super.onStart();
	mOverlay.start();
	overlayManager.addOverlay(mOverlay);
	overlayManager.populate();
	if (load) {
	    if (getIntent().hasExtra(Church.LAT)) {
		centerMap(ChurchPt.createGeoPt(getIntent().getStringExtra(Church.LAT), getIntent().getStringExtra(Church.LNG)));
	    } else {
		myPosition();
	    }
	    load = false;
	}
    }

    @Override
    public void onStop() {
	super.onStop();
	mOverlay.close();
	overlayManager.removeOverlay(mOverlay);
	myLocationOverlay.disableMyLocation();
    }

    public void goHome(View v) {
	final Intent intent = new Intent(this, MessesInfo.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
    }
    
    public void onMapClick(View v) {
	myPosition();
    }
    
    public void onRefreshClick(View v) {
	mOverlay.invokeLazyLoad(0L);
    }
    
    private void setLoading(boolean loading) {
	load = loading;
	findViewById(R.id.title_refresh_progress).setVisibility(loading ? View.VISIBLE : View.GONE);
	findViewById(R.id.btn_title_refresh).setVisibility(loading ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
	int menuItemIndex = menuItem.getItemId();
	String code = selectedItem.get(Church.ID);
	switch (menuItemIndex) {
	case MENU_DETAIL:
	    ChurchActivity.activityStart(this, code);
	    break;
	case MENU_SCHEDULE:
	    ChurchActivity.activityStartSchedule(this, code);
	    break;
	case MENU_CENTER:
	    centerMap(ChurchPt.createGeoPt(selectedItem));
	    break;
	default:
	    break;
	}
	return true;
    }

    protected void initMap() {
	mapView = (MapView) findViewById(R.id.mapview);
	myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
	overlayManager = new OverlayManager(getApplication(), mapView);
	mapView.getOverlays().add(myLocationOverlay);
	mapView.setBuiltInZoomControls(true);
	mapView.getController().setZoom(INITIAL_ZOOM);
	createOverlayManager();
    }

    protected void createOverlayManager() {
        //ImageView loaderanim = (ImageView) findViewById(R.id.loader);
        mOverlay = new ChurchItemizedOverlay(overlayManager, "lazyOverlay", getResources().getDrawable(R.drawable.cross)) {
            
            protected boolean onBalloonTap(int index, ManagedOverlayItem item) {
        	selectedItem = ((ChurchPt) item).getData();
        	ChurchActivity.activityStart(NearMapActivity.this, selectedItem.get(Church.ID));
        	return true;
            }

	    @Override
	    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (this.getCurrentFocussedItem() != null) {
		    selectedItem = ((ChurchPt) getCurrentFocussedItem()).getData();
		    menu.setHeaderTitle(selectedItem.get(Church.NAME));
		    menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE, R.string.menu_context_detail);
		    menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE, R.string.menu_context_schedules);
		    menu.add(Menu.NONE, MENU_CENTER, Menu.NONE, R.string.menu_context_center);
		}
	    };
        };
        //mOverlay.enableLazyLoadAnimation(loaderanim).//setAnimationDrawable((AnimationDrawable) getResources().getDrawable(R.anim.loader));
        mOverlay.setLazyLoadCallback(new LazyLoadCallback() {
            @Override
            public List<ManagedOverlayItem> lazyload(GeoPoint topLeft, GeoPoint bottomRight, ManagedOverlay overlay) throws LazyLoadException {
        	List<ManagedOverlayItem> items = new LinkedList<ManagedOverlayItem>();
        	try {
        	    if (overlay.getZoomlevel() > 12) {
        		Double top_lat = topLeft.getLatitudeE6() / 1E6;
        		Double top_lgt = topLeft.getLongitudeE6() / 1E6;
        		Double bottom_lat = bottomRight.getLatitudeE6() / 1E6;
        		Double bottom_lgt = bottomRight.getLongitudeE6() / 1E6;
        		List<Map<String, String>> result = server.getNearLocation(top_lat, top_lgt, bottom_lat, bottom_lgt);
        		if (result != null) {
        		    listChurch = result;
        		    for (Map<String, String> item : result) {
        			items.add(new ChurchPt(item));
        		    }
        		}
        	    } else {
        		NearMapActivity.this.runOnUiThread(new Runnable() {
        		    @Override
        		    public void run() {
        			Toast.makeText(NearMapActivity.this, R.string.zoom_in_for_display, Toast.LENGTH_SHORT).show();
        		    }
        		});
        	    }
        	} catch (Exception e) {
        	    throw new LazyLoadException(e.getMessage());
        	}
        	return items;
            }
        });
        mOverlay.setLazyLoadListener(new LazyLoadListener() {
            
            @Override
            public void onSuccess(ManagedOverlay overlay) {
        	setLoading(false);
            }
            
            @Override
            public void onError(LazyLoadException exception, ManagedOverlay overlay) {
        	setLoading(false);
            }
            
            @Override
            public void onBegin(ManagedOverlay overlay) {
        	setLoading(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	mapView.getOverlays().clear();
	mapView = null;
    }

    protected void centerMap(final GeoPoint location) {
	Double lat = location.getLatitudeE6() / 1E6;
	Double lgt = location.getLongitudeE6() / 1E6;
	if (mLocation == null)
	    mLocation = new Location("gps");
	mLocation.setLatitude(lat);
	mLocation.setLongitude(lgt);
	runOnUiThread(new Runnable() {

	    @Override
	    public void run() {
		mapView.getController().animateTo(location);
		mapView.getController().setZoom(INITIAL_ZOOM);
		overlayManager.populate();
	    }
	});
    }

    @Override
    protected boolean isRouteDisplayed() {
	return false;
    }

    @Override
    protected boolean isLocationDisplayed() {
	return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	boolean supRetVal = super.onCreateOptionsMenu(menu);
	menu.add(0, MENU_MY_POSITION, 0, getString(R.string.map_menu_my_position)).setIcon(R.drawable.mylocation);
	menu.add(0, MENU_LIST, 0, getString(R.string.map_menu_list)).setIcon(R.drawable.sym_schedule);
	return supRetVal;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case MENU_MY_POSITION:
	    myPosition();
	    return true;
	case MENU_LIST:
	    NearListActivity.activityStart(this, listChurch);
	    return true;
	default:
	    break;
	}
	return false;
    }

    private void myPosition() {
	myLocationOverlay.enableMyLocation();
	myLocationOverlay.runOnFirstFix(new Runnable() {
	@Override
	public void run() {
	    centerMap(myLocationOverlay.getMyLocation());
	}
	});
    }
}

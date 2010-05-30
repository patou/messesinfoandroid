package cef.messeinfo.activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cef.messeinfo.R;
import cef.messeinfo.client.Server;
import cef.messeinfo.maps.ChurchItemizedOverlay;
import cef.messeinfo.maps.ChurchPt;
import cef.messeinfo.provider.Church;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import de.android1.FixedMyLocationOverlay;
import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.lazyload.LazyLoadCallback;
import de.android1.overlaymanager.lazyload.LazyLoadException;

public class NearMapActivity extends MapActivity {
	private static final int MENU_LIST = 0;
	private static final int MENU_MY_POSITION = 1;
	private MapView mapView;
	private Location mLocation;
	private ChurchItemizedOverlay mOverlay;
	OverlayManager overlayManager;
	private MyLocationOverlay myLocationOverlay;

	private Server server;
	private LinearLayout panel;

	/**
	 * Start the Activity
	 * 
	 * @param context
	 */
	public static void activityStart(Context context) {
		context.startActivity(new Intent(context, NearMapActivity.class));
	}

	public void displayResult(List<Map<String, String>> result) {
		if (result != null) {
			for (Map<String, String> item : result) {
				mOverlay.addChurchItem(item);
			}
			List<Overlay> overlays = mapView.getOverlays();
			overlays.add(mOverlay);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.near_map);
		setProgressBarIndeterminateVisibility(true);
		mapView = (MapView) findViewById(R.id.mapview);
		initMap();
		server = new Server(getString(R.string.server_url));
		overlayManager = new OverlayManager(getApplication(), mapView);
		panel = (LinearLayout) findViewById(R.id.church_map_info);
		panel.setVisibility(View.INVISIBLE);
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			ImageView loaderanim = (ImageView) findViewById(R.id.loader);

			mOverlay = new ChurchItemizedOverlay(overlayManager, "lazyOverlay",	getResources().getDrawable(R.drawable.cross)) {
				@Override
				protected boolean onTap(int index) {
					ChurchPt pt = (ChurchPt) getItem(index);
					Map<String, String> item = pt.getData();
					final String code = item.get("code");
					panel.setVisibility(View.VISIBLE);
					panel.setClickable(true);
					panel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							ChurchActivity.activityStart(NearMapActivity.this,
									code);
						}
					});
					TextView nom = (TextView) findViewById(R.id.nom);
					TextView commune = (TextView) findViewById(R.id.commune);
					TextView paroisse = (TextView) findViewById(R.id.paroisse);
					String cp = item.get(Church.CP);
					nom.setText(item.get(Church.NOM));
					commune.setText(cp + " " + item.get(Church.COMMUNE));
					paroisse.setText(item.get(Church.PAROISSE));
					return true;
				}
			};

			overlayManager.addOverlay(mOverlay);
			mOverlay.enableLazyLoadAnimation(loaderanim).setAnimationDrawable(
					(AnimationDrawable) getResources().getDrawable(
							R.anim.loader));
			mOverlay.setLazyLoadCallback(new LazyLoadCallback() {
				@Override
				public List<ManagedOverlayItem> lazyload(GeoPoint topLeft,
						GeoPoint bottomRight, ManagedOverlay overlay)
						throws LazyLoadException {
					List<ManagedOverlayItem> items = new LinkedList<ManagedOverlayItem>();
						try {
							if (overlay.getZoomlevel() > 12) {
								Double top_lat = topLeft.getLatitudeE6() / 1E6;
								Double top_lgt = topLeft.getLongitudeE6() / 1E6;
								Double bottom_lat = bottomRight.getLatitudeE6() / 1E6;
								Double bottom_lgt = bottomRight.getLongitudeE6() / 1E6;
								List<Map<String, String>> result = server.getNearChurch(top_lat, top_lgt,
										bottom_lat, bottom_lgt);
								if (result != null) {
									for (Map<String, String> item : result) {
										items.add(new ChurchPt(item));
									}
								}							
							}
							else {
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

			mOverlay.setOnGestureListener(new OnGestureListener() {
				
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					panel.setVisibility(View.INVISIBLE);
					return false;
				}
				
				@Override
				public void onShowPress(MotionEvent e) {
				}
				
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
						float distanceY) {
					return false;
				}
				
				@Override
				public void onLongPress(MotionEvent e) {
				}
				
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						float velocityY) {
					return false;
				}
				
				@Override
				public boolean onDown(MotionEvent e) {
					return false;
				}
			});
			overlayManager.populate();
			myLocationOverlay.enableMyLocation();
		}
		else {
			mOverlay.close();
			overlayManager.removeOverlay(mOverlay);
			myLocationOverlay.disableMyLocation();
		}
	}

	private void initMap() {
		myLocationOverlay = new FixedMyLocationOverlay(this, mapView);
		
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				centerMap(myLocationOverlay.getMyLocation());
			}
		});
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(14);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.getOverlays().clear();
		mapView = null;
	}

	protected void centerMap(GeoPoint location) {
		Double lat = location.getLatitudeE6() / 1E6;
		Double lgt = location.getLongitudeE6() / 1E6;
		if (mLocation == null)
			mLocation = new Location("gps");
		mLocation.setLatitude(lat);
		mLocation.setLongitude(lgt);
		mapView.getController().animateTo(location);
		mapView.getController().setZoom(14);
		mOverlay.invokeLazyLoad(50);
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
		return supRetVal;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MY_POSITION:
		    	centerMap(myLocationOverlay.getMyLocation());
			return true;
		default:
			break;
		}
		return false;
	}
}

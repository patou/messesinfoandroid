package cef.messeinfo.activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;
import de.android1.overlaymanager.ManagedOverlayGestureDetector.OnOverlayGestureListener;
import de.android1.overlaymanager.lazyload.LazyLoadCallback;
import de.android1.overlaymanager.lazyload.LazyLoadException;
import de.android1.overlaymanager.lazyload.LazyLoadListener;

public class NearMapActivity extends MapActivity {
	private static final int MENU_LIST = 0;
	private MapView mapView;
	private Location mLocation;
	private ChurchItemizedOverlay mOverlay;
	OverlayManager overlayManager;
	private MyLocationOverlay myLocationOverlay;
	private List<Map<String, String>> mResult = null;
	private Boolean load = false;

	private CompleteHandler mHandler = new CompleteHandler();
	private Server server;
	private LinearLayout panel;

	class CompleteHandler extends Handler {
		private static final int MSG_PROGRESS = 0;
		private static final int MSG_COMPLETE = 1;
		private static final int MSG_FAILURE = 2;

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_PROGRESS:
				load = msg.arg1 != 0;
				setProgressBarIndeterminateVisibility(msg.arg1 != 0);
				break;

			case MSG_COMPLETE:
				mResult = (List<Map<String, String>>) msg.obj;
				displayResult(mResult);
				// Toast.makeText(NextMassActivity.this,
				// getString(R.string.location_not_found),
				// Toast.LENGTH_SHORT).show();
				break;
			case MSG_FAILURE:
				// Toast.makeText(NextMassActivity.this,
				// getString(R.string.location_not_found),
				// Toast.LENGTH_SHORT).show();
				break;
			default:
				super.handleMessage(msg);
			}
		}

		public void progress(boolean progress) {
			android.os.Message msg = new android.os.Message();
			msg.what = MSG_PROGRESS;
			msg.arg1 = progress ? 1 : 0;
			sendMessage(msg);
		}

		public void complete(List<Map<String, String>> result) {
			android.os.Message msg = new android.os.Message();
			msg.what = MSG_COMPLETE;
			msg.obj = result;
			sendMessage(msg);
		}

		public void failure(boolean progress) {
			android.os.Message msg = new android.os.Message();
			msg.what = MSG_FAILURE;
			msg.arg1 = progress ? 1 : 0;
			sendMessage(msg);
		}
	}

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
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

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
					nom.setText(item.get(Church.NOM));
					commune.setText(item.get(Church.COMMUNE));
					paroisse.setText(item.get(Church.PAROISSE));
					return true;
				}
			};

			overlayManager.addOverlay(mOverlay);
			// default built-in animation
			mOverlay.enableLazyLoadAnimation(loaderanim).setAnimationDrawable(
					(AnimationDrawable) getResources().getDrawable(
							R.anim.loader));
			// custom animation
			// managedOverlay.enableLazyLoadAnimation(loaderanim).setAnimationDrawable((AnimationDrawable)getResources().getDrawable(R.anim.myanim));

			mOverlay.setLazyLoadCallback(new LazyLoadCallback() {
				@Override
				public List<ManagedOverlayItem> lazyload(GeoPoint topLeft,
						GeoPoint bottomRight, ManagedOverlay overlay)
						throws LazyLoadException {
					List<ManagedOverlayItem> items = new LinkedList<ManagedOverlayItem>();
					/*if (overlay != null && overlay.getZoomlevel() <= 10) {
						Toast.makeText(
								NearMapActivity.this.getApplicationContext(),
								"Veillez zoomer pour afficher les églises",
								Toast.LENGTH_SHORT).show();
					} else {*/
						try {
							// List<GeoPoint> marker =
							// GeoHelper.findMarker(topLeft,
							// bottomRight, overlay.getZoomlevel());
							// for (int i = 0; i < 10; i++) {
							// int j = bottomRight.getLatitudeE6() -
							// topLeft.getLatitudeE6();
							// float d = j * (float)Math.random();
							// int lat = topLeft.getLatitudeE6() +
							// Math.round(d);
							// int k = bottomRight.getLongitudeE6() -
							// topLeft.getLongitudeE6();
							// float e = k * (float)Math.random();
							// int lgt = topLeft.getLongitudeE6() +
							// Math.round(e);
							// GeoPoint point = new GeoPoint(lat, lgt);
							// ManagedOverlayItem item = new
							// ManagedOverlayItem(point, "Item" + i, "");
							// items.add(item);
							// }

							Double top_lat = topLeft.getLatitudeE6() / 1E6;
							Double top_lgt = topLeft.getLongitudeE6() / 1E6;
							Double bottom_lat = bottomRight.getLatitudeE6() / 1E6;
							Double bottom_lgt = bottomRight.getLongitudeE6() / 1E6;
							Log.e("top", top_lat.toString() + ":"
									+ top_lgt.toString());
							Log.e("bottom", bottom_lat.toString() + ":"
									+ bottom_lgt.toString());
							List<Map<String, String>> result = server
									.getNearChurch(top_lat, top_lgt,
											bottom_lat, bottom_lgt);
							if (result != null) {
								for (Map<String, String> item : result) {
									// mOverlay.addChurchItem(item);
									items.add(new ChurchPt(item));
								}
							}
							// lets simulate a latency

						} catch (Exception e) {
							throw new LazyLoadException(e.getMessage());
						}
					//}
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
			myLocationOverlay.enableCompass();
			myLocationOverlay.enableMyLocation();
		}
		else {
			overlayManager.removeOverlay(mOverlay);
			myLocationOverlay.disableMyLocation();
			myLocationOverlay.disableCompass();
		}
	}

	private void initMap() {
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				centerMap(myLocationOverlay.getMyLocation());
			}
		});
		mapView.setBuiltInZoomControls(true);
		// mOverlay = new
		// ChurchItemizedOverlay(this.getResources().getDrawable(R.drawable.cross)){
		// @Override
		// protected boolean onTap(int index) {
		// Map<String,String> item = getChurchItem(index);
		// final String code = item.get("code");
		// LinearLayout panel = (LinearLayout)
		// findViewById(R.id.church_map_info);
		// panel.setClickable(true);
		// panel.setOnClickListener(new OnClickListener() {
		//                    
		// @Override
		// public void onClick(View v) {
		// ChurchActivity.activityStart(NearMapActivity.this, code);
		// }
		// });
		// TextView nom = (TextView) findViewById(R.id.nom);
		// TextView commune = (TextView) findViewById(R.id.commune);
		// TextView paroisse = (TextView) findViewById(R.id.paroisse);
		// nom.setText(item.get(Church.NOM));
		// commune.setText(item.get(Church.COMMUNE));
		// paroisse.setText(item.get(Church.PAROISSE));
		// return true;
		// }
		// };
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
		// startSearchNextMass();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}

	/*
	 * protected Location getLocation() { LocationManager loc =
	 * (LocationManager) getSystemService(LOCATION_SERVICE); String provider =
	 * loc.getBestProvider(new Criteria(), true); Location location =
	 * loc.getLastKnownLocation(provider); if (location != null) {
	 * loc.requestLocationUpdates(provider, 1000l, 10l, new LocationListener() {
	 * public void onLocationChanged(Location loc) { mLocation = loc;
	 * centerMap(loc); startSearchNextMass(); String lat =
	 * String.valueOf(loc.getLatitude()); String lon =
	 * String.valueOf(loc.getLongitude()); Log.e("GPS", "location changed: lat="
	 * + lat + ", lon=" + lon); }
	 * 
	 * public void onProviderDisabled(String arg0) { Log.e("GPS",
	 * "provider disabled " + arg0); }
	 * 
	 * public void onProviderEnabled(String arg0) { Log.e("GPS",
	 * "provider enabled " + arg0); }
	 * 
	 * public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	 * Log.e("GPS", "status changed to " + arg0 + "-" + arg1 + "-"); } }); }
	 * return location; }
	 */

	protected void startSearchNextMass() {
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// mHandler.progress(true);
		// Server server = new Server(getString(R.string.server_url));
		// GeoPoint mapCenter = mapView.getMapCenter();
		// Double lat = mapCenter.getLatitudeE6() / 1E6;
		// Double lgt = mapCenter.getLongitudeE6() / 1E6;
		// List<Map<String, String>> result = server.getNearChurch(lat, lgt);
		// mHandler.complete(result);
		// mHandler.progress(false);
		// }
		//
		// }).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean supRetVal = super.onCreateOptionsMenu(menu);
		if (mResult != null)
			menu.add(0, MENU_LIST, 0, getString(R.string.menu_list)); // .setIcon(R.drawable.contact)
		return supRetVal;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LIST:
			NearListActivity.activityStart(this, mResult);
			return true;
		default:
			break;
		}
		return false;
	}
}

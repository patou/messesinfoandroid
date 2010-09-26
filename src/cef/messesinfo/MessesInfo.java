package cef.messesinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import cef.messesinfo.activity.AboutActivity;
import cef.messesinfo.activity.FavoriteActivity;
import cef.messesinfo.activity.NearMapActivity;
import cef.messesinfo.activity.SearchChurchActivity;
import cef.messesinfo.activity.SearchMassActivity;
import cef.messesinfo.client.Server;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class MessesInfo extends ListActivity {
    /** Called when the activity is first created. */

    /** Attribute key for the list item text. */
    private static final String LABEL = "LABEL";
    /** Attribute key for the list item icon's drawable resource. */
    private static final String ICON = "ICON";

    public static final int MENULIST_NEXT_MASS = 0;
    public static final int MENULIST_SEARCH_MASS = 1;
    public static final int MENULIST_FAVORITE = 2;
    public static final int MENULIST_CHURCH_BOOK = 3;
    public static final int MENULIST_QUIT = 4;
    public static final int MENULIST_LECTIO = 5;

    public static final int MENU_CONTACT = 1;
    public static final int MENU_WEBSITE = 2;
    public static final int MENU_ABOUT = 3;
    public static final int MENU_SHARE = 4;
    public static final String AUTHORITY = "cef.messesinfo";

    static GoogleAnalyticsTracker tracker = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if (tracker == null) {
	    tracker = GoogleAnalyticsTracker.getInstance();
	    tracker.start("UA-12886932-4", 20, this);
	    trackUserInformation();
	}

	// Use an existing ListAdapter that will map an array
	// of strings to TextViews
	List<Map<String, Object>> menulist = buildList();
	SimpleAdapter adapter = new SimpleAdapter(
	// the Context
		this,
		// the data to display
		menulist,
		// The layout to use for each item
		R.layout.main_menu_item,
		// The list item attributes to display
		new String[] { LABEL, ICON },
		// And the ids of the views where they should be displayed (same
		// order)
		new int[] { android.R.id.text1, android.R.id.icon });

	setListAdapter(adapter);
	setContentView(R.layout.main);
    }

    private void trackUserInformation() {

	// Version
	new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {
		    PackageManager pm = getPackageManager();
		    PackageInfo pi;
		    Server server = new Server(getString(R.string.server_url));
		    pi = pm.getPackageInfo(getPackageName(), 0);
		    tracker.trackPageView("/home");
		    tracker.trackEvent("Application", "Version", pi.versionName, pi.versionCode);
		    tracker.trackEvent("Android", "Device", Build.DEVICE, 1);
		    tracker.trackEvent("Android", "Model", Build.MODEL, 1);
		    tracker.trackEvent("Android", "Brand", Build.BRAND, 1);
		    tracker.trackEvent("Android", "Product", Build.PRODUCT, 1);
		    tracker.trackEvent("Android", "Display", Build.DISPLAY, 1);
		    tracker.trackEvent("Android", "Board", Build.BOARD, 1);
		    tracker.trackEvent("Android", "Version", Build.VERSION.RELEASE, 1);
		    HashMap<String, String> map = new HashMap<String, String>();
		    map.put("version", Integer.toString(pi.versionCode));
		    map.put("versionName", pi.versionName);
		    map.put("device", Build.DEVICE);
		    map.put("model", Build.MODEL);
		    map.put("brand", Build.BRAND);
		    map.put("product", Build.PRODUCT);
		    map.put("display", Build.DISPLAY);
		    map.put("board", Build.BOARD);
		    map.put("androidVersion", Build.VERSION.RELEASE);
		    Map<String, String> result;
		    result = server.getMessage(map);
		    if (result != null && result.containsKey("message")) {
			String message = result.get("message");
			showMessage(message);
		    }
		} catch (XMLRPCException e) {
		    e.printStackTrace();
		    showMessage(getString(R.string.error_server));
		} catch (NameNotFoundException e) {
		    e.printStackTrace();
		}
	    }

	}).start();
    }

    private void showMessage(final String message) {
	runOnUiThread(new Runnable() {
	    @Override
	    public void run() {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	    }
	});
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	switch (position) {
	case MENULIST_NEXT_MASS:
	    NearMapActivity.activityStart(MessesInfo.this);
	    tracker.trackPageView("/near_map");
	    break;
	case MENULIST_SEARCH_MASS:
	    SearchMassActivity.activityStart(MessesInfo.this);
	    tracker.trackPageView("/search_mass");
	    break;
	case MENULIST_FAVORITE:
	    FavoriteActivity.activityStart(MessesInfo.this);
	    tracker.trackPageView("/favorite");
	    break;
	case MENULIST_CHURCH_BOOK:
	    SearchChurchActivity.activityStart(MessesInfo.this);
	    tracker.trackPageView("/church_book");
	    break;
	case MENULIST_QUIT:
	    finish();
	    break;
	case MENULIST_LECTIO:
	    try {
		Intent intent = getPackageManager().getLaunchIntentForPackage(getString(R.string.lectio_package));
		startActivity(intent);
		tracker.trackPageView("/lectio");
	    } catch (Exception e) {
		e.printStackTrace();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse("market://details?id=" + getString(R.string.lectio_package));
		intent.setData(uri);
		startActivity(intent);
	    }
	default:
	    break;
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	boolean supRetVal = super.onCreateOptionsMenu(menu);

	menu.add(0, MENU_CONTACT, 0, getString(R.string.main_menu_contact)).setIcon(R.drawable.contact);
	menu.add(0, MENU_WEBSITE, 0, getString(R.string.main_menu_website)).setIcon(R.drawable.web);
	menu.add(0, MENU_ABOUT, 0, getString(R.string.main_menu_about)).setIcon(R.drawable.icon_mini);
	menu.add(0, MENU_SHARE, 0, getString(R.string.main_menu_share)).setIcon(android.R.drawable.ic_menu_share);
	return supRetVal;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	switch (item.getItemId()) {
	case MENU_CONTACT:
	    // Uri uri = Uri.parse("mailto://contact@messesinfo.cef.fr");
	    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent.setType("plain/text");
	    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { getString(R.string.messesinfo_email) });
	    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.messesinfo_email_subject));
	    // emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
	    // "myBodyText");
	    startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_messesinfo)));
	    // startActivity(new Intent(Intent.ACTION_VIEW, uri));
	    return true;
	case MENU_WEBSITE:
	    final Intent urlIntent = new Intent(android.content.Intent.ACTION_VIEW);
	    urlIntent.setData(Uri.parse(getString(R.string.messeinfo_url)));
	    startActivity(urlIntent);
	    return true;
	case MENU_ABOUT:
	    AboutActivity.activityStart(MessesInfo.this);
	    tracker.trackPageView("/about");
	    return true;
	case MENU_SHARE:
	    String text = getString(R.string.messesinfo_share);
	    Intent i = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT,
		    getString(R.string.messesinfo_share_subject))
	    ;
	    startActivityForResult(Intent.createChooser(i, getString(R.string.share_with)), 0);
	default:
	    break;
	}
	return false;
    }

    private List<Map<String, Object>> buildList() {
	// Resulting list...
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(5);

	Map<String, Object> map1 = new HashMap<String, Object>();
	map1.put(LABEL, getString(R.string.menu_next_mass));
	map1.put(ICON, R.drawable.church2);
	list.add(map1);
	Map<String, Object> map2 = new HashMap<String, Object>();
	map2.put(LABEL, getString(R.string.menu_search_mass));
	map2.put(ICON, R.drawable.bible);
	list.add(map2);
	Map<String, Object> map3 = new HashMap<String, Object>();
	map3.put(LABEL, getString(R.string.menu_favorite));
	map3.put(ICON, R.drawable.favorites);
	list.add(map3);
	Map<String, Object> map4 = new HashMap<String, Object>();
	map4.put(LABEL, getString(R.string.menu_church_book));
	map4.put(ICON, R.drawable.church1);
	list.add(map4);
	Map<String, Object> map5 = new HashMap<String, Object>();
	map5.put(LABEL, getString(R.string.menu_quit));
	map5.put(ICON, R.drawable.cross);
	list.add(map5);
	Map<String, Object> map6 = new HashMap<String, Object>();
	map6.put(LABEL, getString(R.string.menu_lectio));
	map6.put(ICON, R.drawable.lectio);
	list.add(map6);
	return list;
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	// Stop the tracker when it is no longer needed.
	if (tracker != null)
	    tracker.stop();
    }

    public static GoogleAnalyticsTracker getTracker() {
	return tracker;
    }
}
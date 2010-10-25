package cef.messesinfo.activity;

import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.client.Server;
import cef.messesinfo.maps.MyLocation;
import cef.messesinfo.provider.Church;

public class SearchChurchActivity extends ListActivity {
    private static final int MENU_DETAIL = 0;
    private static final int MENU_SCHEDULE = 1;
    private static final int MENU_CENTER = 2;
    private static final int MENU_NEAR = 3;
    List<Map<String, String>> list = null;
    MyLocation myLocation = new MyLocation();
    private ChurchAdapter mAdapter;
    private EditText searchText;
    private TextView empty;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
	context.startActivity(new Intent(context, SearchChurchActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.list);
	mAdapter = new ChurchAdapter(this);

	setListAdapter(mAdapter);

	ImageButton button = (ImageButton) findViewById(R.id.searchButton);
	ImageButton nearButton = (ImageButton) findViewById(R.id.nearButton);
	empty = (TextView) findViewById(android.R.id.empty);
	empty.setMovementMethod(ScrollingMovementMethod.getInstance());
	empty.setText(getString(R.string.list_search_church_help));
	searchText = (EditText) findViewById(R.id.searchField);
	searchText.setImeOptions(DEFAULT_KEYS_SEARCH_LOCAL);
	button.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		search(searchText.getText().toString());
	    }
	});
	nearButton.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		empty.setText(getString(R.string.localisation_in_progress));
		myLocation.getLocation(SearchChurchActivity.this, new MyLocation.LocationResult() {
		    
		    @Override
		    public void gotLocation(Location location) {
			String search = searchText.getText().toString() + "> " + location.getLatitude() + ":" + location.getLongitude(); 
			search(search);
		    }
		});
	    }
	});
	searchText.setOnEditorActionListener(new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		search(searchText.getText().toString());
		return true;
	    }
	});
	registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	Map<String, String> item = list.get(position);
	String code = item.get("code");
	ChurchActivity.activityStart(this, code);
    }

    private void search(final String search) {
	empty.setText(getString(R.string.list_search_loading));
	mAdapter.setList(null);
	new Thread(new Runnable() {

	    @Override
	    public void run() {
		MessesInfo.getTracker().trackEvent("Application", "SearchChurch", search, 1);
		try {
		    list = new Server(getString(R.string.server_url)).searchChurch(search, 0);
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    if (list != null && list.size() > 0) {
				mAdapter.setList(list);
			    } else {
				empty.setText(getString(R.string.list_empty));
			    }
			}
		    });
		} catch (XMLRPCException e) {
		    e.printStackTrace();
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    empty.setText(R.string.error_church_book);
			}
		    });
		}

	    }
	}).start();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	if (v.getId() == android.R.id.list) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	    Map<String, String> item = list.get(info.position);
	    menu.setHeaderTitle(item.get(Church.NOM));
	    menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE, R.string.menu_context_detail);
	    menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE, R.string.menu_context_schedules);
	    menu.add(Menu.NONE, MENU_CENTER, Menu.NONE, R.string.menu_context_center);
	    menu.add(Menu.NONE, MENU_NEAR, Menu.NONE, R.string.menu_context_near);
	}
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
	int menuItemIndex = menuItem.getItemId();
	Map<String, String> item = list.get(info.position);
	String code = item.get(Church.CODE);
	switch (menuItemIndex) {
	case MENU_DETAIL:
	    ChurchActivity.activityStart(this, code);
	    break;
	case MENU_SCHEDULE:
	    ChurchActivity.activityStartSchedule(this, code);
	    break;
	case MENU_CENTER:
	    NearMapActivity.activityStart(this, item.get(Church.LAT), item.get(Church.LON));
	    break;
	case MENU_NEAR:
	    searchText.setText("> " + item.get(Church.CP));
	    search("> " + item.get(Church.LAT) + ":" + item.get(Church.LON));
	    break;
	default:
	    break;
	}

	return true;
    }
}

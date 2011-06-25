package cef.messesinfo.activity;

import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.client.Server;
import cef.messesinfo.maps.MyLocation;
import cef.messesinfo.provider.Church;

public class SearchChurchActivity extends ListActivity implements OnScrollListener {
    private static final int MENU_DETAIL = 0;
    private static final int MENU_SCHEDULE = 1;
    private static final int MENU_CENTER = 2;
    private static final int MENU_NEAR = 3;
    private static final int MENU_SCHEDULE_NEAR = 4;
    List<Map<String, String>> list = null;
    MyLocation myLocation = new MyLocation();
    private ChurchAdapter mAdapter;
    private EditText searchText;
    private TextView empty;
    private String search;
    private boolean isLoading = false;
    private boolean isSearchEnd = false;
    private int start = 0;
    private TextView loadMoreView;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context, String search) {
	Intent intent = new Intent(context, SearchChurchActivity.class);
	intent.putExtra("search", search);
	context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.list);
	empty = (TextView) findViewById(android.R.id.empty);
	loadMoreView = new TextView(this);
	loadMoreView.setText("Suite ...");
	loadMoreView.setWidth(LayoutParams.FILL_PARENT);
	loadMoreView.setHeight(50);
	loadMoreView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
	loadMoreView.setGravity(Gravity.CENTER);
	loadMoreView.setOnClickListener(new OnClickListener() {
	    
	    @Override
	    public void onClick(View v) {
		loadMore();
	    }
	});
	getListView().addFooterView(loadMoreView);
	mAdapter = new ChurchAdapter(this);
	setListAdapter(mAdapter);

	registerForContextMenu(getListView());
	getListView().setOnScrollListener(this);
	final RetainNonConfigurationValue data = (RetainNonConfigurationValue) getLastNonConfigurationInstance();
	if (data != null) {
	    mAdapter.setList(data.list);
	    search = data.search;
	    searchText.setText(data.search);
	} else {
	    mAdapter.setList(null);
	}
	search = getIntent().getStringExtra("search");
	if (search != null)
	    search(search);
    }

    public void goHome(View v) {
	final Intent intent = new Intent(this, MessesInfo.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
	RetainNonConfigurationValue value = new RetainNonConfigurationValue();
	value.list = mAdapter.getList();
	value.search = search;
	return value;
    }
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	boolean loadMore = /* maybe add a padding */
	firstVisibleItem + visibleItemCount >= totalItemCount - 1;
	Log.d("MESSESINFO onScroll:", "f=" + firstVisibleItem + ", vc=" + visibleItemCount + ", tc=" + totalItemCount);
	if (loadMore) {
	    Log.d("MESSESINFO onScroll:", "loadMore");
	    loadMore();
	}
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	if (position < mAdapter.getCount()) {
        	Map<String, String> item = (Map<String, String>) mAdapter.getItem(position);
        	String code = item.get(Church.ID);
        	ChurchActivity.activityStart(this, code);
	}
    }

    private void search(final String search) {
	search(search, false);
    }

    
    public void onRefreshClick(View v) {
	search(search);
    }
    
    private void setLoading(boolean loading) {
	isLoading = loading;
	findViewById(R.id.title_refresh_progress).setVisibility(loading ? View.VISIBLE : View.GONE);
	findViewById(R.id.btn_title_refresh).setVisibility(loading ? View.GONE : View.VISIBLE);
    }
    
    private void loadMore() {
	if (search != null && !isLoading && !isSearchEnd) {
	    Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
	    search(this.search, true);
	}
    }

    private void search(final String search, final boolean loadMore) {
	this.search = search;
	setLoading(true);
	empty.setText(getString(R.string.list_search_loading));
	if (!loadMore) {
	    mAdapter.setList(null);
	    isSearchEnd = false;
	}
	loadMoreView.setText("Chargement ...");
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		MessesInfo.getTracker(SearchChurchActivity.this).trackEvent("Application", "SearchChurch", search, 1);
		try {
		    int pageSize = 10;
		    if (!loadMore) {
			start = 0;
		    }
		    list = new Server(getString(R.string.server_url)).searchLocation(search, start, pageSize);
		    if (list != null) {
			start += list.size();
			isSearchEnd = list.size() < pageSize;
		    }
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    if (list != null && list.size() > 1) {

				if (loadMore) {
				    mAdapter.appendList(list);
				    loadMoreView.setText(list.size() == 0 ? "" : "Suite ...");
				} else {
				    mAdapter.setList(list);
				}
			    } else {
				empty.setText(getString(R.string.list_empty));
			    }
			    setLoading(false);
			}

			
		    });
		} catch (XMLRPCException e) {
		    e.printStackTrace();
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    empty.setText(R.string.error_church_book);
			    setLoading(false);
			}
		    });
		}

	    }
	}).start();
	
    }
 

  
    
//    private void showSelectionDepartementDialog() {
//	    new AlertDialog.Builder( this )
//	       .setTitle( "Départements" )
//	       .setItems(, , new DialogSelectionClickHandler() )
//	       .setPositiveButton( "OK", new DialogButtonClickHandler() )
//	       .create();
//    }
    


    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	if (v.getId() == android.R.id.list) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	    Map<String, String> item = (Map<String, String>) mAdapter.getItem(info.position);
	    menu.setHeaderTitle(item.get(Church.NAME));
	    menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE, R.string.menu_context_detail);
	    menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE, R.string.menu_context_schedules);
	    menu.add(Menu.NONE, MENU_CENTER, Menu.NONE, R.string.menu_context_center);
	    menu.add(Menu.NONE, MENU_NEAR, Menu.NONE, R.string.menu_context_near);
	    menu.add(Menu.NONE, MENU_SCHEDULE_NEAR, Menu.NONE, R.string.menu_context_schedule_near);
	}
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
	int menuItemIndex = menuItem.getItemId();
	Map<String, String> item = (Map<String, String>) mAdapter.getItem(info.position);
	String code = item.get(Church.ID);
	switch (menuItemIndex) {
	case MENU_DETAIL:
	    ChurchActivity.activityStart(this, code);
	    break;
	case MENU_SCHEDULE:
	    ChurchActivity.activityStartSchedule(this, code);
	    break;
	case MENU_CENTER:
	    NearMapActivity.activityStart(this, item.get(Church.LAT), item.get(Church.LNG));
	    break;
	case MENU_NEAR:
	    searchText.setText("> " + item.get(Church.ZIPCODE));
	    search("> " + item.get(Church.LAT) + ":" + item.get(Church.LNG));
	    break;
	case MENU_SCHEDULE_NEAR:
	    SearchScheduleActivity.activityStart(this, item.get(Church.LAT) + ":" + item.get(Church.LNG));
	default:
	    break;
	}

	return true;
    }
    
    class RetainNonConfigurationValue {
	String search;
	List<Map<String, String>> list;
    }
}

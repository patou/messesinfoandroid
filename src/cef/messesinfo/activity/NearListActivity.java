package cef.messesinfo.activity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import cef.messesinfo.R;
import cef.messesinfo.provider.Church;

public class NearListActivity extends ListActivity {
    private static final int MENU_DETAIL = 0;
    private static final int MENU_SCHEDULE = 1;
    private static final int MENU_CENTER = 2;
    private static final int MENU_MAPS = 0;
    List<Map<String, String>> list = null;
    private ChurchAdapter mAdapter;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context, List<Map<String, String>> list) {
	Intent intent = new Intent(context, NearListActivity.class);
	intent.putExtra("list", (Serializable) list);
	context.startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.list_favorite);
	mAdapter = new ChurchAdapter(this);
	list = (List<Map<String, String>>) getIntent().getSerializableExtra("list");
	mAdapter.setList(list);
	setListAdapter(mAdapter);
	registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	if (v.getId() == android.R.id.list) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	    Map<String, String> item = list.get(info.position);
	    menu.setHeaderTitle(item.get(Church.NAME));
	    menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE, R.string.menu_context_detail);
	    menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE, R.string.menu_context_schedules);
	    menu.add(Menu.NONE, MENU_CENTER, Menu.NONE, R.string.menu_context_center);
	}
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
	int menuItemIndex = menuItem.getItemId();
	Map<String, String> item = list.get(info.position);
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
	default:
	    break;
	}

	return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	Map<String, String> item = list.get(position);
	String code = item.get(Church.ID);
	ChurchActivity.activityStart(this, code);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, MENU_MAPS, 0, getString(R.string.menu_maps)).setIcon(android.R.drawable.ic_menu_mapmode);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case MENU_MAPS:
	    finish();
	    return true;
	default:
	    break;
	}
	return false;
    }
}

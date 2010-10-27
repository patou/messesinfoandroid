package cef.messesinfo.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import cef.messesinfo.R;
import cef.messesinfo.provider.Church;

public class FavoriteActivity extends ListActivity {
    private static final int MENU_DETAIL = 0;
    private static final int MENU_SCHEDULE = 1;
    private static final int MENU_CENTER = 2;
    private static final int MENU_REMOVE_FAVORY = 3;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
	context.startActivity(new Intent(context, FavoriteActivity.class));
    }

    private static final String[] PROJECTION = new String[] { Church._ID, // 0
	    Church.ID, // 1
	    Church.NAME, // 2
	    Church.ZIPCODE, // 3
	    Church.CITY, // 4
	    Church.COMMUNITY, // 5
	    Church.LAT, //6
	    Church.LNG //7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Intent intent = getIntent();
	if (intent.getData() == null) {
	    intent.setData(Church.CONTENT_URI);
	}
	setContentView(R.layout.list_favorite);
	Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null, null);
	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_church_item, cursor, new String[] { Church.NAME, // 2
		Church.CITY, // 3
		Church.COMMUNITY }, new int[] { R.id.nom, R.id.commune, R.id.paroisse });
	setListAdapter(adapter);
	registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	if (v.getId() == android.R.id.list) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	    SimpleCursorAdapter adp = (SimpleCursorAdapter) getListAdapter();
	    Cursor cursor = adp.getCursor();
	    cursor.moveToPosition(info.position);
	    menu.setHeaderTitle(cursor.getString(2));
	    menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE, R.string.menu_context_detail);
	    menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE, R.string.menu_context_schedules);
	    menu.add(Menu.NONE, MENU_CENTER, Menu.NONE, R.string.menu_context_center);
	    menu.add(Menu.NONE, MENU_REMOVE_FAVORY, Menu.NONE, R.string.menu_remove_favory);
	}
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
	int menuItemIndex = menuItem.getItemId();
	SimpleCursorAdapter adp = (SimpleCursorAdapter) getListAdapter();
	Cursor cursor = adp.getCursor();
	cursor.moveToPosition(info.position);
	String code = cursor.getString(1);
	switch (menuItemIndex) {
	case MENU_DETAIL:
	    ChurchActivity.activityStart(this, code);
	    break;
	case MENU_SCHEDULE:
	    ChurchActivity.activityStartSchedule(this, code);
	    break;
	case MENU_CENTER:
	    NearMapActivity.activityStart(this, cursor.getString(6), cursor.getString(7));
	    break;
	case MENU_REMOVE_FAVORY:
	    getContentResolver().delete(Uri.withAppendedPath(Church.CONTENT_URI, code), null, null);
	    ((SimpleCursorAdapter) getListAdapter()).notifyDataSetChanged();
	default:
	    break;
	}

	return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	// Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
	SimpleCursorAdapter adp = (SimpleCursorAdapter) l.getAdapter();
	Cursor cursor = adp.getCursor();
	cursor.moveToPosition(position);
	String code = cursor.getString(1);
	ChurchActivity.activityStartSchedule(this, code);
    }
}

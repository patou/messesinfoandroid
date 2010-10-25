package cef.messesinfo.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView.OnEditorActionListener;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.client.Server;
import cef.messesinfo.maps.MyLocation;
import cef.messesinfo.provider.Church;
import cef.messesinfo.provider.Schedule;

public class SearchMassActivity extends ExpandableListActivity {
    private static final int MENU_DETAIL = 0;
    private static final int MENU_SCHEDULE = 1;
    private static final int MENU_CENTER = 2;
    private static final int MENU_NEAR = 3;
    private static final int MENU_EVENT = 4;
    private static final int MENU_SHARE = 5;

    private Server server;
    private TextView empty;
    private EditText searchText;
    private View button;
    private View nearButton;
    private ScheduleExpandableListAdapter scheduleExpandableListAdapter;
    MyLocation myLocation = new MyLocation();

    public SearchMassActivity() {

    }

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
	context.startActivity(new Intent(context, SearchMassActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.search_expandable_list);
	scheduleExpandableListAdapter = new ScheduleExpandableListAdapter(SearchMassActivity.this);
	scheduleExpandableListAdapter.setList(null);
	button = (ImageButton) findViewById(R.id.searchButton);
	nearButton = (ImageButton) findViewById(R.id.nearButton);
	empty = (TextView) findViewById(android.R.id.empty);
	empty.setMovementMethod(ScrollingMovementMethod.getInstance());
	empty.setText(getString(R.string.list_search_mass_help));
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
		myLocation.getLocation(SearchMassActivity.this, new MyLocation.LocationResult() {
		    
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
	registerForContextMenu(getExpandableListView());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
	Map<String, Object> child = (Map<String, Object>) scheduleExpandableListAdapter.getChild(groupPosition, childPosition);
	if (child != null) {
	    String code = (String) child.get(Church.CODE);
	    if (code != null)
		ChurchActivity.activityStartSchedule(SearchMassActivity.this, code);
	}
	return true;
    }

    private void search(final String search) {
	scheduleExpandableListAdapter.setList(null);
	empty.setText(getString(R.string.list_search_loading));
	new Thread(new Runnable() {

	    @Override
	    public void run() {
		server = new Server(getString(R.string.server_url));
		MessesInfo.getTracker().trackEvent("Application", "SearchMass", search, 1);
		try {
		    final List<Map<String, Object>> result = server.searchSchedule(search);
		    if (result != null) {
			runOnUiThread(new Runnable() {
			    @Override
			    public void run() {
				if (result != null && result.size() > 0) {
				    scheduleExpandableListAdapter.setList(result);
				} else {
				    scheduleExpandableListAdapter.setList(null);
				    empty.setText(getString(R.string.schedules_empty));
				}
				setListAdapter(scheduleExpandableListAdapter);
			    }
			});
		    }
		} catch (XMLRPCException e) {
		    e.printStackTrace();
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    empty.setText(R.string.error_church_schedule);
			}
		    });
		}
	    }
	}).start();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	if (v.getId() == android.R.id.list) {
	    ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
	    int typeId = ExpandableListView.getPackedPositionType(info.packedPosition);
	    if (typeId == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
		int groupId = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childId = ExpandableListView.getPackedPositionChild(info.packedPosition);
		Map<String, String> item = (Map<String, String>) scheduleExpandableListAdapter.getChild(groupId, childId);
		if (item != null) {
		    menu.setHeaderTitle(item.get(Church.NOM));
		    menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE, R.string.menu_context_detail);
		    menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE, R.string.menu_context_schedules);
		    if (item.containsKey(Church.LAT) && item.containsKey(Church.LON)) {
			menu.add(Menu.NONE, MENU_CENTER, Menu.NONE, R.string.menu_context_center);
			menu.add(Menu.NONE, MENU_NEAR, Menu.NONE, R.string.menu_context_near);
		    }
		    menu.add(Menu.NONE, MENU_EVENT, Menu.NONE, R.string.menu_context_event);
		    menu.add(Menu.NONE, MENU_SHARE, Menu.NONE, R.string.menu_context_event_share);
		}
	    }
	}
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();
	int typeId = ExpandableListView.getPackedPositionType(info.packedPosition);
	if (typeId == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	    int groupId = ExpandableListView.getPackedPositionGroup(info.packedPosition);
	    int childId = ExpandableListView.getPackedPositionChild(info.packedPosition);
	    Map<String, String> item = (Map<String, String>) scheduleExpandableListAdapter.getChild(groupId, childId);
	    Map<String, Object> itemGroup = (Map<String, Object>) scheduleExpandableListAdapter.getGroup(groupId);
	    String code = item.get(Church.CODE);
	    switch (menuItem.getItemId()) {
	    case MENU_DETAIL:
		ChurchActivity.activityStart(this, code);
		break;
	    case MENU_SCHEDULE:
		ChurchActivity.activityStartSchedule(this, code);
		break;
	    case MENU_CENTER:
		if (item.containsKey(Church.LAT) && item.containsKey(Church.LON)) {
		    NearMapActivity.activityStart(this, item.get(Church.LAT), item.get(Church.LON));
		}
		break;
	    case MENU_NEAR:
		if (item.containsKey(Church.LAT) && item.containsKey(Church.LON)) {
		    searchText.setText("> " + item.get(Church.CODE));
		    search("> " + item.get(Church.LAT) + ":" + item.get(Church.LON));
		}
		break;
	    case MENU_EVENT:
		try {
		    Intent intent = new Intent(Intent.ACTION_EDIT);
		    intent.setType("vnd.android.cursor.item/event");
		    intent.putExtra("title", item.get(Church.NOM));
		    intent.putExtra("description", item.get(Church.PAROISSE) + "\n" + item.get(Church.COMMUNE));
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH'h'mm", Locale.FRANCE);
		    Date d = sdf.parse(item.get(Schedule.DATE) + " " + item.get(Schedule.HEURE));
		    intent.putExtra("beginTime", d.getTime());
		    d.setHours(d.getHours() + 1);
		    intent.putExtra("endTime", d.getTime());
		    startActivity(intent);
		} catch (ParseException e) {
		    e.printStackTrace();
		}
		break;
	    case MENU_SHARE:
		Intent i = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT,
			item.get(Schedule.HEURE) + "\n" + item.get(Schedule.DATE) + "\n" + "\n" + item.get(Church.COMMUNE)).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT,
			item.get(Church.NOM));
		startActivityForResult(Intent.createChooser(i, getString(R.string.menu_context_event_share)), 0);
	    default:
		break;
	    }
	    return true;
	}
	return false;
    }

    private static class ViewGroupHolder {
	TextView title;
	TextView nbItem;
    }

    private static class ViewChildHolder {
	View color;
	TextView title;
	TextView name;
	TextView label;
    }

    public class ScheduleExpandableListAdapter extends BaseExpandableListAdapter {

	private List<Map<String, Object>> list = null;
	private LayoutInflater mInflater;

	private ViewGroupHolder viewGroupHolder;
	private ViewChildHolder viewChildHolder;
	private Boolean dateDisplayType = true;
	
	public ScheduleExpandableListAdapter(Context context) {
	    mInflater = LayoutInflater.from(context);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getChild(int groupId, int childPosition) {
	    if (list == null)
		return null;
	    Map<String, Object> group = (Map<String, Object>) getGroup(groupId);
	    if (group != null) {
		List<Map<String, Object>> childs = (List<Map<String, Object>>) group.get(Schedule.CHILD);
		if (childs != null) {
		    return childs.get(childPosition);
		}
	    }
	    return null;
	}

	@Override
	public long getChildId(int groupId, int childPosition) {
	    return childPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
	    Map<String, Object> block = (Map<String, Object>) getChild(groupPosition, childPosition);
	    if (convertView == null) {
		convertView = mInflater.inflate(R.layout.schedule_search_row, null);
		viewChildHolder = new ViewChildHolder();
		viewChildHolder.color = convertView.findViewById(R.id.schedule_color);
		viewChildHolder.title = ((TextView) convertView.findViewById(R.id.schedule_title));
		viewChildHolder.name = ((TextView) convertView.findViewById(R.id.schedule_name));
		viewChildHolder.label = ((TextView) convertView.findViewById(R.id.schedule_label));
		convertView.setTag(viewChildHolder);

	    } else {
		viewChildHolder = (ViewChildHolder) convertView.getTag();
	    }

	    String c = (String) block.get(Schedule.COLOR);
	    Integer color = 1;
	    if (c != null) {
		color = Integer.parseInt(c);
	    }
	    viewChildHolder.color.setBackgroundColor(getLiturgicalColor(color));
	    if (dateDisplayType) {
		viewChildHolder.title.setText((String) block.get(Schedule.HEURE));
	    }
	    else {
		viewChildHolder.title.setText((String) block.get(Schedule.FDATE) + " - " + block.get(Schedule.HEURE));
	    }
	    viewChildHolder.name.setText((String) block.get(Church.NOM));
	    viewChildHolder.label.setText((String) block.get(Church.CP) + " " + (String) block.get(Church.COMMUNE));
	    return convertView;
	}

	public int getLiturgicalColor(Integer massType) {
	    // TODO Avent, Careme et temps après Pâques !
	    switch (massType) {
	    case 1: // Blanc
		return 0xFFFFFFFF; // Blanc
	    case 2: // Violet
		return 0xFF990099; // Violet
	    case 3: // Rouge
		return 0xFFFF0000; // Rouge
	    case 4: // Noir
		return 0xFF000000; // Noir
	    default: // Vert
		return 0xFF006600; // Vert
	    }
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getChildrenCount(int groupId) {
	    if (list == null)
		return 0;
	    Map<String, Object> group = (Map<String, Object>) getGroup(groupId);
	    if (group != null) {
		List<Map<String, Object>> childs = (List<Map<String, Object>>) group.get(Schedule.CHILD);
		if (childs != null) {
		    return childs.size();
		}
	    }
	    return 0;
	}

	@Override
	public Object getGroup(int groupId) {
	    return list.get(groupId);
	}

	@Override
	public int getGroupCount() {
	    if (list == null)
		return 0;
	    return list.size();
	}

	@Override
	public long getGroupId(int groupId) {
	    return groupId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
	    Map<String, Object> block = (Map<String, Object>) getGroup(groupPosition);
	    if (convertView == null) {
		convertView = mInflater.inflate(R.layout.schedule_separator_view, null);
		viewGroupHolder = new ViewGroupHolder();
		viewGroupHolder.title = ((TextView) convertView.findViewById(R.id.text_sep));
		viewGroupHolder.nbItem = ((TextView) convertView.findViewById(R.id.text_count));
		convertView.setTag(viewGroupHolder);
	    } else {
		viewGroupHolder = (ViewGroupHolder) convertView.getTag();
	    }
	    viewGroupHolder.title.setText((String) block.get(Schedule.NOM));
	    viewGroupHolder.nbItem.setText("(" + getChildrenCount(groupPosition) + " items)");
	    return convertView;
	}

	@Override
	public boolean hasStableIds() {
	    return true;
	}

	@Override
	public boolean isChildSelectable(int groupId, int childPosition) {
	    return true;
	}

	public void setList(List<Map<String, Object>> list) {
	    this.list = list;
	    if (list != null) {
		if (list.size() > 0 && !list.get(0).containsKey(Schedule.DATE)) {
		    dateDisplayType = false;
		}
		else {
		    dateDisplayType = true;
		}
	    }
	    super.notifyDataSetChanged();
	}

	public List<Map<String, Object>> getList() {
	    return list;
	}

    }

}
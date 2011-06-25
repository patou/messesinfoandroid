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
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import cef.messesinfo.ExpandableView;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.client.Server;
import cef.messesinfo.provider.Church;
import cef.messesinfo.provider.Schedule;

public class ScheduleActivity extends ExpandableListActivity implements OnScrollListener {

    private static final int MENU_EVENT = 4;
    private static final int MENU_SHARE = 5;

    private Server server;
    private TextView empty;
    private int start = 0;
    private boolean isSearchEnd = false;
    private boolean isLoading;
    private ScheduleExpandableListAdapter scheduleExpandableListAdapter;
    private String code;

    public ScheduleActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.expandable_list);
	code = getIntent().getStringExtra(Church.ID);
	empty = (TextView) findViewById(android.R.id.empty);
	scheduleExpandableListAdapter = new ScheduleExpandableListAdapter(ScheduleActivity.this);
	setListAdapter(scheduleExpandableListAdapter);
	final Object data = getLastNonConfigurationInstance();
	Log.e("MESSESINFO onCreate:", data != null ? "savedList" : "none");
	if (data != null) {
	    List<Map<String, Object>> list = (List<Map<String, Object>>) data;
	    Log.e("MESSESINFO onCreate:", list != null ? "size : " + list.size() : "none");
	    scheduleExpandableListAdapter.setList(list);
	} else {
	    loadList(code, false);
	}
	registerForContextMenu(getExpandableListView());
	getExpandableListView().setClickable(true);
	getExpandableListView().setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	boolean loadMore = ((ExpandableView) view).getScrollPosition() >= 96;
	Log.d("MESSESINFO onScroll:", "f=" + firstVisibleItem + ", vc=" + visibleItemCount + ", tc=" + totalItemCount);
	if (loadMore && !isLoading && !isSearchEnd) {
	    Log.d("MESSESINFO onScroll:", "loadMore");
	    Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
	    loadList(code, true);
	}
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
	List<Map<String, Object>> list = scheduleExpandableListAdapter.getList();
	Log.e("MESSESINFO onRetainNonConfigurationInstance:", list != null ? "size : " + list.size() : "none");
	return list;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }
    
    public void goHome(View v) {
	final Intent intent = new Intent(this, MessesInfo.class);
	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
    }

    private void loadList(final String code, final boolean loadMore) {
	isLoading = true;
	new Thread(new Runnable() {

	    @Override
	    public void run() {
		server = new Server(getString(R.string.server_url));
		int pageSize = 25;
		if (!loadMore) {
		    start = 0;
		}
		try {
		    final List<Map<String, Object>> result = server.getLocationSchedule(code, start, pageSize);
		    if (result != null) {
			int size = getSize(result);
			start += size;
			isSearchEnd = size < pageSize;
			runOnUiThread(new Runnable() {

			    @Override
			    public void run() {
				empty.setText(getString(R.string.schedules_empty));
				if (loadMore) {
				    scheduleExpandableListAdapter.appendList(result);
				} else {
				    scheduleExpandableListAdapter.setList(result);
				    if (result.size() > 0)
					getExpandableListView().expandGroup(0);
				    if (result.size() > 1)
					getExpandableListView().expandGroup(1);
				}
				isLoading = false;
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
		    isLoading = false;
		}
	    }
	}).start();
    }

    private int getSize(final List<Map<String, Object>> result) {
	int size = 0;
	for (Map<String, Object> group : result) {
	    if (group != null) {
		List<Map<String, Object>> childs = (List<Map<String, Object>>) group.get(Schedule.CHILD);
		if (childs != null) {
		    size += childs.size();
		}
	    }
	}
	return size;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	if (v.getId() == android.R.id.list) {
	    ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
	    int typeId = ExpandableListView.getPackedPositionType(info.packedPosition);
	    if (typeId == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
		int groupId = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childId = ExpandableListView.getPackedPositionChild(info.packedPosition);
		Map<String, String> item = (Map<String, String>) getExpandableListAdapter().getChild(groupId, childId);
		if (item != null) {
		    menu.setHeaderTitle(item.get(Schedule.TIME));
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
	    Map<String, String> item = (Map<String, String>) getExpandableListAdapter().getChild(groupId, childId);
	    Map<String, Object> itemGroup = (Map<String, Object>) getExpandableListAdapter().getGroup(groupId);
	    switch (menuItem.getItemId()) {
	    case MENU_EVENT:
		try {
		    Intent intent = new Intent(Intent.ACTION_EDIT);
		    intent.setType("vnd.android.cursor.item/event");
		    intent.putExtra("title", item.get(Church.NAME));
		    intent.putExtra("description", item.get(Church.COMMUNITY) + "\n" + item.get(Church.CITY));
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH'h'mm", Locale.FRANCE);
		    Date d = sdf.parse(item.get(Schedule.DATE) + " " + item.get(Schedule.TIME));
		    intent.putExtra("beginTime", d.getTime());
		    d.setHours(d.getHours() + 1);
		    intent.putExtra("endTime", d.getTime());
		    startActivity(intent);
		} catch (ParseException e) {
		    e.printStackTrace();
		}
		break;
	    case MENU_SHARE:
		Intent i = new Intent(Intent.ACTION_SEND)
			.putExtra(Intent.EXTRA_TEXT, item.get(Schedule.TIME) + "\n" + itemGroup.get(Schedule.LABEL) + "\n" + item.get(Church.CITY)).setType("text/plain").putExtra(
				Intent.EXTRA_SUBJECT, item.get(Church.NAME));
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

    /*
     * private List<Map<String, Object>> createTestData(){ List<Map<String, Object>> list = new ArrayList<Map<String,Object>>(); for (int i = 0; i < 3; i++) {
     * list.add(createGroupTest(i)); } return list; }
     * 
     * private Map<String, Object> createGroupTest(int i){ Map<String, Object> group = new HashMap<String, Object>(); group.put(Schedule.NOM, "Group " + i); List<Map<String,
     * Object>> list = new ArrayList<Map<String,Object>>(); for (int j = 0; j < 3; j++) { list.add(createChildTest(j)); } group.put(Schedule.CHILD, list); return group; }
     * 
     * private Map<String, Object> createChildTest(int i){ Map<String, Object> child = new HashMap<String, Object>(); child.put(Schedule.NOM, "Child " + i);
     * child.put(Schedule.SCHEDULE, "11h"); child.put(Schedule.PAROISSE, "paroisse"); child.put(Schedule.COMMUNE, "commune"); child.put(Schedule.CODE, "code");
     * child.put(Schedule.SCHEDULE, "" + i); return child; }
     */
    public class ScheduleExpandableListAdapter extends BaseExpandableListAdapter {

	private List<Map<String, Object>> list = null;
	private LayoutInflater mInflater;

	private ViewGroupHolder viewGroupHolder;
	private ViewChildHolder viewChildHolder;

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

	    String color = (String) block.get(Schedule.LITURGICALTIMECODE);
	    viewChildHolder.color.setBackgroundColor(getLiturgicalColor(color));
	    viewChildHolder.title.setText((String) block.get(Schedule.TIME));

	    viewChildHolder.name.setText((String) block.get(Schedule.MISCELLANEOUS));
	    viewChildHolder.label.setText((String) block.get(Schedule.LABEL) + " " + (String) block.get(Schedule.LANGUAGE));
	    return convertView;
	}

	public int getLiturgicalColor(String massType) {
	    if (massType == null)
		return 0xFFFFFFFF; // Blanc
	    if (massType.equals("white")) // Blanc
		return 0xFFFFFFFF; // Blanc
	    if (massType.equals("violet")) // Violet
		return 0xFF990099; // Violet
	    if (massType.equals("red")) // Rouge
		return 0xFFFF0000; // Rouge
	    if (massType.equals("black")) // Noir
		return 0xFF000000; // Noir
	    if (massType.equals("green")) // Vert
		return 0xFF006600; // Vert
	    return 0xFFFFFFFF;
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
	    viewGroupHolder.title.setText((String) block.get(Schedule.LABEL));
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
	    notifyDataSetChanged();
	}

	public void appendList(List<Map<String, Object>> list) {
	    if (this.list == null) {
		setList(list);
	    } else {
		boolean found = false;
		String oldName, newName;
		for (Map<String, Object> newGroup : list) {
		    found = false;
		    for (Map<String, Object> oldGroup : this.list) {
			newName = (String) newGroup.get(Schedule.LABEL);
			oldName = (String) oldGroup.get(Schedule.LABEL);
			if (newName != null && oldName != null && newName.equals(oldName)) {
			    found = true;
			    List<Map<String, Object>> oldChilds = (List<Map<String, Object>>) oldGroup.get(Schedule.CHILD);
			    List<Map<String, Object>> newChilds = (List<Map<String, Object>>) newGroup.get(Schedule.CHILD);
			    if (oldChilds != null) {
				oldChilds.addAll(newChilds);
			    }
			    break;
			}
		    }
		    if (!found) {
			this.list.add(newGroup);
		    }
		}
		super.notifyDataSetChanged();
		notifyDataSetChanged();
	    }
	}

	public List<Map<String, Object>> getList() {
	    return list;
	}

    }
}

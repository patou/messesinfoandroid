package cef.messeinfo.activity;

import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import cef.messeinfo.R;
import cef.messeinfo.client.Server;
import cef.messeinfo.provider.Schedule;

public class ScheduleActivity extends ExpandableListActivity {

    private Server server;
    private TextView empty;

    public ScheduleActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.expandable_list);
	final String code = getIntent().getStringExtra("code");
	empty = (TextView) findViewById(android.R.id.empty);
	new Thread(new Runnable() {

	    @Override
	    public void run() {
		server = new Server(getString(R.string.server_url));
		try {
		    final List<Map<String, Object>> result = server.getSchedule(code);
		    if (result != null) {
			runOnUiThread(new Runnable() {

			    @Override
			    public void run() {
				ScheduleExpandableListAdapter scheduleExpandableListAdapter = new ScheduleExpandableListAdapter(ScheduleActivity.this);
				empty.setText(getString(R.string.schedules_empty));
				scheduleExpandableListAdapter.setList(result);
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
     * private List<Map<String, Object>> createTestData(){ List<Map<String, Object>> list = new ArrayList<Map<String,Object>>(); for (int i = 0; i <
     * 3; i++) { list.add(createGroupTest(i)); } return list; }
     * 
     * private Map<String, Object> createGroupTest(int i){ Map<String, Object> group = new HashMap<String, Object>(); group.put(Schedule.NOM, "Group "
     * + i); List<Map<String, Object>> list = new ArrayList<Map<String,Object>>(); for (int j = 0; j < 3; j++) { list.add(createChildTest(j)); }
     * group.put(Schedule.CHILD, list); return group; }
     * 
     * private Map<String, Object> createChildTest(int i){ Map<String, Object> child = new HashMap<String, Object>(); child.put(Schedule.NOM, "Child "
     * + i); child.put(Schedule.SCHEDULE, "11h"); child.put(Schedule.PAROISSE, "paroisse"); child.put(Schedule.COMMUNE, "commune");
     * child.put(Schedule.CODE, "code"); child.put(Schedule.SCHEDULE, "" + i); return child; }
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

	    String c = (String) block.get(Schedule.COLOR);
	    Integer color = 1;
	    if (c != null) {
		color = Integer.parseInt(c);
	    }
	    viewChildHolder.color.setBackgroundColor(getLiturgicalColor(color));
	    viewChildHolder.title.setText((String) block.get(Schedule.HEURE));

	    viewChildHolder.name.setText((String) block.get(Schedule.LIBRE));
	    viewChildHolder.label.setText((String) block.get(Schedule.LIBELLE) + " " + (String) block.get(Schedule.LANGUE));
	    return convertView;
	}

	public int getLiturgicalColor(Integer massType) {
	    // TODO Avent, Careme et temps apr�s P�ques !
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
	    return false;
	}

	public void setList(List<Map<String, Object>> list) {
	    this.list = list;
	}

	public List<Map<String, Object>> getList() {
	    return list;
	}

    }
}

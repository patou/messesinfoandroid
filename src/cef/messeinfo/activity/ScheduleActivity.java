package cef.messeinfo.activity;

import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
	private Handler handler = new Handler();

	public ScheduleActivity() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.expandable_list);
		final String code = getIntent().getStringExtra("code");
		new Thread(new Runnable() {

			@Override
			public void run() {
				server = new Server(getString(R.string.server_url));
				final List<Map<String, Object>> result = server.getSchedule(code);
				if (result != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							ScheduleExpandableListAdapter scheduleExpandableListAdapter = new ScheduleExpandableListAdapter(ScheduleActivity.this);
							TextView empty = (TextView) findViewById(android.R.id.empty);
							empty.setText(getString(R.string.schedules_empty));
							scheduleExpandableListAdapter.setList(result);
							setListAdapter(scheduleExpandableListAdapter);
						}
					});
				}
				
			}
		}).start();
	}

	/*
	 * private List<Map<String, Object>> createTestData(){ List<Map<String,
	 * Object>> list = new ArrayList<Map<String,Object>>(); for (int i = 0; i <
	 * 3; i++) { list.add(createGroupTest(i)); } return list; }
	 * 
	 * private Map<String, Object> createGroupTest(int i){ Map<String, Object>
	 * group = new HashMap<String, Object>(); group.put(Schedule.NOM, "Group " +
	 * i); List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
	 * for (int j = 0; j < 3; j++) { list.add(createChildTest(j)); }
	 * group.put(Schedule.CHILD, list); return group; }
	 * 
	 * private Map<String, Object> createChildTest(int i){ Map<String, Object>
	 * child = new HashMap<String, Object>(); child.put(Schedule.NOM, "Child " +
	 * i); child.put(Schedule.SCHEDULE, "11h"); child.put(Schedule.PAROISSE,
	 * "paroisse"); child.put(Schedule.COMMUNE, "commune");
	 * child.put(Schedule.CODE, "code"); child.put(Schedule.SCHEDULE, "" + i);
	 * return child; }
	 */
	public class ScheduleExpandableListAdapter extends BaseExpandableListAdapter {

		private List<Map<String, Object>> list = null;
		private Context m_context;

		public ScheduleExpandableListAdapter(Context context) {
			m_context = context;
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
			View rv;
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rv = vi.inflate(R.layout.schedule_row, null);
			} else {
				rv = convertView;
			}

			String c = (String) block.get(Schedule.COLOR);
			Integer color = 1;
			if (c != null) {
				color = Integer.parseInt(c);
			}
			rv.findViewById(R.id.schedule_color).setBackgroundColor(getLiturgicalColor(color));
			((TextView) rv.findViewById(R.id.schedule_title)).setText((String) block.get(Schedule.HEURE));
			
			((TextView) rv.findViewById(R.id.schedule_speakers)).setText((String) block.get(Schedule.LIBRE));
			TextView st = ((TextView) rv.findViewById(R.id.schedule_track));
			st.setText((String) block.get(Schedule.LIBELLE));
			// st.setTextColor(sri.getColor());
			((TextView) rv.findViewById(R.id.schedule_room)).setText((String) block.get(Schedule.LANGUE));
			rv.findViewById(R.id.schedule_track).setVisibility(View.VISIBLE);
			rv.findViewById(R.id.schedule_room).setVisibility(View.VISIBLE);
			return rv;
		}
		
		public int getLiturgicalColor(Integer massType) {
			//TODO Avent, Careme et temps après Pâques !
			switch (massType) {
			case 1: //Blanc
				return 0xFFFFFFFF; // Blanc
			case 2: //Violet
				return 0xFF990099; //Violet
			case 3: // Rouge
				return 0xFFFF0000; //Rouge
			case 4: //Noir
				return 0xFF000000; // Noir
			default: //Vert
				return 0xFF006600; //Vert
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
			View rv;
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rv = vi.inflate(R.layout.schedule_separator_view, null);
			} else {
				rv = convertView;
			}
			((TextView) rv.findViewById(R.id.text_sep)).setText((String) block.get(Schedule.NOM));
			((TextView) rv.findViewById(R.id.text_count)).setText("(" + getChildrenCount(groupPosition) + " items)");
			return rv;
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

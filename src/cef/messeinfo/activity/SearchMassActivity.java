package cef.messeinfo.activity;

import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import cef.messeinfo.R;
import cef.messeinfo.client.Server;
import cef.messeinfo.provider.Church;
import cef.messeinfo.provider.Schedule;

public class SearchMassActivity extends ExpandableListActivity {

	private Server server;
	private Handler handler = new Handler();
	private TextView empty;
	private EditText searchText;
	private View button;
	private ScheduleExpandableListAdapter scheduleExpandableListAdapter;

	public SearchMassActivity() {

	}
	
    /**
     * Start the Activity
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
		empty = (TextView) findViewById(android.R.id.empty);
        empty.setText(getString(R.string.list_search_help));
        searchText = (EditText) findViewById(R.id.searchField);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                empty.setText(getString(R.string.list_search_loading));
                final String search = searchText.getText().toString().trim();
        		new Thread(new Runnable() {

        			@Override
        			public void run() {
        				server = new Server(getString(R.string.server_url));
        				final List<Map<String, Object>> result = server.searchSchedule(search);
        				if (result != null) {
        					handler.post(new Runnable() {
        						@Override
        						public void run() {
        							if (result != null && result.size() > 0) {
        								scheduleExpandableListAdapter.setList(result);
        							}
        							else {
        								scheduleExpandableListAdapter.setList(null);
        								empty.setText(getString(R.string.schedules_empty));
        							}
        							setListAdapter(scheduleExpandableListAdapter);
        						}
        					});
        				}
        			}
        		}).start();
            }
        });
		
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Map<String, Object> child =  (Map<String, Object>) scheduleExpandableListAdapter.getChild(groupPosition, childPosition);
		if (child != null) {
			String code = (String) child.get(Church.CODE);
			if (code != null)
				ChurchActivity.activityStart(SearchMassActivity.this, code);
		}
		return true;
	}
	
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
				rv = vi.inflate(R.layout.schedule_search_row, null);
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
			
			((TextView) rv.findViewById(R.id.schedule_name)).setText((String) block.get(Church.NOM));
			TextView st = ((TextView) rv.findViewById(R.id.schedule_label));
			st.setText((String) block.get(Church.CP) + " " + (String) block.get(Church.COMMUNE));
			// st.setTextColor(sri.getColor());
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
			return true;
		}

		public void setList(List<Map<String, Object>> list) {
			this.list = list;
			if (list != null)
				super.notifyDataSetChanged();
		}

		public List<Map<String, Object>> getList() {
			return list;
		}

	}

}
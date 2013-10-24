package cef.messesinfo.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import cef.messesinfo.ExpandableView;
import cef.messesinfo.MessesInfo;
import cef.messesinfo.R;
import cef.messesinfo.client.Server;
import cef.messesinfo.provider.Church;
import cef.messesinfo.provider.Schedule;

public class SearchScheduleActivity extends ExpandableListActivity implements OnScrollListener {
    private static final int MENU_DETAIL = 0;
    private static final int MENU_SCHEDULE = 1;
    private static final int MENU_CENTER = 2;
    private static final int MENU_NEAR = 3;
    private static final int MENU_EVENT = 4;
    private static final int MENU_SHARE = 5;

    private static final int CHOOSE_DEPARTMENT = 1;
    private static final int CHOOSE_EXTENSION_SEARCH = 2;

    private Server server;
    private TextView empty;
    private TextView titleText;
    private ScheduleExpandableListAdapter scheduleExpandableListAdapter;
    int start;
    private String search;
    private boolean isSearchEnd = false;
    private boolean isLoading;
    private TextView loadMoreView;
    private List<Map<String, Object>> listChoose;

    public SearchScheduleActivity() {

    }

    /**
     * Start the Activity
     *
     * @param context
     */
    public static void activityStart(Context context, String search) {
        Intent intent = new Intent(context, SearchScheduleActivity.class);
        intent.putExtra("search", search);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_expandable_list);
        loadMoreView = new TextView(this);
        loadMoreView.setWidth(LayoutParams.FILL_PARENT);
        loadMoreView.setText("Suite ...");
        loadMoreView.setHeight(50);
        loadMoreView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
        loadMoreView.setGravity(Gravity.CENTER);
        loadMoreView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loadMore();
            }
        });
        getExpandableListView().addFooterView(loadMoreView);
        scheduleExpandableListAdapter = new ScheduleExpandableListAdapter(SearchScheduleActivity.this);
        setListAdapter(scheduleExpandableListAdapter);
        titleText = (TextView) findViewById(R.id.title_text);
        empty = (TextView) findViewById(android.R.id.empty);
        registerForContextMenu(getExpandableListView());
        getExpandableListView().setOnScrollListener(this);
        final RetainNonConfigurationValue data = (RetainNonConfigurationValue) getLastNonConfigurationInstance();
        if (data != null) {
            scheduleExpandableListAdapter.setList(data.list);
            search = data.search;
            titleText.setText(search);
        } else {
            scheduleExpandableListAdapter.setList(null);
        }
        search = getIntent().getStringExtra("search");
        if (search != null)
            search(search);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean loadMore = ((ExpandableView) view).getScrollPosition() >= 96;
        Log.d("MESSESINFO onScroll:", "f=" + firstVisibleItem + ", vc=" + visibleItemCount + ", tc=" + totalItemCount);
        if (loadMore) {
            Log.d("MESSESINFO onScroll:", "loadMore");
            loadMore();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case CHOOSE_DEPARTMENT:
                DialogButtonClickHandler dialogButtonClickHandler = new DialogButtonClickHandler();
                Builder dialog = new AlertDialog.Builder(this).setTitle(R.string.search_mass_choose_department).setPositiveButton("OK", dialogButtonClickHandler).setNeutralButton("Cancel", null);
                String[] department = new String[listChoose.size()];
                int i = 0;
                for (Map<String, Object> item : listChoose) {
                    department[i++] = (String) item.get(Schedule.LABEL);
                }
                dialog.setItems(department, dialogButtonClickHandler);
                return dialog.create();
            case CHOOSE_EXTENSION_SEARCH:
                DialogButtonClickHandler dialogExtButtonClickHandler = new DialogButtonClickHandler();
                Builder dialogExt = new AlertDialog.Builder(this).setTitle(R.string.search_mass_extension_search).setPositiveButton("OK", dialogExtButtonClickHandler).setNeutralButton("Cancel", null);
                String[] locations = new String[listChoose.size()];
                int j = 0;
                for (Map<String, Object> item : listChoose) {
                    locations[j++] = item.get(Church.NAME) + " � " + item.get(Church.CITY);
                }
                dialogExt.setItems(locations, dialogExtButtonClickHandler);
                return dialogExt.create();

            default:
                break;
        }
        return null;
    }

    public class DialogButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            if (clicked >= 0) {
                search((String) listChoose.get(clicked).get("query"));
            }
        }
    }

    public void goHome(View v) {
        final Intent intent = new Intent(this, MessesInfo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        RetainNonConfigurationValue value = new RetainNonConfigurationValue();
        value.list = scheduleExpandableListAdapter.getList();
        value.search = search;
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Map<String, Object> child = (Map<String, Object>) scheduleExpandableListAdapter.getChild(groupPosition, childPosition);
        if (child != null) {
            String code = (String) child.get(Church.ID);
            if (code != null)
                ChurchActivity.activityStartSchedule(SearchScheduleActivity.this, code);
        }
        return true;
    }

    private void search(final String search) {
        search(search, false);
    }

    private void loadMore() {
        if (search != null && !isLoading && !isSearchEnd) {
            search(search, true);
        }
    }

    public void onRefreshClick(View v) {
        search(search);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        findViewById(R.id.title_refresh_progress).setVisibility(loading ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_title_refresh).setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void search(final String search, final boolean loadMore) {
        this.search = search;
        setLoading(true);
        loadMoreView.setText("Chargement ...");
        titleText.setText(search);
        if (!loadMore) {
            scheduleExpandableListAdapter.setList(null);
            isSearchEnd = false;
        }
        empty.setText(getString(R.string.list_search_loading));

        new Thread(new Runnable() {

            @Override
            public void run() {
                server = new Server(getString(R.string.server_url));
                MessesInfo.getTracker(SearchScheduleActivity.this).trackEvent("Application", "SearchMass", search, 1);
                try {
                    int pageSize = 10;
                    if (!loadMore) {
                        start = 0;
                        pageSize = 25;
                    }
                    final List<Map<String, Object>> result = server.searchSchedule(search, start, pageSize);
                    if (result != null) {
                        final Map<String, Object> item = result.remove(0);
                        final int size = getSize(result);
                        start += size;
                        isSearchEnd = size < pageSize;
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (loadMore) {
                                    scheduleExpandableListAdapter.appendList(result);
                                    loadMoreView.setText(size == 0 ? "" : "Suite ...");
                                } else {
                                    if (size > 0) {
                                        scheduleExpandableListAdapter.setList(result);
                                        getExpandableListView().expandGroup(0);
                                        if (result.size() > 1)
                                            getExpandableListView().expandGroup(1);
                                    } else {
                                        if ("department".equals(item.get("queryType"))) {
                                            listChoose = result;
                                            showDialog(CHOOSE_DEPARTMENT);
                                        } else if ("schedule".equals(item.get("queryType"))) {
                                            listChoose = result;
                                            showDialog(CHOOSE_EXTENSION_SEARCH);
                                        } else {
                                            scheduleExpandableListAdapter.setList(null);
                                            String message = (String) item.get("errorMessage");
                                            empty.setText(message != null ? message : getString(R.string.schedules_empty));
                                        }
                                    }
                                }
                                // setListAdapter(scheduleExpandableListAdapter);
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(getWindowToken(), 0);

                                setLoading(false);
                            }
                        });
                    }
                } catch (XMLRPCException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            empty.setText(R.string.error_church_schedule);
                            setLoading(false);
                        }
                    });
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
                Map<String, String> item = (Map<String, String>) scheduleExpandableListAdapter.getChild(groupId, childId);
                if (item != null) {
                    menu.setHeaderTitle(item.get(Church.NAME));
                    menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE, R.string.menu_context_detail);
                    menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE, R.string.menu_context_schedules);
                    if (item.containsKey(Church.LAT) && item.containsKey(Church.LNG)) {
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
            String code = item.get(Church.ID);
            switch (menuItem.getItemId()) {
                case MENU_DETAIL:
                    ChurchActivity.activityStart(this, code);
                    break;
                case MENU_SCHEDULE:
                    ChurchActivity.activityStartSchedule(this, code);
                    break;
                case MENU_CENTER:
                    if (item.containsKey(Church.LAT) && item.containsKey(Church.LNG)) {
                        NearMapActivity.activityStart(this, item.get(Church.LAT), item.get(Church.LNG));
                    }
                    break;
                case MENU_NEAR:
                    if (item.containsKey(Church.LAT) && item.containsKey(Church.LNG)) {
//		    searchText.setText("> " + item.get(Church.ID));
                        search("> " + item.get(Church.LAT) + ":" + item.get(Church.LNG));
                    }
                    break;
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
                    Intent i = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT,
                            item.get(Schedule.TIME) + "\n" + item.get(Schedule.DATE) + "\n" + "\n" + item.get(Church.CITY)).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT,
                            item.get(Church.NAME));
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

            String color = (String) block.get(Schedule.LITURGICALTIMECODE);
            viewChildHolder.color.setBackgroundColor(getLiturgicalColor(color));
            if (dateDisplayType) {
                viewChildHolder.title.setText((String) block.get(Schedule.TIME));
            } else {
                viewChildHolder.title.setText((String) block.get(Schedule.FDATE) + " - " + block.get(Schedule.TIME));
            }
            viewChildHolder.name.setText((String) block.get(Church.NAME));
            viewChildHolder.label.setText((String) block.get(Church.ZIPCODE) + " " + (String) block.get(Church.CITY));
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
            if (list != null) {
                if (list.size() > 0 && !list.get(0).containsKey(Schedule.DATE)) {
                    dateDisplayType = false;
                } else {
                    dateDisplayType = true;
                }
            }
            super.notifyDataSetChanged();
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
            }
        }

        public List<Map<String, Object>> getList() {
            return list;
        }

    }

    class RetainNonConfigurationValue {
        String search;
        List<Map<String, Object>> list;
    }

}
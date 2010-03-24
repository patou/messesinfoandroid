package cef.messeinfo.activity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import cef.messeinfo.R;

public class NearListActivity extends ListActivity {
    List<Map<String, String>> list = null;
    private ChurchAdapter mAdapter;
    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context, List<Map<String, String>> list) {
        Intent intent = new Intent(context, NearListActivity.class);
        intent.putExtra("list", (Serializable)list);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        mAdapter = new ChurchAdapter(this);
        list = (List<Map<String, String>>) getIntent().getSerializableExtra("list");
        mAdapter.setList(list);
        setListAdapter( mAdapter ); 
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, String> item = list.get(position);
        String code = item.get("code");
        ChurchActivity.activityStart(this, code);
    }
}

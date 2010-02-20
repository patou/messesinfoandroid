package cef.messeinfo.activity;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import cef.messeinfo.R;
import cef.messeinfo.client.Server;

public class ChurchBookActivity extends ListActivity {
    List<Map<String, String>> list = null;
    private ChurchAdapter mAdapter;
    private EditText searchText;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
        context.startActivity(new Intent(context, ChurchBookActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        mAdapter = new ChurchAdapter(this);

        setListAdapter(mAdapter);

        ImageButton button = (ImageButton) findViewById(R.id.searchButton);
        empty = (TextView) findViewById(android.R.id.empty);
        empty.setText(getString(R.string.list_search_help));
        searchText = (EditText) findViewById(R.id.searchField);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                empty.setText(getString(R.string.list_search_loading));
                final String search = searchText.getText().toString();
                mAdapter.setList(null);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        list = new Server(getString(R.string.server_url)).searchChurch(search, 0);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (list != null && list.size() > 0) {
                                    mAdapter.setList(list);
                                }
                                else {
                                    empty.setText(getString(R.string.list_empty));
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, String> item = list.get(position);
        String code = item.get("code");
        ChurchActivity.activityStart(this, code);
    }

    private Handler handler = new Handler();
    private TextView empty;
}

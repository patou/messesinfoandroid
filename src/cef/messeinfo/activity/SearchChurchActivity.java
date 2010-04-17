package cef.messeinfo.activity;

import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cef.messeinfo.MesseInfo;
import cef.messeinfo.R;
import cef.messeinfo.client.Server;

public class SearchChurchActivity extends ListActivity {
    List<Map<String, String>> list = null;
    private ChurchAdapter mAdapter;
    private EditText searchText;
    private TextView empty;

    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
	context.startActivity(new Intent(context, SearchChurchActivity.class));
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
	searchText.setImeOptions(DEFAULT_KEYS_SEARCH_LOCAL);
	button.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		search();
	    }
	});
	searchText.setOnEditorActionListener(new OnEditorActionListener() {
	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		search();
		return true;
	    }
	});
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	Map<String, String> item = list.get(position);
	String code = item.get("code");
	ChurchActivity.activityStart(this, code);
    }

    private void search() {
	empty.setText(getString(R.string.list_search_loading));
	final String search = searchText.getText().toString();
	mAdapter.setList(null);
	new Thread(new Runnable() {

	    @Override
	    public void run() {
		MesseInfo.getTracker().trackEvent("Application", "SearchChurch", search, 1);
		try {
		    list = new Server(getString(R.string.server_url)).searchChurch(search, 0);
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    if (list != null && list.size() > 0) {
				mAdapter.setList(list);
			    } else {
				empty.setText(getString(R.string.list_empty));
			    }
			}
		    });
		} catch (XMLRPCException e) {
		    e.printStackTrace();
		    runOnUiThread(new Runnable() {
			@Override
			public void run() {
			    empty.setText(R.string.error_church_book);
			}
		    });
		}

	    }
	}).start();
    }
}

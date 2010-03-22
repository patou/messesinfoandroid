package cef.messeinfo.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import cef.messeinfo.R;
import cef.messeinfo.provider.Church;

public class FavoriteActivity extends ListActivity {
    /**
     * Start the Activity
     * 
     * @param context
     */
    public static void activityStart(Context context) {
        context.startActivity(new Intent(context, FavoriteActivity.class));
    }

    private static final String[] PROJECTION = new String[] { Church._ID, // 0
            Church.CODE, // 1
            Church.NOM, // 2
            Church.COMMUNE, // 3
            Church.PAROISSE // 4
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
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_church_item, cursor, new String[] { Church.NOM, // 2
                Church.COMMUNE, // 3
                Church.PAROISSE }, new int[] { R.id.nom, R.id.commune, R.id.paroisse });
        setListAdapter(adapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        SimpleCursorAdapter adp = (SimpleCursorAdapter)l.getAdapter();
        Cursor cursor = adp.getCursor();
        cursor.moveToPosition(position);
        String code = cursor.getString(1);
        ChurchActivity.activityStart(this, code);
    }
}

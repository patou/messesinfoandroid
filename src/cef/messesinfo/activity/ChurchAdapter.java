package cef.messesinfo.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cef.messesinfo.R;
import cef.messesinfo.provider.Church;

public class ChurchAdapter extends BaseAdapter {
    List<Map<String,String>> list = null;
    private ViewHolder holder;
    private LayoutInflater mInflater;
    private Context context;
    
    public ChurchAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public List<Map<String, String>> getList() {
        return list;
    }

    public void setList(List<Map<String, String>> list) {
        this.list = list;
        notifyDataSetChanged();
    }
    
    public void appendList(List<Map<String, String>> list) {
	if (this.list == null) {
	    setList(list);
	}
	else {
	    this.list.addAll(list);
	    notifyDataSetChanged();
	}
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return list != null ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_church_item, null);

            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.nom = (TextView) convertView.findViewById(R.id.nom);
            holder.commune = (TextView) convertView.findViewById(R.id.commune);
            holder.paroisse = (TextView) convertView.findViewById(R.id.paroisse);
            holder.next_mass = (TextView) convertView.findViewById(R.id.next_mass);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String,String> item = list.get(position);
        holder.icon.setImageResource(R.drawable.church1);
        holder.nom.setText(item.get(Church.NAME));
        holder.paroisse.setText(item.get(Church.COMMUNITY));
        holder.commune.setText(item.get(Church.ZIPCODE) +  " " + item.get(Church.CITY));
        String next_mass = item.get(Church.NEXT_MASS);
	if (next_mass != null) {
	    try {
		Date date_next_mass = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(next_mass.substring(0, 16));
		holder.next_mass.setText(context.getString(R.string.church_next_mass) + new SimpleDateFormat("EEE d 'à' HH'h'mm").format(date_next_mass));
		holder.next_mass.setVisibility(View.VISIBLE);
	    } catch (ParseException e) {
		e.printStackTrace();
	    }
	}
	else {
	    holder.next_mass.setVisibility(View.GONE);
	}
        return convertView;
    }
    
    private static class ViewHolder {
        public TextView next_mass;
	ImageView icon;
        TextView nom;
        TextView paroisse;
        TextView commune;
    }

}

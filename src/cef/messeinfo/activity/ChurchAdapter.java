package cef.messeinfo.activity;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cef.messeinfo.R;
import cef.messeinfo.provider.Church;

public class ChurchAdapter extends BaseAdapter {
    List<Map<String,String>> list = null;
    private ViewHolder holder;
    private LayoutInflater mInflater;
    
    public ChurchAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public List<Map<String, String>> getList() {
        return list;
    }

    public void setList(List<Map<String, String>> list) {
        this.list = list;
        notifyDataSetChanged();
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

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String,String> item = list.get(position);
        holder.icon.setImageResource(R.drawable.church1);
        holder.nom.setText(item.get(Church.NOM));
        holder.paroisse.setText(item.get(Church.PAROISSE));
        holder.commune.setText(item.get(Church.CP) +  " " + item.get(Church.COMMUNE));
        return convertView;
    }
    
    private static class ViewHolder {
        ImageView icon;
        TextView nom;
        TextView paroisse;
        TextView commune;
    }

}

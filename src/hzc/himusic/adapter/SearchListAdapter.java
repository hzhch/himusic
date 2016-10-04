package hzc.himusic.adapter;

import hzc.himusic.R;
import hzc.himusic.entity.Music;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchListAdapter extends BaseAdapter {
	private Context context;
	private List<Music> musics;
	private LayoutInflater inflater;

	public SearchListAdapter(Context context, List<Music> musics) {
		this.context = context;
		this.musics = musics;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return musics.size();
	}

	@Override
	public Music getItem(int position) {
		return musics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.item_lv_search_result, null);
			holder = new ViewHolder();
			holder.tvName =(TextView) convertView.findViewById(R.id.tvName);
			holder.tvSinger = (TextView) convertView.findViewById(R.id.tvSinger);
			convertView.setTag(holder);
		}
		holder=(ViewHolder) convertView.getTag();
		//…Ë÷√≤Œ ˝
		Music m = getItem(position);
		holder.tvName.setText(m.getTitle());
		holder.tvSinger.setText(m.getAuthor());
		return convertView;
	}

	class ViewHolder{
		TextView tvName;
		TextView tvSinger;
	}
}

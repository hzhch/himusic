package hzc.himusic.adapter;

import hzc.himusic.R;
import hzc.himusic.entity.Music;
import hzc.himusic.util.ImageLoader;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter {
	private Context context;
	private List<Music> musics;
	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	
	
	public MusicAdapter(Context context, List<Music> musics, ListView listView) {
		this.context = context;
		this.musics = musics;
		this.inflater = LayoutInflater.from(context);
		this.imageLoader = new ImageLoader(context, listView);
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
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_lv_music, null);
			holder = new ViewHolder();
			holder.ivAlbum = (ImageView) convertView.findViewById(R.id.ivAlbum);
			holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			holder.tvSinger = (TextView) convertView.findViewById(R.id.tvSinger);
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		Music m = getItem(position);
		// 设置控件的参数
		holder.tvName.setText(m.getTitle());
		holder.tvSinger.setText(m.getAuthor());
		//使用imageLoader批量加载图片
		imageLoader.displayImage(m.getPic_small(), holder.ivAlbum);
		
		
		return convertView;
	}

	/**
	 * 停止线程
	 */
	public void stopThread(){
		imageLoader.stopThread();
	}
	
	class ViewHolder {
		ImageView ivAlbum;
		TextView tvName;
		TextView tvSinger;
	}


}

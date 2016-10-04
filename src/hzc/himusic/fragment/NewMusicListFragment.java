package hzc.himusic.fragment;

import hzc.himusic.R;
import hzc.himusic.adapter.MusicAdapter;
import hzc.himusic.app.MusicApplication;
import hzc.himusic.entity.Music;
import hzc.himusic.entity.SongInfo;
import hzc.himusic.entity.SongUrl;
import hzc.himusic.model.MusicListCallback;
import hzc.himusic.model.MusicModel;
import hzc.himusic.model.SongInfoCallback;
import hzc.himusic.service.PlayMusicService.MusicBinder;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 承载新歌榜列表
 */
public class NewMusicListFragment extends Fragment{
	private MusicModel model;
	private List<Music> musics;
	private ListView listView;
	private MusicAdapter adapter;
	private MusicBinder musicBinder;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_list, null);
		listView=(ListView) view.findViewById(R.id.listView);
		//添加监听
		setListeners();
		//调用业务层代码 查询新歌榜列表
		model = new MusicModel();
		model.loadNewMusicList(0, 20, new MusicListCallback() {
			public void onMusicListLoaded(List<Music> musics) {
				NewMusicListFragment.this.musics = musics;
				setAdapter();
			}
		});
		return view;
	}
	
	//添加监听
	private void setListeners() {
		//滚动事件
		listView.setOnScrollListener(new OnScrollListener() {
			boolean atBottom = false;
			boolean requestSending = false;
			//当滚动状态改变时执行 
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_FLING:
					//Log.i("info", "SCROLL_STATE_FLING");
					break;
				case OnScrollListener.SCROLL_STATE_IDLE:
					//Log.i("info", "SCROLL_STATE_IDLE");
					if(atBottom){
						if(requestSending){
							return;
						}
						//Log.i("info", "真的...到底了......");
						//向后继续查询后续20条
						requestSending = true;
						model.loadNewMusicList(musics.size(), 20, new MusicListCallback() {
							public void onMusicListLoaded(List<Music> musics) {
								requestSending = false;
								if(musics.isEmpty()){
									Toast.makeText(getActivity(), "没有了", Toast.LENGTH_SHORT).show();
									return;
								}
								//把新查出来的数据添加到adapter使用的数据源中
								NewMusicListFragment.this.musics.addAll(musics);
								//更新Adapter
								adapter.notifyDataSetChanged();
							}
						});
					}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					//Log.i("info", "SCROLL_STATE_TOUCH_SCROLL");
					break;
				}
				
			}
			//每当listView滚动时，执行频率非常高
			public void onScroll(AbsListView view, 
					int firstVisibleItem, 
					int visibleItemCount, 
					int totalItemCount) {
				//Log.i("info", "执行了：onScroll......");
				if(firstVisibleItem+visibleItemCount==totalItemCount){
					atBottom=true;
				}else{
					atBottom=false;
				}
			}
		});
		
		//item点击事件
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//把当前播放列表和position存入Application
				MusicApplication app = MusicApplication.getApp();
				app.setMusicList(musics);
				app.setPosition(position);
				Log.i("info", "postion:"+position);
				final Music m=musics.get(position);
				String songId = m.getSong_id();
				//加载音乐的基本信息  调用业务层发送请求
				model.loadSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						if(urls==null || info ==null){
							Toast.makeText(getActivity(), "音乐信息加载失败", Toast.LENGTH_SHORT).show();
							return;
						}
						//把urls与songInfo对象存入当前音乐对象
						m.setUrls(urls);
						m.setInfo(info);
						//没有加载失败  开始准备播放音乐
						SongUrl url = urls.get(0);
						musicBinder.playMusic(url.getFile_link());
					}
				});
			}
		});
	}

	//设置适配器
	public void setAdapter(){
		//自定义Adapter   给listView设置适配器
		adapter = new MusicAdapter(getActivity(), musics, listView);
		listView.setAdapter(adapter);
	}

	/**
	 * 当承载当前Fragment的Activity执行onDestory
	 * 时也会级联执行Fragment.onDestroy()方法
	 */
	public void onDestroy() {
		super.onDestroy();
		adapter.stopThread();
	}

	public void setBinder(MusicBinder musicBinder) {
		this.musicBinder = musicBinder;
	}
	
}




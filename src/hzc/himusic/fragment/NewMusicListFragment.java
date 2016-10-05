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
 * �����¸���б�
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
		//��Ӽ���
		setListeners();
		//����ҵ������ ��ѯ�¸���б�
		model = new MusicModel();
		model.loadNewMusicList(0, 20, new MusicListCallback() {
			public void onMusicListLoaded(List<Music> musics) {
				NewMusicListFragment.this.musics = musics;
				setAdapter();
			}
		});
		return view;
	}
	
	//��Ӽ���
	private void setListeners() {
		//�����¼�
		listView.setOnScrollListener(new OnScrollListener() {
			boolean atBottom = false;
			boolean requestSending = false;
			//������״̬�ı�ʱִ�� 
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
						//Log.i("info", "���...������......");
						//��������ѯ����20��
						requestSending = true;
						model.loadNewMusicList(musics.size(), 20, new MusicListCallback() {
							public void onMusicListLoaded(List<Music> musics) {
								requestSending = false;
								if(musics.isEmpty()){
									Toast.makeText(getActivity(), "û����", Toast.LENGTH_SHORT).show();
									return;
								}
								//���²������������ӵ�adapterʹ�õ�����Դ��
								NewMusicListFragment.this.musics.addAll(musics);
								//����Adapter
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
			//ÿ��listView����ʱ��ִ��Ƶ�ʷǳ���
			public void onScroll(AbsListView view, 
					int firstVisibleItem, 
					int visibleItemCount, 
					int totalItemCount) {
				//Log.i("info", "ִ���ˣ�onScroll......");
				if(firstVisibleItem+visibleItemCount==totalItemCount){
					atBottom=true;
				}else{
					atBottom=false;
				}
			}
		});
		
		//item����¼�
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//�ѵ�ǰ�����б��position����Application
				MusicApplication app = MusicApplication.getApp();
				app.setMusicList(musics);
				app.setPosition(position);
				Log.i("info", "postion:"+position);
				final Music m=musics.get(position);
				String songId = m.getSong_id();
				//�������ֵĻ�����Ϣ  ����ҵ��㷢������
				model.loadSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						if(urls==null || info ==null){
							Toast.makeText(getActivity(), "������Ϣ����ʧ��", Toast.LENGTH_SHORT).show();
							return;
						}
						//��urls��songInfo������뵱ǰ���ֶ���
						m.setUrls(urls);
						m.setInfo(info);
						//û�м���ʧ��  ��ʼ׼����������
						SongUrl url = urls.get(0);
						musicBinder.playMusic(url.getFile_link());
					}
				});
			}
		});
	}

	//����������
	public void setAdapter(){
		//�Զ���Adapter   ��listView����������
		adapter = new MusicAdapter(getActivity(), musics, listView);
		listView.setAdapter(adapter);
	}

	/**
	 * �����ص�ǰFragment��Activityִ��onDestory
	 * ʱҲ�ἶ��ִ��Fragment.onDestroy()����
	 */
	public void onDestroy() {
		super.onDestroy();
		adapter.stopThread();
	}

	public void setBinder(MusicBinder musicBinder) {
		this.musicBinder = musicBinder;
	}
	
}




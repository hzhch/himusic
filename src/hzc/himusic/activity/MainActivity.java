package hzc.himusic.activity;

import hzc.himusic.R;
import hzc.himusic.adapter.SearchListAdapter;
import hzc.himusic.app.MusicApplication;
import hzc.himusic.entity.Music;
import hzc.himusic.entity.SongInfo;
import hzc.himusic.entity.SongUrl;
import hzc.himusic.fragment.HotMusicListFragment;
import hzc.himusic.fragment.NewMusicListFragment;
import hzc.himusic.model.LrcCallback;
import hzc.himusic.model.MusicModel;
import hzc.himusic.model.SearchResultCallback;
import hzc.himusic.model.SongInfoCallback;
import hzc.himusic.service.DownloadService;
import hzc.himusic.service.PlayMusicService;
import hzc.himusic.service.PlayMusicService.MusicBinder;
import hzc.himusic.util.BitmapCallback;
import hzc.himusic.util.BitmapUtils;
import hzc.himusic.util.GlobalConsts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	private RadioGroup radioGroup;
	private RadioButton radioNew;
	private RadioButton radioHot;
	private ViewPager viewPager;
	private ImageView ivCMPic;
	private TextView tvCMTitle;
	private RelativeLayout relativePlayMusic;
	private TextView tvPMTitle, tvPMSinger, tvPMLrc, tvPMCurrentTime, tvPMTotalTime;
	private ImageView ivPMAlbum, ivPMBackground;
	private SeekBar seekBar;
	private RelativeLayout relativeSearchMusic;
	private Button btnSearch;
	private Button btnCancel;
	private EditText etSearch;
	private ListView lvSearchResult;
	private ArrayList<Fragment> fragments;
	private MainPagerAdapter pagerAdapter;
	private ServiceConnection conn;
	private MusicInfoReceiver receiver;
	private MusicBinder musicBinder;
	private MusicModel model;
	protected List<Music> searchMusicList;
	private SearchListAdapter searchAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//��ʼ���ؼ�
		setViews();
		//����������
		setAdapter();
		//���ü�����
		setListeners();
		//ע�����
		registComponents();
	}
	
	/**
	 * ��Service  �� ע������Ȳ���
	 */
	private void registComponents() {
		//ע��㲥������
		receiver = new MusicInfoReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_MUSIC_STARTED);
		filter.addAction(GlobalConsts.ACTION_UPDATE_PROGRESS);
		this.registerReceiver(receiver, filter);
		
		//��Service
		Intent intent = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {
			//�����쳣�Ͽ�
			public void onServiceDisconnected(ComponentName name) {
			}
			//���ӳɹ�
			public void onServiceConnected(ComponentName name, IBinder binder) {
				musicBinder = (MusicBinder) binder;
				//��musicBinder��������Fragment
				NewMusicListFragment f = (NewMusicListFragment) fragments.get(0);
				f.setBinder(musicBinder);
				HotMusicListFragment f2 = (HotMusicListFragment) fragments.get(1);
				f2.setBinder(musicBinder);
				
			}
		};
		this.bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	/**
	 * ���ü�����
	 */
	private void setListeners() {
		//�����б�ļ���
		lvSearchResult.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//����������б��ɵ�ǰ�����б�
				//���浽Application
				MusicApplication.getApp().setMusicList(searchMusicList);
				MusicApplication.getApp().setPosition(position);
				
				final Music m=searchMusicList.get(position);
				String songId=m.getSong_id();
				model.loadSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						m.setUrls(urls);
						m.setInfo(info);
						//��ʼ��������
						if(urls==null || info==null){
							return;
						}
						String musicPath = m.getUrls().get(0).getFile_link();
						musicBinder.playMusic(musicPath);
					}
				});
			}
		});
		
		//relativePlayMusic�Լ������¼�
		relativePlayMusic.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		//seekBar�����ק����  
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){ //�û�������seekbar�Ľ���
					int position=seekBar.getProgress();
					musicBinder.seekTo(position);
				}
			}
		});
		//viewPager����radioGroup
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			//��ҳ�汻ѡ��ʱ ִ��
			public void onPageSelected(int index) {
				switch (index) {
				case 0:
					radioNew.setChecked(true);
					break;
				case 1:
					radioHot.setChecked(true);
					break;
				}
			}
			//ÿ����viewpager��������ִ��  ִ��Ƶ�ʷǳ���
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		//radioGroup����viewpager
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioNew: //ѡ�����¸�
					viewPager.setCurrentItem(0);
					break;
				case R.id.radioHot: //ѡ�����ȸ��
					viewPager.setCurrentItem(1);
					break;
				}
			}
		});
	}

	/**
	 * ��Ӽ���
	 * @param view
	 */
	public void doClick(View view){
		switch (view.getId()) {
		case R.id.btnCancel: //�����ȡ��
			relativeSearchMusic.setVisibility(View.INVISIBLE);
			TranslateAnimation anim3 = new TranslateAnimation(0, 0, 0, -relativeSearchMusic.getHeight());
			anim3.setDuration(350);
			relativeSearchMusic.startAnimation(anim3);
			break;
		case R.id.btnSearch: //��������
			searchMusic();
			break;
		case R.id.btnToSearch: //ȥ���� ��ʾ��������
			relativeSearchMusic.setVisibility(View.VISIBLE);
			TranslateAnimation anim2 = new TranslateAnimation(0, 0, -relativeSearchMusic.getHeight(), 0);
			anim2.setDuration(350);
			relativeSearchMusic.startAnimation(anim2);
			break;
		case R.id.ivCMPic: //���Բ��ͼ�� �������Ž���
			//ִ��ƽ�ƶ��� ��ʾrelativePlayMusic
			relativePlayMusic.setVisibility(View.VISIBLE);
//			TranslateAnimation anim = new TranslateAnimation(0, 0, relativePlayMusic.getHeight(), 0);
			ScaleAnimation anim = new ScaleAnimation(0.1f, 1f, 0.1f, 1f, 0, relativePlayMusic.getHeight());
			anim.setDuration(380);
			anim.setInterpolator(new BounceInterpolator());
			relativePlayMusic.startAnimation(anim);
			break;
		}
	}
	
	/**
	 * ���ivPMAlbumִ������ҵ��
	 * @param view
	 */
	public void download(View view){
		final Music m = MusicApplication.getApp().getCurrentMusic();
		//�������ְ汾ѡ���alertDialog
		final List<SongUrl> urls = m.getUrls();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String[] items = new String[urls.size()];
		for(int i=0; i<items.length; i++){
			int size = Integer.parseInt(urls.get(i).getFile_size());
			items[i] = Math.floor(100.0*size/1024/1024)/100+"M";
		}
		builder.setItems(items , new OnClickListener() {
			public void onClick(DialogInterface dialog, int position) {
				SongUrl url = urls.get(position);
				Log.i("info", "ѡ��İ汾��"+url);
				String musicUrl = url.getFile_link();
				if(musicUrl.equals("")){
					musicUrl = url.getShow_link();
				}
				if(musicUrl.equals("")){
					Toast.makeText(MainActivity.this, "��û�л�ȡ����Ȩ, �Ե�...", Toast.LENGTH_SHORT).show();
					return;
				}
				//����ִ������
				Intent intent = new Intent(MainActivity.this, DownloadService.class);
				intent.putExtra("url", musicUrl);
				intent.putExtra("bit", url.getFile_bitrate());
				intent.putExtra("title", m.getTitle());
				intent.putExtra("total", url.getFile_size());
				startService(intent);
				
			}
		});
		builder.create().show();
	}
	
	/**
	 * ��������
	 */
	public void searchMusic(){
		String keyword = etSearch.getText().toString();
		if(keyword.trim().equals("")){
			return;
		}
		//ִ������ҵ��  ����model�ķ��� 
		model.searchMusicList(keyword, new SearchResultCallback() {
			public void onResultLoaded(List<Music> musics) {
				MainActivity.this.searchMusicList = musics;
				setSearchListAdapter();
			}
		});
	}

	/**
	 * ���������б��������
	 */
	public void setSearchListAdapter(){
		searchAdapter = new SearchListAdapter(this, searchMusicList);
		lvSearchResult.setAdapter(searchAdapter);
	}
	
	/**
	 * �������ֲ��ŵļ���  ��һ������һ������ͣ������
	 * @param view
	 */
	public void controllMusic(View view){
		switch (view.getId()) {
		case R.id.ivPMPre:
			//ʹ��application�ķ����л�����һ�׸�
			final Music m=MusicApplication.getApp().preMusic();
			List<SongUrl> urls = m.getUrls();
			if(urls!=null){ //��ǰ�Ѿ����ع��˻���������ϢsongInfo
				String path=m.getUrls().get(0).getFile_link();
				musicBinder.playMusic(path);
			}else{ //��ǰû�м��ع������ֵ�songInfo
				model.loadSongInfoBySongId(m.getSong_id(), new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						m.setUrls(urls);
						m.setInfo(info);
						String path=m.getUrls().get(0).getFile_link();
						musicBinder.playMusic(path);
					}
				});
			}
			break;
		case R.id.ivPMNext:
			//ʹ��Application�ķ����л�����һ�׸�
			final Music m2=MusicApplication.getApp().nextMusic();
			urls = m2.getUrls();
			if(urls!=null){ //��ǰ�Ѿ����ع��˻���������ϢsongInfo
				String path=m2.getUrls().get(0).getFile_link();
				musicBinder.playMusic(path);
			}else{ //��ǰû�м��ع������ֵ�songInfo
				model.loadSongInfoBySongId(m2.getSong_id(), new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						m2.setUrls(urls);
						m2.setInfo(info);
						String path=m2.getUrls().get(0).getFile_link();
						musicBinder.playMusic(path);
					}
				});
			}
			break;
		case R.id.ivPMStart:
			musicBinder.startOrPause();

			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if(relativePlayMusic.getVisibility() == View.VISIBLE){
			//ִ��һ������  ��relativeLatyout�ջ�ȥ
			TranslateAnimation anim = new TranslateAnimation(0, 0, 0, relativePlayMusic.getHeight());
			anim.setDuration(350);
			relativePlayMusic.startAnimation(anim);
			relativePlayMusic.setVisibility(View.INVISIBLE);
			
			
		}else{
			super.onBackPressed();
		}
	}
	
	/**
	 * ����������
	 */
	private void setAdapter() {
		//׼��Fragment����Դ   List<Fragment>
		fragments = new ArrayList<Fragment>();
		fragments.add(new NewMusicListFragment());
		fragments.add(new HotMusicListFragment());
		pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
	}

	/**
	 * �ؼ���ʼ�� 
	 */
	private void setViews() {
		model = new MusicModel();
		
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		radioNew = (RadioButton) findViewById(R.id.radioNew);
		radioHot = (RadioButton) findViewById(R.id.radioHot);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		
		ivCMPic = (ImageView) findViewById(R.id.ivCMPic);
		tvCMTitle = (TextView) findViewById(R.id.tvCMTitle);
	
		relativePlayMusic = (RelativeLayout) findViewById(R.id.relativePlayMusic);
		tvPMTitle = (TextView) findViewById(R.id.tvPMTitle);
		tvPMSinger = (TextView) findViewById(R.id.tvPMSinger);
		tvPMLrc = (TextView) findViewById(R.id.tvPMLrc);
		tvPMCurrentTime = (TextView) findViewById(R.id.tvPMCurrentTime);
		tvPMTotalTime = (TextView) findViewById(R.id.tvPMTotalTime);
		ivPMAlbum = (ImageView) findViewById(R.id.ivPMAlbum);
		ivPMBackground = (ImageView) findViewById(R.id.ivPMBackground);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		
		relativeSearchMusic = (RelativeLayout) findViewById(R.id.relativeSearchMusic);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		etSearch = (EditText) findViewById(R.id.etSearch);
		lvSearchResult = (ListView) findViewById(R.id.lvSearchResult);
	}

	
	@Override
	protected void onDestroy() {
		//���Service
		this.unbindService(conn);
		//����㲥������
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/**
	 * viewpager��������
	 */
	class MainPagerAdapter extends FragmentPagerAdapter{
		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		public Fragment getItem(int position) {
			return fragments.get(position);
		}
		public int getCount() {
			return fragments.size();
		}
	}
	
	/**
	 * ��д���ڽ���������Ϣ��صĹ㲥������
	 */
	class MusicInfoReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(GlobalConsts.ACTION_UPDATE_PROGRESS)){
				//���յ��������ֽ��ȹ㲥
				int current = intent.getIntExtra("current", 0);
				int total = intent.getIntExtra("total", 0);
				seekBar.setMax(total);
				seekBar.setProgress(current);
				String currentTime=GlobalConsts.SDF.format(new Date(current));
				tvPMCurrentTime.setText(currentTime);
				tvPMTotalTime.setText(GlobalConsts.SDF.format(new Date(total)));
				//˳����¸����Ϣ
				Music m = MusicApplication.getApp().getCurrentMusic();
				HashMap<String, String> lrc = m.getLrc();
				if(lrc!=null){  //����Ѿ��������
					//���ݵ�ǰʱ�� ��ȡ�������
					String content=lrc.get(currentTime);
					if(content!=null){
						tvPMLrc.setText(content);
					}
				}
			}else if(GlobalConsts.ACTION_MUSIC_STARTED.equals(action)){
				//�����Ѿ���ʼ���� 
				final Music m = MusicApplication.getApp().getCurrentMusic();
				//���µײ���   ivCMPic  tvCMTitle
				String path = m.getPic_small();
				if(path==null || path.equals("")){ //����·��
					path = m.getInfo().getPic_big();
					path = path.equals("") ? m.getInfo().getAlbum_500_500() : path;
				}
				String title = m.getTitle();
				tvCMTitle.setText(title);
				//����ͼƬ
				BitmapUtils.loadBitmap(path, new BitmapCallback(){
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap!=null){
							ivCMPic.setImageBitmap(bitmap);
							//ִ����ת����
							RotateAnimation anim = new RotateAnimation(0, 360, ivCMPic.getWidth()/2, ivCMPic.getHeight()/2);
							//����ѭ��
							anim.setRepeatCount(Animation.INFINITE);
							anim.setDuration(10000);
							anim.setInterpolator(new LinearInterpolator());
							ivCMPic.startAnimation(anim);
						}else{
							ivCMPic.setImageResource(R.drawable.ic_launcher);
						}
					}
				});
				
				//����ivPMAlbum
				String albumPath = m.getInfo().getAlbum_500_500();
				BitmapUtils.loadBitmap(albumPath, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap!=null){
							ivPMAlbum.setImageBitmap(bitmap);
						}else{
							ivPMAlbum.setImageResource(R.drawable.default_music_pic);
						}
					}
				});
				//����ivPMBackground  
				String bgPath = m.getInfo().getArtist_480_800();
				if(bgPath.equals("")){
					bgPath = m.getInfo().getArtist_640_1136();
				}
				if(bgPath.equals("")){
					bgPath = m.getInfo().getAlbum_500_500();
				}
				BitmapUtils.loadBitmap(bgPath,  8 , new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap!=null){
							//ģ��������
							BitmapUtils.loadBlurBitmap(bitmap, 5, new BitmapCallback() {
								public void onBitmapLoaded(Bitmap bitmap) {
									ivPMBackground.setImageBitmap(bitmap);
								}
							});
						}else{
							ivPMBackground.setImageResource(R.drawable.default_music_background);
						}
					}
				});
				//����tvPMTitle  tvPMSinger
				tvPMTitle.setText(m.getTitle());
				tvPMSinger.setText(m.getAuthor());
				//���ظ��
				model.loadLrc(m.getInfo().getLrclink(), new LrcCallback() {
					public void onLrcLoaded(HashMap<String, String> lrc) {
						m.setLrc(lrc);
					}
				});
			}
		}
	}
}





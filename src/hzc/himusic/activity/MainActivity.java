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
		//初始化控件
		setViews();
		//设置适配器
		setAdapter();
		//设置监听器
		setListeners();
		//注册组件
		registComponents();
	}
	
	/**
	 * 绑定Service  或 注册组件等操作
	 */
	private void registComponents() {
		//注册广播接收器
		receiver = new MusicInfoReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_MUSIC_STARTED);
		filter.addAction(GlobalConsts.ACTION_UPDATE_PROGRESS);
		this.registerReceiver(receiver, filter);
		
		//绑定Service
		Intent intent = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {
			//连接异常断开
			public void onServiceDisconnected(ComponentName name) {
			}
			//连接成功
			public void onServiceConnected(ComponentName name, IBinder binder) {
				musicBinder = (MusicBinder) binder;
				//把musicBinder交给两个Fragment
				NewMusicListFragment f = (NewMusicListFragment) fragments.get(0);
				f.setBinder(musicBinder);
				HotMusicListFragment f2 = (HotMusicListFragment) fragments.get(1);
				f2.setBinder(musicBinder);
				
			}
		};
		this.bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	/**
	 * 设置监听器
	 */
	private void setListeners() {
		//搜索列表的监听
		lvSearchResult.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//把搜索结果列表当成当前播放列表
				//保存到Application
				MusicApplication.getApp().setMusicList(searchMusicList);
				MusicApplication.getApp().setPosition(position);
				
				final Music m=searchMusicList.get(position);
				String songId=m.getSong_id();
				model.loadSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						m.setUrls(urls);
						m.setInfo(info);
						//开始播放音乐
						if(urls==null || info==null){
							return;
						}
						String musicPath = m.getUrls().get(0).getFile_link();
						musicBinder.playMusic(musicPath);
					}
				});
			}
		});
		
		//relativePlayMusic自己消费事件
		relativePlayMusic.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		//seekBar添加拖拽监听  
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){ //用户更改了seekbar的进度
					int position=seekBar.getProgress();
					musicBinder.seekTo(position);
				}
			}
		});
		//viewPager控制radioGroup
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			//当页面被选中时 执行
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
			//每当当viewpager滚动都会执行  执行频率非常高
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		//radioGroup控制viewpager
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioNew: //选中了新歌
					viewPager.setCurrentItem(0);
					break;
				case R.id.radioHot: //选中了热歌榜
					viewPager.setCurrentItem(1);
					break;
				}
			}
		});
	}

	/**
	 * 添加监听
	 * @param view
	 */
	public void doClick(View view){
		switch (view.getId()) {
		case R.id.btnCancel: //点击了取消
			relativeSearchMusic.setVisibility(View.INVISIBLE);
			TranslateAnimation anim3 = new TranslateAnimation(0, 0, 0, -relativeSearchMusic.getHeight());
			anim3.setDuration(350);
			relativeSearchMusic.startAnimation(anim3);
			break;
		case R.id.btnSearch: //搜索音乐
			searchMusic();
			break;
		case R.id.btnToSearch: //去搜索 显示搜索界面
			relativeSearchMusic.setVisibility(View.VISIBLE);
			TranslateAnimation anim2 = new TranslateAnimation(0, 0, -relativeSearchMusic.getHeight(), 0);
			anim2.setDuration(350);
			relativeSearchMusic.startAnimation(anim2);
			break;
		case R.id.ivCMPic: //点击圆形图标 弹出播放界面
			//执行平移动画 显示relativePlayMusic
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
	 * 点击ivPMAlbum执行下载业务
	 * @param view
	 */
	public void download(View view){
		final Music m = MusicApplication.getApp().getCurrentMusic();
		//弹出音乐版本选择的alertDialog
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
				Log.i("info", "选择的版本："+url);
				String musicUrl = url.getFile_link();
				if(musicUrl.equals("")){
					musicUrl = url.getShow_link();
				}
				if(musicUrl.equals("")){
					Toast.makeText(MainActivity.this, "还没有获取到版权, 稍等...", Toast.LENGTH_SHORT).show();
					return;
				}
				//可以执行下载
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
	 * 搜索音乐
	 */
	public void searchMusic(){
		String keyword = etSearch.getText().toString();
		if(keyword.trim().equals("")){
			return;
		}
		//执行搜索业务  调用model的方法 
		model.searchMusicList(keyword, new SearchResultCallback() {
			public void onResultLoaded(List<Music> musics) {
				MainActivity.this.searchMusicList = musics;
				setSearchListAdapter();
			}
		});
	}

	/**
	 * 设置搜索列表的适配器
	 */
	public void setSearchListAdapter(){
		searchAdapter = new SearchListAdapter(this, searchMusicList);
		lvSearchResult.setAdapter(searchAdapter);
	}
	
	/**
	 * 控制音乐播放的监听  上一曲、下一曲、暂停、播放
	 * @param view
	 */
	public void controllMusic(View view){
		switch (view.getId()) {
		case R.id.ivPMPre:
			//使用application的方法切换到上一首歌
			final Music m=MusicApplication.getApp().preMusic();
			List<SongUrl> urls = m.getUrls();
			if(urls!=null){ //以前已经加载过了基本音乐信息songInfo
				String path=m.getUrls().get(0).getFile_link();
				musicBinder.playMusic(path);
			}else{ //以前没有加载过这音乐的songInfo
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
			//使用Application的方法切换到下一首歌
			final Music m2=MusicApplication.getApp().nextMusic();
			urls = m2.getUrls();
			if(urls!=null){ //以前已经加载过了基本音乐信息songInfo
				String path=m2.getUrls().get(0).getFile_link();
				musicBinder.playMusic(path);
			}else{ //以前没有加载过这音乐的songInfo
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
			//执行一个动画  把relativeLatyout收回去
			TranslateAnimation anim = new TranslateAnimation(0, 0, 0, relativePlayMusic.getHeight());
			anim.setDuration(350);
			relativePlayMusic.startAnimation(anim);
			relativePlayMusic.setVisibility(View.INVISIBLE);
			
			
		}else{
			super.onBackPressed();
		}
	}
	
	/**
	 * 设置适配器
	 */
	private void setAdapter() {
		//准备Fragment数据源   List<Fragment>
		fragments = new ArrayList<Fragment>();
		fragments.add(new NewMusicListFragment());
		fragments.add(new HotMusicListFragment());
		pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
	}

	/**
	 * 控件初始化 
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
		//解绑Service
		this.unbindService(conn);
		//解除广播接收器
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/**
	 * viewpager的适配器
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
	 * 编写用于接收音乐信息相关的广播接收器
	 */
	class MusicInfoReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(GlobalConsts.ACTION_UPDATE_PROGRESS)){
				//接收到更新音乐进度广播
				int current = intent.getIntExtra("current", 0);
				int total = intent.getIntExtra("total", 0);
				seekBar.setMax(total);
				seekBar.setProgress(current);
				String currentTime=GlobalConsts.SDF.format(new Date(current));
				tvPMCurrentTime.setText(currentTime);
				tvPMTotalTime.setText(GlobalConsts.SDF.format(new Date(total)));
				//顺便更新歌词信息
				Music m = MusicApplication.getApp().getCurrentMusic();
				HashMap<String, String> lrc = m.getLrc();
				if(lrc!=null){  //歌词已经加载完毕
					//根据当前时间 获取歌词内容
					String content=lrc.get(currentTime);
					if(content!=null){
						tvPMLrc.setText(content);
					}
				}
			}else if(GlobalConsts.ACTION_MUSIC_STARTED.equals(action)){
				//音乐已经开始播放 
				final Music m = MusicApplication.getApp().getCurrentMusic();
				//更新底部栏   ivCMPic  tvCMTitle
				String path = m.getPic_small();
				if(path==null || path.equals("")){ //换个路径
					path = m.getInfo().getPic_big();
					path = path.equals("") ? m.getInfo().getAlbum_500_500() : path;
				}
				String title = m.getTitle();
				tvCMTitle.setText(title);
				//更新图片
				BitmapUtils.loadBitmap(path, new BitmapCallback(){
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap!=null){
							ivCMPic.setImageBitmap(bitmap);
							//执行旋转动画
							RotateAnimation anim = new RotateAnimation(0, 360, ivCMPic.getWidth()/2, ivCMPic.getHeight()/2);
							//无限循环
							anim.setRepeatCount(Animation.INFINITE);
							anim.setDuration(10000);
							anim.setInterpolator(new LinearInterpolator());
							ivCMPic.startAnimation(anim);
						}else{
							ivCMPic.setImageResource(R.drawable.ic_launcher);
						}
					}
				});
				
				//更新ivPMAlbum
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
				//更新ivPMBackground  
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
							//模糊化处理
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
				//更新tvPMTitle  tvPMSinger
				tvPMTitle.setText(m.getTitle());
				tvPMSinger.setText(m.getAuthor());
				//加载歌词
				model.loadLrc(m.getInfo().getLrclink(), new LrcCallback() {
					public void onLrcLoaded(HashMap<String, String> lrc) {
						m.setLrc(lrc);
					}
				});
			}
		}
	}
}





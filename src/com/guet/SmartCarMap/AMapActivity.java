package com.guet.SmartCarMap;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.mapapi.core.AMapException;
import com.amap.mapapi.core.GeoPoint;
import com.amap.mapapi.core.PoiItem;
import com.amap.mapapi.geocoder.Geocoder;
import com.amap.mapapi.map.MapActivity;
import com.amap.mapapi.map.MapController;
import com.amap.mapapi.map.MapView;
import com.amap.mapapi.map.MyLocationOverlay;
import com.amap.mapapi.map.Overlay;
import com.amap.mapapi.map.PoiOverlay;
import com.amap.mapapi.map.RouteMessageHandler;
import com.amap.mapapi.map.RouteOverlay;
import com.amap.mapapi.poisearch.PoiPagedResult;
import com.amap.mapapi.poisearch.PoiSearch;
import com.amap.mapapi.poisearch.PoiSearch.Query;
import com.amap.mapapi.poisearch.PoiSearch.SearchBound;
import com.amap.mapapi.poisearch.PoiTypeDef;
import com.amap.mapapi.route.Route;
import com.guet.SmartCarSystem.R;

public class AMapActivity extends MapActivity implements RouteMessageHandler{

	private MapView mMapView;
	private MapController mMapController;
	private GeoPoint point;
	private ImageButton mainLocationCallBack;
	private ImageButton mainCurrentLayer;
	private MyLocationOverlay mMyLocationOverlay;
	private boolean isTraffic = false;// ����ʵʱ·��
	private GetLocation mGetLocation;
	private Location mLocation;
	private String addressName;	
	private Geocoder coder;
	private TextView tvSearch;
	private String query="";
	private PoiPagedResult result;
	private PoiOverlay poiOverlay;
	private TextView startTextView,endTextView;
	private Button drivingButton,transitButton,walkButton;
	private MapPointOverlay overlay;
	private String poiType;
	private GeoPoint startPoint=null;
	private GeoPoint endPoint=null;
	private int mode = Route.BusDefault;
	private ImageButton startImageButton,endImageButton;
	private ImageButton routeSearchImagebtn;
	private String strStart,strEnd;
	private ProgressDialog progDialog;
	private PoiPagedResult startSearchResult;
	private PoiPagedResult endSearchResult;
	private List<Route> routeResult;
	private RouteOverlay ol;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ����ȫ��ģʽ,ȥ��������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.mapview);
		initView();
		initData();
		initListener();
	}
	
	// ��ʼ����ͼ
	private void initView() {
		mMapView = (MapView) findViewById(R.id.mapView);
		mainLocationCallBack = (ImageButton) findViewById(R.id.main_location_callback);
		mainCurrentLayer = (ImageButton) findViewById(R.id.main_current_layer);
		tvSearch = (TextView) findViewById(R.id.toplayout_textview_search);
		startTextView = (AutoCompleteTextView) findViewById(R.id.autotextview_roadsearch_start);
		startTextView.setSelectAllOnFocus(true);	// Set the TextView so that when it takes focus, all the text is selected.
		endTextView = (AutoCompleteTextView) findViewById(R.id.autotextview_roadsearch_goals);
		endTextView.setSelectAllOnFocus(true);
		drivingButton = (Button) findViewById(R.id.imagebtn_roadsearch_tab_driving);
		transitButton = (Button) findViewById(R.id.imagebtn_roadsearch_tab_transit);
		walkButton = (Button) findViewById(R.id.imagebtn_roadsearch_tab_walk);
		startImageButton = (ImageButton) findViewById(R.id.imagebtn_roadsearch_startoption);
		endImageButton = (ImageButton) findViewById(R.id.imagebtn_roadsearch_goalsoption);
		routeSearchImagebtn = (ImageButton) findViewById(R.id.imagebtn_roadsearch_search);
	}
	// ��ʼ������
	private void initData() {
		coder = new Geocoder(this);
		overlay=new MapPointOverlay(this);
		mMapView.setBuiltInZoomControls(true); // ���������������ſؼ�
		mMapController = mMapView.getController(); // �õ�mMapView����Ȩ,�����������ƺ�����ƽ�ƺ�����
		point = new GeoPoint((int) (25.305158 * 1E6), (int) (110.339729 * 1E6)); // �ø����ľ�γ�ȹ���һ��GeoPoint��
		mMapController.setCenter(point);		//�������ĵ�����
		mMapController.setZoom(12);				//�������ż���
		mMyLocationOverlay = new MyLocationOverlay(AMapActivity.this, mMapView);
	}
	// ��ʼ��������
	private void initListener() {
		// �����ť���Զ���λ
		mainLocationCallBack.setOnClickListener(new MyOnclickListener());
		// ��ʾ��ͼͼ��
		mainCurrentLayer.setOnClickListener(new MyOnclickListener());
		// ����
		tvSearch.setOnClickListener(new MyOnclickListener());
		// �Լ�Ѱ��
		drivingButton.setOnClickListener(new MyOnclickListener());
		// ����Ѱ��
		transitButton.setOnClickListener(new MyOnclickListener());
		// ����
		walkButton.setOnClickListener(new MyOnclickListener());
		// ���������ť
		startImageButton.setOnClickListener(new MyOnclickListener());
		// �յ�������ť
		endImageButton.setOnClickListener(new MyOnclickListener());
		// ������ť
		routeSearchImagebtn.setOnClickListener(new MyOnclickListener());
	}
	// ��������¼�
	public class MyOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
				case R.id.main_location_callback:	// �ص���ǰλ��
					mMyLocationOverlay.enableMyLocation();	//���Կ���MyLocation���ܣ�����LocationManager.GPS_PROVIDER ��
																				//  LocationManager.NETWORK_PROVIDERע����¡�
					mMyLocationOverlay.enableCompass(); // ����ָ������¹��ܡ�
					mMapView.getOverlays().add(mMyLocationOverlay);	//��Ӹ�������MapView��
					// ʵ�ֳ��ζ�λʹ��λ���������ʾ
					mMyLocationOverlay.runOnFirstFix(new Runnable() {
						public void run() {
							getCurrentLocation();	//��ȡ��ǰλ�� 
							handler.sendMessage(Message.obtain(handler,Constants.FIRST_LOCATION));
						}
					});
					break;
				case R.id.main_current_layer:		// ͼ��
					showDialog(Constants.DIALOG_LAYER);
					break;
				case R.id.toplayout_textview_search:	//����
					onSearchRequested();
					break;
				case R.id.imagebtn_roadsearch_tab_driving:	// �Լ�
					mode = Route.DrivingDefault;
					drivingButton.setBackgroundResource(R.drawable.mode_driving_on);
					transitButton.setBackgroundResource(R.drawable.mode_transit_off);
					break;
				case R.id.imagebtn_roadsearch_tab_transit:	//����
					mode = Route.BusDefault;
					drivingButton.setBackgroundResource(R.drawable.mode_driving_off);
					transitButton.setBackgroundResource(R.drawable.mode_transit_on);
					break;
				case R.id.imagebtn_roadsearch_tab_walk:		//����
					showToast("�ݲ�֧�ֲ��й滮");
					break;
				case R.id.imagebtn_roadsearch_startoption:
					showToast("�ڵ�ͼ�ϵ���������");
					poiType="startPoint";
					mMapView.getOverlays().add(overlay);
					break;
				case R.id.imagebtn_roadsearch_goalsoption:
					showToast("�ڵ�ͼ�ϵ�������յ�");
					poiType="endPoint";
					mMapView.getOverlays().add(overlay);
					break;
				case R.id.imagebtn_roadsearch_search:		
					strStart = startTextView.getText().toString().trim();
					strEnd = endTextView.getText().toString().trim();
					if (strStart==null||strStart.length()==0) {
						Toast.makeText(AMapActivity.this, "��ѡ�����",
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (strEnd==null||strEnd.length()==0) {
						Toast.makeText(AMapActivity.this, "��ѡ���յ�",
								Toast.LENGTH_SHORT).show();
						return;
					}
					
					startSearchResult();
					break;
			}
		}
		
	}
	
	// ��ѯ·���滮���
	public void startSearchResult() {
		strStart = startTextView.getText().toString().trim();
		if(startPoint!=null&&strStart.equals("��ͼ�ϵĵ�")){
			endSearchResult();
		}else{
			final Query startQuery = new Query(strStart, PoiTypeDef.All, "010");
			progDialog = ProgressDialog.show(AMapActivity.this, null,
					"����������������Ϣ...", true, true);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					// ��������POI����
					PoiSearch poiSearch = new PoiSearch(AMapActivity.this, startQuery); // ���������ַ���
					try {
						startSearchResult = poiSearch.searchPOI();
						if(progDialog.isShowing()){
							handler.sendMessage(Message.obtain(handler,
									Constants.ROUTE_START_SEARCH));
					}
					} catch (AMapException e) {
						Message msg = new Message();
						msg.what = Constants.ROUTE_SEARCH_ERROR;
						msg.obj =  e.getErrorMessage();
						handler.sendMessage(msg);
					} 
				}

			});
			t.start();
		}
	} 
	
	// ��ѯ·���滮�յ�
	public void endSearchResult() {
		
		strEnd = endTextView.getText().toString().trim();
		if(endPoint!=null&&strEnd.equals("��ͼ�ϵĵ�")){
			searchRouteResult(startPoint,endPoint);
		}else{
			final Query endQuery = new Query(strEnd, PoiTypeDef.All, "010");
	        progDialog = ProgressDialog.show(AMapActivity.this, null,
					"����������������Ϣ...", true, false);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					PoiSearch poiSearch = new PoiSearch(AMapActivity.this,endQuery); // ���������ַ���
					try {
						endSearchResult = poiSearch.searchPOI();
						if(progDialog.isShowing()){
						 handler.sendMessage(Message.obtain(handler,
								Constants.ROUTE_END_SEARCH));
						}
					} catch (AMapException e) {
						Message msg = new Message();
						msg.what = Constants.ROUTE_SEARCH_ERROR;
						msg.obj =  e.getErrorMessage();
						handler.sendMessage(msg);
					} 
				}

			});
			t.start();
		}
	}
	
	public void searchRouteResult(GeoPoint startPoint, GeoPoint endPoint) {
		progDialog = ProgressDialog.show(AMapActivity.this, null,
				"���ڻ�ȡ��·", true, true);
		final Route.FromAndTo fromAndTo = new Route.FromAndTo(startPoint,
				endPoint);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					routeResult = Route.calculateRoute(AMapActivity.this,
							fromAndTo, mode);
					if(progDialog.isShowing()){
						if(routeResult!=null||routeResult.size()>0)
						handler.sendMessage(Message
								.obtain(handler, Constants.ROUTE_SEARCH_RESULT));
					}
				} catch (AMapException e) {
					Message msg = new Message();
					msg.what = Constants.ROUTE_SEARCH_ERROR;
					msg.obj =  e.getErrorMessage();
					handler.sendMessage(msg);
				}
			}
		});
		t.start();

	}
	
	public void showToast(String showString) {
		Toast.makeText(getApplicationContext(), showString, Toast.LENGTH_SHORT)
				.show();
	}
	
	@Override
	protected void onNewIntent(final Intent newIntent) {
		super.onNewIntent(newIntent);
		String ac = newIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(ac)) {
			//doSearchQuery(newIntent);
		}
	}
	
	/*protected void doSearchQuery(Intent intent) {
		query = intent.getStringExtra(SearchManager.QUERY);
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				AMapActivity.this, MySuggestionProvider.AUTHORITY,
				MySuggestionProvider.MODE);
		suggestions.saveRecentQuery(query, null);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					PoiSearch poiSearch = new PoiSearch(AMapActivity.this,
							new PoiSearch.Query(query, PoiTypeDef.All, "010")); // ���������ַ�����"010Ϊ��������"
					poiSearch.setBound(new SearchBound(mMapView));//�ڵ�ǰ��ͼ��ʾ��Χ�ڲ���
					poiSearch.setPageSize(10);//��������ÿ����෵�ؽ����
					result = poiSearch.searchPOI();
					handler.sendMessage(Message.obtain(handler,
							Constants.POISEARCH));
				} catch (AMapException e) {
					handler.sendMessage(Message.obtain(handler,
							Constants.ERROR));
					e.printStackTrace();
				}
			}
		});
		t.start();
	}*/

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		this.mMyLocationOverlay.disableCompass();
		this.mMyLocationOverlay.disableMyLocation();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		this.mMyLocationOverlay.enableCompass();
		this.mMyLocationOverlay.enableMyLocation();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		removeDialog(Constants.DIALOG_LAYER);
		removeDialog(Constants.DIALOG_GET_LOCATION_ADDRESS);
		removeDialog(Constants.DIALOG_GET_LOCATION_POION);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constants.FIRST_LOCATION) {
				mMapController.animateTo(mMyLocationOverlay.getMyLocation());
			}else if (msg.what == Constants.REOCODER_RESULT) {
				showToast(addressName);
			}else if(msg.what == Constants.ERROR){
				showToast("�������������Ƿ���ȷ?");
			}else if(msg.what == Constants.POISEARCH){
				try {
					if (result != null) {
						List<PoiItem> poiItems = result.getPage(1);
						if (poiItems != null && poiItems.size() > 0) {
							mMapController.setZoom(13);
							mMapController
									.animateTo(poiItems.get(0).getPoint());
							if (poiOverlay != null) {
								poiOverlay.removeFromMap();
							}
							Drawable drawable = getResources().getDrawable(
									R.drawable.da_marker_red);
//							poiOverlay = new MyPoiOverlay(PoiSearchDemo.this,
//									drawable, poiItems); // ������ĵ�һҳ��ӵ�PoiOverlay
							poiOverlay = new PoiOverlay(drawable, poiItems);
							poiOverlay.addToMap(mMapView); // ��poiOverlay��ע�ڵ�ͼ��
							poiOverlay.showPopupWindow(0);
							return;
						}
					}
					showToast("����ؽ����");
				} catch (AMapException e) {
					showToast("�������Ӵ���");
				}
			}else if (msg.what == Constants.ROUTE_START_SEARCH) {
				progDialog.dismiss();
				try {
					List<PoiItem> poiItems;
					if (startSearchResult != null && (poiItems = startSearchResult.getPage(1)) != null 
							&& poiItems.size() > 0) {
						RouteSearchPoiDialog dialog = new RouteSearchPoiDialog(
								AMapActivity.this, poiItems);
						dialog.setTitle("��Ҫ�ҵ������:");
						dialog.show();
						dialog.setOnListClickListener(new RouteSearchPoiDialog.OnListItemClick() {
							@Override
							public void onListItemClick(
									RouteSearchPoiDialog dialog,
									PoiItem startpoiItem) {
								startPoint = startpoiItem.getPoint();
								strStart = startpoiItem.getTitle();
								startTextView.setText(strStart);
								endSearchResult();
							}

						});
					} else {
						showToast("�����������,���������趨...");
					}
				} catch (AMapException e) {
					e.printStackTrace();
				}

			} else if (msg.what == Constants.ROUTE_END_SEARCH) {
				progDialog.dismiss();
				try {
					List<PoiItem> poiItems;
					if (endSearchResult != null && (poiItems = endSearchResult.getPage(1)) != null 
							&& poiItems.size() > 0) {
						RouteSearchPoiDialog dialog = new RouteSearchPoiDialog(
								AMapActivity.this, poiItems);
						dialog.setTitle("��Ҫ�ҵ��յ���:");
						dialog.show();
						dialog.setOnListClickListener(new RouteSearchPoiDialog.OnListItemClick() {
							@Override
							public void onListItemClick(
									RouteSearchPoiDialog dialog,
									PoiItem endpoiItem) {
								// TODO Auto-generated method stub
								endPoint = endpoiItem.getPoint();
								strEnd = endpoiItem.getTitle();
								endTextView.setText(strEnd);
								searchRouteResult(startPoint, endPoint);
							}

						});
					} else {
						showToast("�����������,���������趨...");
					}
				} catch (AMapException e) {
					e.printStackTrace();
				}

			} else if (msg.what == Constants.ROUTE_SEARCH_RESULT) {
				progDialog.dismiss();
				if (routeResult != null && routeResult.size()>0) {
					Route route = routeResult.get(0);
					if (route != null) {
						if (ol != null) {
							ol.removeFromMap(mMapView);
						}
						ol = new RouteOverlay(AMapActivity.this, route);
						ol.registerRouteMessage(AMapActivity.this); // ע����Ϣ������
						ol.addToMap(mMapView); // ���뵽��ͼ
						ArrayList<GeoPoint> pts = new ArrayList<GeoPoint>();
						pts.add(route.getLowerLeftPoint());
						pts.add(route.getUpperRightPoint());
						mMapView.getController().setFitView(pts);//������ͼ��ʾ��Χ
						mMapView.invalidate();
					}
				}
			} else if (msg.what == Constants.ROUTE_SEARCH_ERROR) {
				progDialog.dismiss();
				showToast((String)msg.obj);
			}
		}

	};
	
	public class MapPointOverlay extends Overlay{
	    private Context context;
	    private LayoutInflater inflater;
	    private View popUpView;
	    public MapPointOverlay(Context context){
	    	this.context=context;
	    	inflater = (LayoutInflater)context.getSystemService(
	    	        Context.LAYOUT_INFLATER_SERVICE);
	    }
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			// TODO Auto-generated method stub
			super.draw(canvas, mapView, shadow);                
		}

		@Override
		public boolean onTap(final GeoPoint point, final MapView view) {
			if(popUpView!=null){
				view.removeView(popUpView);
			}
		   // Projection�ӿ�������Ļ���ص�����ϵͳ�͵�����澭γ�ȵ�����ϵͳ֮��ı任
		    popUpView=inflater.inflate(R.layout.popup, null);
			TextView textView=(TextView) popUpView.findViewById(R.id.PoiName);
			textView.setText("�������ѡ��˵�");
			MapView.LayoutParams lp;
			lp = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
					MapView.LayoutParams.WRAP_CONTENT,
					point,0,0,
					MapView.LayoutParams.BOTTOM_CENTER);
				view.addView(popUpView,lp);
			popUpView.setOnClickListener(new OnClickListener() {	
				@Override
				public void onClick(View v) {
					if(poiType.equals("startPoint")){
						startTextView.setText("��ͼ�ϵĵ�");
//						startTextView.selectAll();
						startPoint = point;
					}
					
					if(poiType.equals("endPoint")){
						endTextView.setText("��ͼ�ϵĵ�");
//						endTextView.selectAll();
						endPoint = point;
					}
					
					view.removeView(popUpView);
					view.getOverlays().remove(overlay);
				}
			});
	        return super.onTap(point, view);
		}
	}
	
	//��ȡ��ǰλ��Location
	private void getCurrentLocation() {
		mGetLocation = new GetLocation(AMapActivity.this);
		mLocation = mGetLocation.getLocation();
		if(mLocation != null){
			Double longitude = mLocation.getLongitude();
			Double latitude = mLocation.getLatitude();
			getAddress(latitude, longitude);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		View view;
		switch (id) {
		case Constants.DIALOG_LAYER:	//��ʾʵʱ·��
			String[] traffic = { getResources().getString(
					R.string.real_time_traffic) };
			boolean[] traffic_falg = new boolean[] { isTraffic };
			return new AlertDialog.Builder(AMapActivity.this)
					.setTitle(R.string.choose_layer)
					.setMultiChoiceItems(traffic, traffic_falg,
							new DialogInterface.OnMultiChoiceClickListener() {

								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {

									if (which == 0) {
										if (isChecked) {
											mMapView.setTraffic(true);// ��ʾʵʱ·��
										} else {
											mMapView.setTraffic(false);// �ر�ʵʱ·��
										}
										isTraffic = isChecked;
									}
									mMapView.postInvalidate();
									dismissDialog(Constants.DIALOG_LAYER);
								}
							})
					.setPositiveButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dismissDialog(Constants.DIALOG_LAYER);
								}
							}).create();
		case Constants.DIALOG_GET_LOCATION_ADDRESS:	
			view = getLayoutInflater().inflate(R.layout.dialog_get_location_adress, null);
			final EditText dialogInputLatitude = (EditText) view.findViewById(R.id.dialog_input_latitude);
			final EditText dialogInputLongitude = (EditText) view.findViewById(R.id.dialog_input_longitude);
			return new AlertDialog.Builder(AMapActivity.this)
								.setIcon(android.R.drawable.ic_dialog_info)
								.setTitle("ͨ����γ�Ȳ�ѯ��ַ")
								.setView(view)
								.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										Double latitude = Double.parseDouble(dialogInputLatitude.getText().toString());
										Double longitude = Double.parseDouble(dialogInputLongitude.getText().toString());
										AMapActivity.this.getAddress(latitude, longitude);
									}
								})
								.setNegativeButton("ȡ��", null)
								.create();
			
		case Constants.DIALOG_GET_LOCATION_POION:
			view = getLayoutInflater().inflate(R.layout.dialog_get_location_point, null);
			final EditText dialogInputAddress = (EditText) view.findViewById(R.id.dialog_input_address);
			return new AlertDialog.Builder(AMapActivity.this)
								.setIcon(android.R.drawable.ic_dialog_info)
								.setTitle("ͨ����ַ��ѯ��γ��")
								.setView(view)
								.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										String etInput = dialogInputAddress.getText().toString();
										dialogInputAddress.setText("");
										AMapActivity.this.getLatlon(etInput);
										
									}
								})
								.setNegativeButton("ȡ��", null)
								.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, Constants.SEARCH_POI, 1, getString(R.string.search_poi));
		menu.add(1, Constants.SEARCH_TRANSIT, 0, getString(R.string.search_transit));
		menu.add(1, Constants.EXIT_MAP, 1, getString(R.string.exit_map));
		Menu file = menu.addSubMenu(0,Constants.SEARCH_LOCATION,0,getString(R.string.search_location));
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, file);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.getLocationMessage:
				//ͨ����γ�Ȳ�ѯ��ַ
				showDialog(Constants.DIALOG_GET_LOCATION_ADDRESS);
				break;
			case R.id.getLocationPoint:
				//ͨ����ַ��ѯ��γ��
				showDialog(Constants.DIALOG_GET_LOCATION_POION);
				break;
			case Constants.SEARCH_POI:
				LinearLayout l = (LinearLayout) findViewById(R.id.toplayout_search_frame);
				if(l.getVisibility() == View.GONE){
					l.setVisibility(View.VISIBLE);
				}else{
					l.setVisibility(View.GONE);
				}
				break;
			case Constants.SEARCH_TRANSIT:
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.RelativeLayout_roadsearch_top);
				if(rl.getVisibility() == View.GONE){
					rl.setVisibility(View.VISIBLE);
				}else{
					rl.setVisibility(View.GONE);
				}
				break;
			case Constants.EXIT_MAP:
				finish();
				break;
		}
		return true;
	}
	
	//��������
	public void getLatlon(final String name){
		Thread t = new Thread(new Runnable() 
		{
			public void run()
			{
				try {
					List<Address> address = coder.getFromLocationName(name, 3);
					if (address != null && address.size()>0) {
						Address addres = address.get(0);
						addressName=addres.getLatitude()
								+ "," + addres.getLongitude();
						handler.sendMessage(Message
								.obtain(handler, Constants.REOCODER_RESULT));
						
					}
				} catch (AMapException e) {
					handler.sendMessage(Message
							.obtain(handler, Constants.ERROR));
				}
		
			}
		});
		t.start();
	}
	
	//�������
	public void getAddress(final double mlat,final double mLon){
		Thread t = new Thread(new Runnable() 
		{
			public void run()
			{
				try {
					List<Address> address = coder.getFromLocation(mlat,
							mLon, 3);
					if (address != null && address.size()>0) {
						Address addres = address.get(0);
						addressName=addres.getAdminArea()
								+ addres.getSubLocality() + addres.getFeatureName()
								+ "����";
						handler.sendMessage(Message
								.obtain(handler, Constants.REOCODER_RESULT));
						
					}
				} catch (AMapException e) {
					// TODO Auto-generated catch block
					handler.sendMessage(Message
							.obtain(handler, Constants.ERROR));
				}
		
			}
		});
		t.start();
	}

	
	// RouteOverlay�϶������д���
	@Override
	public void onDrag(MapView mapView, RouteOverlay overlay, int index,
			GeoPoint pos) {
	}

	
	// RouteOverlay�϶���ʼʱ����
	@Override
	public void onDragBegin(MapView mapView, RouteOverlay overlay, int index,
		GeoPoint pos) {
	}

	
	// RouteOverlay�϶���ɴ���
	@Override
	public void onDragEnd(MapView mapView, RouteOverlay overlay, int index,
			GeoPoint pos) {
		try {
			startPoint = overlay.getStartPos();
			endPoint = overlay.getEndPos();
//			overlay.renewOverlay(mapView);
			searchRouteResult(startPoint, endPoint);
		} catch (IllegalArgumentException e) {
			ol.restoreOverlay(mMapView);
			overlayToBack(ol, mMapView);
		} catch (Exception e1) {
			overlay.restoreOverlay(mMapView);
			overlayToBack(ol, mMapView);
		}
	}
	
	private void overlayToBack(RouteOverlay overlay, MapView mapView) {
		startPoint = overlay.getStartPos();
		endPoint = overlay.getEndPos();
	}
	

	@Override
	public boolean onRouteEvent(MapView arg0, RouteOverlay arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		return false;
	}
}
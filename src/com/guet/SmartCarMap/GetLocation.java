package com.guet.SmartCarMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**
 * 获取当前的Location
 */
public class GetLocation {
	private LocationManager lmanager;
	private Context context;
	private Location location;

	public GetLocation(Context context) {
		lmanager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		this.context = context;
	}

	/**
	 *  根据wifi获取当前位置
	 * @param context
	 */
	public void getCurrentLocationWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			location = lmanager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
	}

	/**
	 *  根据基站获取当前的位置
	 */
	public void getCurrentLocationAGPS() {
		TelephonyManager tmanager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tmanager.getCellLocation() == null) {
			return;
		}
		GsmCellLocation gcl = (GsmCellLocation) tmanager.getCellLocation();

		int cid = gcl.getCid();

		int lac = gcl.getLac();
		String oper=tmanager.getNetworkOperator();
		int mcc = 0;
		int mnc = 0;
		if(!oper.equals("")){
			mcc = Integer.valueOf(oper.substring(0,3));
			mnc = Integer.valueOf(oper.substring(3,5));
		}
		try {

			// 组装JSON查询字符串

			JSONObject holder = new JSONObject();

			holder.put("version", "1.1.0");

			holder.put("host", "maps.google.com");

			// holder.put("address_language", "zh_CN");

			holder.put("request_address", true);

			JSONArray array = new JSONArray();

			JSONObject data = new JSONObject();
			data.put("cell_id", cid); // 25070
			data.put("location_area_code", lac);// 4474
			data.put("mobile_country_code", mcc);// 460

			data.put("mobile_network_code", mnc);// 0
			array.put(data);

			holder.put("cell_towers", array);

			// 创建连接，发送请求并接受回应

			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://www.google.com/loc/json");

			StringEntity se = new StringEntity(holder.toString());

			post.setEntity(se);

			HttpResponse resp = client.execute(post);

			HttpEntity entity = resp.getEntity();

			BufferedReader br = new BufferedReader(

			new InputStreamReader(entity.getContent()));

			StringBuffer resultStr = new StringBuffer();

			String readLine = null;

			while ((readLine = br.readLine()) != null) {

				resultStr.append(readLine);

			}

			JSONObject jsonResult = new JSONObject(resultStr.toString());
			JSONObject jsonLocation = jsonResult.getJSONObject("location");
			double jsonLat = jsonLocation.getDouble("latitude");
			double jsonLon = jsonLocation.getDouble("longitude");

			location = new Location("AGPS");
			location.setLatitude(jsonLat);
			location.setLongitude(jsonLon);

		} catch (Exception e) {
			Log.i("life", "location get failed");
		}
	}

	/**
	 *  根据gps获得当前位置
	 */
	public void getCurrentLocationGPS() {
//		Criteria criteria = new Criteria();
//		criteria.setAccuracy(Criteria.ACCURACY_FINE);
//		criteria.setAltitudeRequired(false);
//		criteria.setBearingRequired(false);
//		criteria.setCostAllowed(true);
//		criteria.setPowerRequirement(Criteria.POWER_LOW);
//		String locationProvider = lmanager.getBestProvider(criteria, true);	
//		location = lmanager.getLastKnownLocation(locationProvider);
		String locationProvider = LocationManager.GPS_PROVIDER;
        location = lmanager.getLastKnownLocation(locationProvider); 	
	}
	/**
	 * 返回location
	 * @return Location
	 */
	public Location getLocation(){
		getCurrentLocationGPS();
		if(location!=null){
			Log.i("life", "GPS");
			return location;
		}
		else{
			getCurrentLocationWifi(context);
			if(location!=null){
				Log.i("life", "Wifi");
				return location;
			}
			else {
				getCurrentLocationAGPS();
				if(location!=null){
					Log.i("life", "AGPS");
					return location;
				}
			}
		}
		return null;
	}
}

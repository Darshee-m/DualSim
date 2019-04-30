package com.example.networkmaps;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;



public class MainActivity extends AppCompatActivity implements LocationListener{



	private static final String TAG="MainActivity";
	private static final int ERROR_DAILOG_REQUEST=9001;
	public Button pos;
	public TextView locationText, tv;
	Button btnAdd;
	Button gettt;
	LocationManager locationManager;
	Double latitude;
	Double longitude;
	FirebaseDatabase database ;
	DatabaseReference myRef ;
	Button getLocationBtn;
	Button getMapBtn;
	TextView providerText,strengthText;
	SimpleDateFormat simpleDateFormat;
	Calendar c ;
	String currentTime;
	boolean isDualSIM;
	String provider_def, provider1, provider2;
	int st1, st2;
	double lat=0.0,longi=0.0;
	private static final int PERMISSION_ACCESS_COURSE_LOCATION = 0;


	Data data_obj1, data_obj2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);

		String imeiSIM1 = telephonyInfo.getImsiSIM1();
		String imeiSIM2 = telephonyInfo.getImsiSIM2();

		boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
		boolean isSIM2Ready = telephonyInfo.isSIM2Ready();
		isDualSIM = telephonyInfo.isDualSIM();

		tv = (TextView) findViewById(R.id.tv);
		tv.setText(" IME1 : " + imeiSIM1 + "\n" +
				" IME2 : " + imeiSIM2 + "\n" +
				" IS DUAL SIM : " + isDualSIM + "\n" +
				" IS SIM1 READY : " + isSIM1Ready + "\n" +
				" IS SIM2 READY : " + isSIM2Ready + "\n");

		if (isServiceOK()) {
			init();
		}
		getLocationBtn = (Button)findViewById(R.id.getLocationBtn);
		locationText = (TextView)findViewById(R.id.locationText);
		providerText=(TextView)findViewById(R.id.providerText);
		strengthText=(TextView)findViewById(R.id.strengthText);

		Context context=this;
		getProvider(context);
		providerText.setText("Default provider: " + provider_def);


		getLocationBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getLocation();
			}
		});

		database = FirebaseDatabase.getInstance();
		myRef=database.getReference("data");

	}
	void getLocation() {
		try {
			Toast.makeText(this, "Searching for location", Toast.LENGTH_SHORT).show();
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
		}
		catch(SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		locationText.setText("Current Location: " + location.getLatitude() + ", " + location.getLongitude());
		lat=  location.getLatitude();
		longi=location.getLongitude();
		//providerText.setText("provider: " + provider);
		String info="blank";
		info="Current Location: " + location.getLatitude() + ", " + location.getLongitude();
		Context context=this;
		getCellSignalStrength(context);
		//String strength=String.valueOf(st)
		strengthText.setText("Current Strength: " + String.valueOf(st1));

		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		c = Calendar.getInstance();
		currentTime = simpleDateFormat.format(c.getTime());
		data_obj1= new Data(currentTime, lat, longi, provider1, st1 );
		String id1= myRef.push().getKey();
		myRef.child(id1).setValue(data_obj1);
		if(isDualSIM){
			String id2= myRef.push().getKey();
			data_obj2= new Data(currentTime, lat, longi, provider2, st2 );
			myRef.child(id2).setValue(data_obj2);
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}
	public void getProvider(Context context)
	{
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		provider_def= manager.getNetworkOperatorName();
		provider1 = getOutput(getApplicationContext(), "getCarrierName", 0);
		provider2 = getOutput(getApplicationContext(), "getCarrierName", 1);
	}

	private static String getOutput(Context context, String methodName,
									int slotId) {
		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		Class<?> telephonyClass;
		String reflectionMethod = null;
		String output = null;
		try {
			telephonyClass = Class.forName(telephony.getClass().getName());
			for (Method method : telephonyClass.getMethods()) {
				String name = method.getName();
				if (name.contains(methodName)) {
					Class<?>[] params = method.getParameterTypes();
					if (params.length == 1 && params[0].getName().equals("int")) {
						reflectionMethod = name;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (reflectionMethod != null) {
			try {
				output = getOpByReflection(telephony, reflectionMethod, slotId,
						false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return output;
	}

	private static String getOpByReflection(TelephonyManager telephony,
											String predictedMethodName, int slotID, boolean isPrivate) {

		// Log.i("Reflection", "Method: " + predictedMethodName+" "+slotID);
		String result = null;

		try {

			Class<?> telephonyClass = Class.forName(telephony.getClass()
					.getName());

			Class<?>[] parameter = new Class[1];
			parameter[0] = int.class;
			Method getSimID;
			if (slotID != -1) {
				if (isPrivate) {
					getSimID = telephonyClass.getDeclaredMethod(
							predictedMethodName, parameter);
				} else {
					getSimID = telephonyClass.getMethod(predictedMethodName,
							parameter);
				}
			} else {
				if (isPrivate) {
					getSimID = telephonyClass
							.getDeclaredMethod(predictedMethodName);
				} else {
					getSimID = telephonyClass.getMethod(predictedMethodName);
				}
			}

			Object ob_phone;
			Object[] obParameter = new Object[1];
			obParameter[0] = slotID;
			if (getSimID != null) {
				if (slotID != -1) {
					ob_phone = getSimID.invoke(telephony, obParameter);
				} else {
					ob_phone = getSimID.invoke(telephony);
				}

				if (ob_phone != null) {
					result = ob_phone.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Log.i("Reflection", "Result: " +  e.printStackTrace());
			return null;
		}

		return result;
	}

	public void getCellSignalStrength(Context context) {
		int strength1=0, strength2 = 0;
		//Context ct;
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
					PERMISSION_ACCESS_COURSE_LOCATION);
		} else {

			List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile


			if (cellInfos != null && cellInfos.size() > 0) {
				for (int i = 0; i < cellInfos.size(); i++) {
					if (cellInfos.get(i) instanceof CellInfoWcdma) {
						CellInfoWcdma cellInfoWcdma1 = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
						CellSignalStrengthWcdma cellSignalStrengthWcdma1 = cellInfoWcdma1.getCellSignalStrength();
						strength1 = cellSignalStrengthWcdma1.getDbm();
						if(isDualSIM) {
							CellInfoWcdma cellInfoWcdma2 = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(1);
							CellSignalStrengthWcdma cellSignalStrengthWcdma2 = cellInfoWcdma2.getCellSignalStrength();
							strength2 = cellSignalStrengthWcdma2.getDbm();
							Toast.makeText(getBaseContext(), "file saved", Toast.LENGTH_SHORT).show();
						}
						break;
					} else if (cellInfos.get(i) instanceof CellInfoGsm) {
						CellInfoGsm cellInfogsm1 = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
						CellSignalStrengthGsm cellSignalStrengthGsm1 = cellInfogsm1.getCellSignalStrength();
						strength1 = cellSignalStrengthGsm1.getDbm();
						if(isDualSIM) {
							CellInfoGsm cellInfogsm2 = (CellInfoGsm) telephonyManager.getAllCellInfo().get(1);
							CellSignalStrengthGsm cellSignalStrengthGsm2 = cellInfogsm2.getCellSignalStrength();
							strength2 = cellSignalStrengthGsm2.getDbm();
						}
						break;
					} else if (cellInfos.get(i) instanceof CellInfoLte) {
						CellInfoLte cellInfoLte1 = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
						CellSignalStrengthLte cellSignalStrengthLte1 = cellInfoLte1.getCellSignalStrength();
						strength1 = cellSignalStrengthLte1.getDbm();
						if(isDualSIM) {
							CellInfoLte cellInfoLte2 = (CellInfoLte) telephonyManager.getAllCellInfo().get(1);
							CellSignalStrengthLte cellSignalStrengthLte2 = cellInfoLte2.getCellSignalStrength();
							strength2 = cellSignalStrengthLte2.getDbm();
						}
						break;
					}
				}
			}
		}
		//txt.setText("hello");
		st1=strength1;
		st2= strength2;


	}

	public void init(){

		Button btmMap=(Button)findViewById(R.id.getMapBtn);
		btmMap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,MapActivity.class);
				startActivity(intent);

			}
		});
	}

	public boolean isServiceOK(){
		Log.d(TAG, "isServiceOK: checking google services version");
		int available= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
		if(available== ConnectionResult.SUCCESS){
			return true;
		}else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
			Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DAILOG_REQUEST);
			dialog.show();
		}
		else{
			Toast.makeText(this, "YOu cant make map request", Toast.LENGTH_SHORT).show();
		}
		return false;
	}


}

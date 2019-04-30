package com.example.networkmaps;
import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{
		Data data;
	FirebaseDatabase database ;
	DatabaseReference myRef ;
	List dataList;
    @Override
    public void onMapReady(GoogleMap googleMap) {
		Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
		gmap=googleMap;
		MarkerOptions markerOptions = new MarkerOptions();

		LatLng india=new LatLng(19,73);
		float zoomlevel=(float)10.0;
		gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(india,zoomlevel));
	}

    public static final String FINE_LOACTION=Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION=Manifest.permission.ACCESS_COARSE_LOCATION;
    //vars
    private Boolean mLocation =false;
    public  GoogleMap gmap;


    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getLocationPermission();
        dataList=new ArrayList();
//        btn=(Button)findViewById(R.id.next);
		database = FirebaseDatabase.getInstance();
		myRef=database.getReference("data");
    }



//	public void nextt(View v){
//		LatLng india=new LatLng(19.2020801,73.1610635);
//		CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
//				india, 15);
//		gmap.animateCamera(location);
//		Toast.makeText(this, "next clicked", Toast.LENGTH_SHORT).show();
//		float zoomlevel=(float)18.0;
//		gmap.addMarker(new MarkerOptions()
//				.position(new LatLng(19.2020801,73.1610635))
//				.title("fuck world").snippet("here i am").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//
//
//	}

	public void nextt(View v){
		LatLng india=new LatLng(19.2020801,73.1610635);
		CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
				india, 15);
		gmap.animateCamera(location);
		Toast.makeText(this, "next clicked", Toast.LENGTH_SHORT).show();
		float zoomlevel=(float)18.0;
		gmap.addMarker(new MarkerOptions()
				.position(new LatLng(19.2020801,73.1610635))
				.title("hello world"));


	}

	@Override
	protected void onStart() {
		super.onStart();
		myRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			dataList.clear();
				for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
					Data data=dataSnapshot1.getValue(Data.class);
					LatLng mr=new LatLng(data.lat,data.longi);
					String title="Strength:"+data.strength;
					String provider=data.provider;
					String color;
					if(data.strength>-70){
						gmap.addMarker(new MarkerOptions()
								.position(mr)
								.title(title)
								.snippet(provider).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
					}
					else if(data.strength<-90){
						gmap.addMarker(new MarkerOptions()
								.position(mr)
								.title(title)
								.snippet(provider).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
					}
					else{

						gmap.addMarker(new MarkerOptions()
								.position(mr)
								.title(title)
								.snippet(provider).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
					}

				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	public void initMap(){

		SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(MapActivity.this);
    }


    private void getLocationPermission(){

        String[] permissions= {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                COURSE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    FINE_LOACTION) == PackageManager.PERMISSION_GRANTED) {
                mLocation = true;
                initMap();

            } else {
                ActivityCompat.requestPermissions(this, permissions, 1234);
            }
        }
             else {
                ActivityCompat.requestPermissions(this, permissions, 1234);
            }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocation=false;
        switch(requestCode){
            case 1234:{
                if(grantResults.length>0){
                	for(int i=0;i<grantResults.length;i++){
                		if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                			mLocation=false;
                			return;
						}
					}

					mLocation=true;
				}
                //initialize our map
                initMap();
            }
        }
    }


}

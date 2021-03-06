package com.sharedcab.batchcar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class PickupFragment extends Fragment {

	GoogleMap googleMap;
	LatLng old_target;
	Address pickup = new Address(new Locale("en","INDIA"));
    LatLng center;
    Context mainContext;
    MainActivity mainActivity;
    
    View rootView;
    MapFragment pickupMapFragment;
    FragmentManager fm;
	
    public PickupFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public void onAttach(Activity a){
    	super.onAttach(a);
    	mainContext = a;
    	mainActivity = (MainActivity) a;    
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    }
    
   
    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        Log.d("OnResume View", "Sucess");
    }
  
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.activity_pickup, container, false);
        setUpMapIfNeeded();
      
        Button button = (Button) rootView.findViewById(R.id.btn_next);
        button.setBackgroundColor(Color.rgb(4,180,4));
	    button.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	if(pickup==null || pickup.getMaxAddressLineIndex() == -1){
	                Toast.makeText(mainContext, "Please select a pickup location!", Toast.LENGTH_SHORT).show();
	            }
	        	else{
	        		String full_address = "";
	        		for(int i = 0; i < pickup.getMaxAddressLineIndex()-1;i++){
	        			full_address += pickup.getAddressLine(i);
	        		}
	        	    CustomAddress c = new CustomAddress(full_address, 
	        	    		Double.toString(pickup.getLatitude()), Double.toString(pickup.getLongitude()) );
		        	mainActivity.pickup.setAddressString(c.getAddressString());
		        	mainActivity.pickup.setLatitude(c.getLatitude());
		        	mainActivity.pickup.setLongitude(c.getLongitude());
		        	Fragment dropFragment = new DropFragment();
		        	FragmentTransaction ft  = getFragmentManager().beginTransaction();
		        	ft.replace(R.id.content_frame,dropFragment);
		        	ft.addToBackStack(null);
		        	ft.commit();
		        	
		        }
	        }
	    });
	    TextView pickup_address = (TextView) rootView.findViewById(R.id.tv_pickup);
        pickup_address.setText("");
        return rootView;
    }
    
    @Override
  	public void onActivityCreated(Bundle savedInstanceState) {
  		super.onActivityCreated(savedInstanceState);
        mainContext = getActivity();
    }
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_pickup))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                Toast.makeText(mainContext,"Calling set up map!" , Toast.LENGTH_SHORT).show();    
                setUpMap();
            }
        }
    }    
    
    private void setUpMap(){
    	if(googleMap==null)
            Toast.makeText(mainContext,"No Map!" , Toast.LENGTH_SHORT).show();
		else{
            Toast.makeText(mainContext,"Ye! We have map!" , Toast.LENGTH_SHORT).show();			
	        //we get the google map here
	        googleMap.setMyLocationEnabled(true);
	        googleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(19.1167, 72.8333),14.0f) );
	        center = googleMap.getCameraPosition().target;
	        googleMap.getUiSettings().setRotateGesturesEnabled(false);
	        googleMap.setOnCameraChangeListener(mOnCameraChangeListener);
		}
    }
    
  
            
    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<LatLng, Void, List<Address>>{
 
        @Override
        protected List<Address> doInBackground(LatLng... cur_center) {
            Geocoder geocoder = new Geocoder(mainContext);
            List<Address> addresses = null;
            try {
            	addresses = geocoder.getFromLocation(cur_center[0].latitude,cur_center[0].longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }
        @Override
        protected void onPreExecute(){

        }
        
        @Override
        protected void onPostExecute(List<Address> addresses) {
 
            if(addresses==null || addresses.size()==0){
                Toast.makeText(mainContext, "No Location found", Toast.LENGTH_SHORT).show();
            }
            else{
            	pickup = addresses.get(0);
            	String full_address = "";
        		for(int i = 0; i < pickup.getMaxAddressLineIndex()-1;i++){
        			full_address += pickup.getAddressLine(i);
        		}
                TextView pickup_address = (TextView) rootView.findViewById(R.id.tv_pickup); 
                CharSequence c = full_address;
                pickup_address.setText(c);
                old_target = googleMap.getCameraPosition().target;                	
            } 
        }
    }
    
    
    private final OnCameraChangeListener mOnCameraChangeListener = 
            new OnCameraChangeListener() {

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            LatLng x = googleMap.getCameraPosition().target;
            new GeocoderTask().execute(x);
        }
    };
    
}
package net.cascadingstyle.android.lTrax;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.IBinder;
import android.widget.Toast;

/**
 * A location tracking service. Can record tracklogs as well as individual waypoints.
 * 
 * @author steve
 *
 */
public class TrackerService extends Service {
	private LocationManager lm;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
    	lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	List<String> providers = lm.getAllProviders();
		Toast.makeText(getApplicationContext(), 
				providers.toString(), 
				Toast.LENGTH_LONG).show();
	}
	
	
	
	public void saveWaypoint(){
		String providerString = lm.getBestProvider(new Criteria(), true);
    	if (providerString == null){
    		Toast.makeText(getApplicationContext(), 
    				getString(R.string.error_no_providers), 
    				Toast.LENGTH_LONG).show();
    	}

    	LocationProvider provider = lm.getProvider(providerString);
    	
    	Location l = lm.getLastKnownLocation(providerString);
    	if (l != null){
		Toast.makeText(getApplicationContext(), 
				getString(R.string.favorite_result) + ": " + l.toString(), 
				Toast.LENGTH_SHORT).show();
    	}else{
    		Toast.makeText(getApplicationContext(), 
    				getString(R.string.error_no_fix), 
    				Toast.LENGTH_LONG).show();
    	}
	}
}

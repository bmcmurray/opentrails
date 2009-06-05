package net.cascadingstyle.android.lTrax;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

/**
 * A location tracking service. Can record tracklogs as well as individual waypoints.
 * 
 * @author steve
 *
 */
public class TrackerService extends Service {
	private LocationManager lm;
	private LocationDbAdapter db;
	
	private NotificationManager nm;
	private static final int TRACKING_NOTIFICATION = 0x00;
	private Notification notification;

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		
		db = new LocationDbAdapter(getApplication().getApplicationContext());
		nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    	lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	db.open();
    	
    	List<String> providers = lm.getAllProviders();
		Toast.makeText(getApplicationContext(), 
				providers.toString(), 
				Toast.LENGTH_LONG).show();
		showNotification();
	}
	
	public void onDestroy() {
		nm.cancel(TRACKING_NOTIFICATION);
		db.close();
	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	/********************** public methods ***********************/
	
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
    		if (db.createWaypoint(l) > -1){
    			setNotificationStatus("Now have "+db.getAllWaypoints().getCount() + " waypoints.");
    		}
    		Toast.makeText(getApplicationContext(), 
					getString(R.string.favorite_result) + ": " + l.toString(), 
					Toast.LENGTH_SHORT).show();
    	}else{
    		Toast.makeText(getApplicationContext(), 
    				getString(R.string.error_no_fix), 
    				Toast.LENGTH_LONG).show();
    	}
	}
	
	public void showNotification(){
		CharSequence text = getText(R.string.tracking_started);
		notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
		
		setNotificationStatus(text);
		
		nm.notify(TRACKING_NOTIFICATION, notification);
	}
	
	public void setNotificationStatus(CharSequence text){
		// intent that gets triggered upon activation
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, 
				new Intent(this, L_Trax.class), 0);
		notification.setLatestEventInfo(this, getText(R.string.service_label), text, contentIntent);
	}
	
	/********************* inner classes ************************/
	
	public class LocalBinder extends Binder {
		TrackerService getService(){
			return TrackerService.this;
		}
	}
	
	private final class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
            long endTime = System.currentTimeMillis() + 5*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
		}
	}
}

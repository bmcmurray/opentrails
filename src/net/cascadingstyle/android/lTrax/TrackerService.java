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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
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
public class TrackerService extends Service implements LocationListener {
	private LocationManager lm;
	public LocationDbAdapter db;
	
	private NotificationManager nm;
	private static final int TRACKING_NOTIFICATION = 0x00;
	private Notification notification;

	private final IBinder mBinder = new LocalBinder();
	
	private long currentTrack = -1;
	private String currentTrackName;

	@Override
	public void onCreate() {
		super.onCreate();
		
		db = new LocationDbAdapter(getApplicationContext());
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
	

	
	public void startRecordingTrack(String title){
		currentTrackName = title;
		startRecording();
	}
	
	public void startRecordingTrack(){
		startRecordingTrack(null);
	}
	
	public void stopRecordingTrack(){
		stopRecording();
	}
	
	public void showRecorded(){
		Toast.makeText(getApplicationContext(), 
				"DB has "+db.getAllWaypoints().getCount()+" waypoints stored.", 
				Toast.LENGTH_LONG).show();
	}
	
	public void saveWaypoint(){
		String providerString = lm.getBestProvider(new Criteria(), true);
    	if (providerString == null){
    		Toast.makeText(getApplicationContext(), 
    				getString(R.string.error_no_providers), 
    				Toast.LENGTH_LONG).show();
    	}

    	
    	Location loc = lm.getLastKnownLocation(providerString);
    	if (loc != null){
    		if (db.createWaypoint(loc) > -1){
    			setNotificationStatus("Now have "+db.getAllWaypoints().getCount() + " waypoints.");
    		}
    		Toast.makeText(getApplicationContext(), 
					getString(R.string.favorite_result) + ": " + loc.toString(), 
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
	
	private void startRecording(){
		lm.requestLocationUpdates("gps", 60000, 1, this);
		
	}
	
	private void stopRecording(){
		lm.removeUpdates(this);
	}
	
	public void onLocationChanged(Location loc) {
		if (currentTrack == -1){
			if (currentTrackName != null){
				currentTrack = db.createTrack(loc, currentTrackName);
			}else{
				currentTrack = db.createTrack(loc);
			}
		}else{
			db.createTrackPoint(currentTrack, loc);
		}
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
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

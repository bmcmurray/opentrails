package net.cascadingstyle.android.lTrax;

import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class L_Trax extends Activity implements ServiceConnection {
	private static final String TAG = "LTraxMain";
	private LocationDbAdapter mDbHelper;
	private TrackerService trackerService;
	private boolean isBound = false;
	
	private GPXEncoder gpxEncoder;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new LocationDbAdapter(this);
        mDbHelper.open();
        initButtons();
        
        try {
			gpxEncoder = new GPXEncoder(getResources().openRawResource(R.raw.gpxtemplate));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
       
    }
    
    private void initButtons(){
		Button bFavorite = (Button)findViewById(R.id.button_favorite);
		bFavorite.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				favorite();
			}
		});
		
		((Button)findViewById(R.id.button_start)).
			setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				bindService();
				
			}
		});
		
		((Button)findViewById(R.id.button_stop)).
		setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				unbindService();
				
			}
		});
	
		((Button)findViewById(R.id.button_showDb)).
		setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				TextView tv = (TextView)findViewById(R.id.debugText);
				Cursor tracks = trackerService.db.getAllTracks();
				int trkIdCol = tracks.getColumnIndex("id");
				int nameCol = tracks.getColumnIndex("title");
				
				tracks.moveToFirst();
				while(!tracks.isAfterLast()){
					Cursor c = trackerService.db.getAllTrackPoints(tracks.getLong(trkIdCol));
					
					int trkId = gpxEncoder.addTrack(tracks.getString(nameCol));
					int latIdx = c.getColumnIndex("lat");
					int lonIdx = c.getColumnIndex("lon");
					
					c.moveToFirst();
					while(!c.isAfterLast()){
						gpxEncoder.addTrackPoint(trkId, c.getFloat(latIdx), c.getFloat(lonIdx));
						c.moveToNext();
					}
					c.close();
					tracks.moveToNext();
				}
				tracks.close();
				tv.append(gpxEncoder.getXML());
			}
		});
		
    }
    
    public void bindService(){
    	if (isBound) return;
    	
    	isBound = bindService(new Intent(this, TrackerService.class), this, BIND_AUTO_CREATE);
    }
    
    public void unbindService(){
    	if (!isBound) return;
    	
    	unbindService(this);
    	isBound = false;
    }
    
    public void favorite(){
    	trackerService.saveWaypoint();
    }

	public void onServiceConnected(ComponentName name, IBinder service) {
		trackerService = ((TrackerService.LocalBinder)service).getService();
		trackerService.startRecordingTrack();
		
	}

	public void onServiceDisconnected(ComponentName name) {
		trackerService.stopRecordingTrack();
		trackerService = null;
		
	}
}

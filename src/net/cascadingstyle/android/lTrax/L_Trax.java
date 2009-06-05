package net.cascadingstyle.android.lTrax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class L_Trax extends Activity implements ServiceConnection {
	private static final String TAG = "LTraxMain";
	private LocationDbAdapter db;
	private TrackerService trackerService;
	private boolean isBound = false;
	
	private GPXEncoder gpxEncoder;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        db = new LocationDbAdapter(this);
        db.open();
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
				
				tv.setText(gpxEncoder.dbToGpx(db));
				
			}
		});
	
		((Button)findViewById(R.id.button_export_log)).
		setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				writeGpxToSD("openTrails.gpx");
				
			}
		});

    }
    
    public void writeGpxToSD(String filename){
    	try {
    	    File root = Environment.getExternalStorageDirectory();
    	    if (root.canWrite()){
    	        File gpxfile = new File(root, filename);
    	        FileWriter gpxwriter = new FileWriter(gpxfile);
    	        BufferedWriter out = new BufferedWriter(gpxwriter);
    	        out.write(gpxEncoder.dbToGpx(db));
    	        out.close();
    	    }
    	} catch (IOException e) {
    	    Log.e(TAG, "Could not write file", e);
    	}
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

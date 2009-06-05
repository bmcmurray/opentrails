package net.cascadingstyle.android.lTrax;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new LocationDbAdapter(this);
        mDbHelper.open();
        initButtons();
        
        
        
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
				Cursor c = trackerService.db.getAllWaypoints();
				TextView tv = (TextView)findViewById(R.id.debugText);
				tv.append("dumping "+c.getCount()+" rows:\n");
				while(!c.isAfterLast()){
					for (int i = 0; i < c.getColumnCount(); i++){
						tv.append(c.getString(i)+"\t");
					}
					tv.append("\n");
					c.moveToNext();
				}
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

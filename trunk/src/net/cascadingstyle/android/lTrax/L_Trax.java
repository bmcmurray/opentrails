package net.cascadingstyle.android.lTrax;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class L_Trax extends Activity implements ServiceConnection {
	private static final String TAG = "LTraxMain";
	private LocationDbAdapter mDbHelper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new LocationDbAdapter(this);
        mDbHelper.open();
        initButtons();
        
        bindService(new Intent(this, TrackerService.class), this, BIND_AUTO_CREATE);
    }
    
    private void initButtons(){
		Button bFavorite = (Button)findViewById(R.id.button_favorite);
		bFavorite.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				favorite();
			}
		});
    }
    
    public void favorite(){
    	
    }

	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		
	}

	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		
	}
}
package net.cascadingstyle.android.lTrax;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class L_Trax extends Activity {
	private LocationManager lm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initButtons();
        
    	lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	List<String> providers = lm.getAllProviders();
		Toast.makeText(getApplicationContext(), 
				providers.toString(), 
				Toast.LENGTH_LONG).show();
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
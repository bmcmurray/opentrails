/**
 * 
 */
package net.cascadingstyle.android.lTrax;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

/**
 * @author bmcmurray
 * 
 * Simple location database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * Adapted from NotesDbAdapter.java from http://developer.android.com/guide/tutorials/notepad/notepad-ex1.html
 * 
 */
public class LocationDbAdapter {

    private static final String TAG = "LTraxLocationDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private InputStream is;
    
    private String loadSqlFile(InputStream is) {
    	StringBuilder sqlString = new StringBuilder();
    	
    	try {
    		for (BufferedReader sqlReader = new BufferedReader(new InputStreamReader(is), 16000); sqlReader.ready();) {
    			sqlString.append(sqlReader.readLine());
    		}
    		
    		return sqlString.toString();
    	} catch (Exception e) {
    		Log.w(TAG, "Error trying to read the sql file.");
    		return "";
    	}
    }
    
    private static String DATABASE_CREATE;

    private static final String DATABASE_NAME = "ltrax_data";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public LocationDbAdapter(Context ctx) {
        this.mCtx = ctx;
        if (DATABASE_CREATE == null){
	        this.is = this.mCtx.getResources().openRawResource(R.raw.ltrax_sql);
	        DATABASE_CREATE = this.loadSqlFile(is);
        }
        
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.v(TAG, "hello");
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }



    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public LocationDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }

    
    
    /**
     * Create a new trackpoint for a given Track. If the point is
     * successfully created return the new rowId for that point, otherwise return
     * a -1 to indicate failure.
     * 
     * @param trackId the ID of the track
     * @param location the current location 
     * @return rowId or -1 if failed
     */
    public long createTrackPoint(long trackId, Location location) {
        ContentValues pointValues = new ContentValues();
        pointValues.put("tid", trackId);
        pointValues.put("lon", location.getLongitude());
        pointValues.put("lat", location.getLatitude());
        pointValues.put("timestamp", location.getTime());
        
        return mDb.insert(
        		"TrackPoint", 
        		null, 
        		pointValues);
    }

    /**
     * Create a new track using an initial Location and title. If the track is
     * successfully created return the new rowId for that track, otherwise return
     * a -1 to indicate failure.
     * 
     * @param location the initial Location object to start the track
     * @param title the title of the new track 
     * @return rowId or -1 if failed
     */
    public long createTrack(Location location, String title) {
    	// Make a new Track first
    	ContentValues initialValues = new ContentValues();
        initialValues.put("title", title);
        initialValues.put("created", location.getTime());
        initialValues.put("modified", location.getTime());
        
        long track = mDb.insert(
        		"Track", 
        		null, 
        		initialValues);
        
        if (track != -1)
        {
	        // Now make the initial TrackPoint	        
	        return createTrackPoint(track, location);
        }
        else
        {
        	return track;
        }    	
    }
    
    /**
     * Create a new track using an initial Location. Without the title, we generate
     * a title for the user based off the timestamp of the Location. If the track is
     * successfully created return the new rowId for that track, otherwise return
     * a -1 to indicate failure.
     * 
     * @param location the initial Location object to start the track
     * @return rowId or -1 if failed
     */
    public long createTrack(Location location) {
    	String title = "";
    	long create_time = location.getTime();
    	// convert the UTC timestamp into readable datetime
    	
    	title = Long.toString(create_time);
    	
    	return createTrack(location, title);
    }
    
    /**
     * Create a new waypoint using an initial Location and title. If the waypoint is
     * successfully created return the new rowId for that point, otherwise return
     * a -1 to indicate failure.
     * 
     * @param location the Location of the waypoint
     * @param title the title of the new track 
     * @return rowId or -1 if failed
     */
    public long createWaypoint(Location location, String title) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put("title", title);
    	initialValues.put("lon", location.getLongitude());
    	initialValues.put("lat", location.getLatitude());
    	initialValues.put("timestamp", location.getTime());
    	
    	return mDb.insert(
    			"Point", 
    			null, 
    			initialValues);
    }
    
    /**
     * Create a new waypoint using an initial Location. Without the title, we generate
     * a title for the user based off the timestamp of the Location. If the waypoint is
     * successfully created return the new rowId for that track, otherwise return
     * a -1 to indicate failure.
     * 
     * @param location the Location of the waypoint
     * @return rowId or -1 if failed
     */
    public long createWaypoint(Location location) {
    	String title = "";
    	long create_time = location.getTime();
    	// convert the UTC timestamp into readable datetime
    	
    	title = Long.toString(create_time);
    	return createWaypoint(location, title);
    }
    
    /**
     * Delete the Track with the given trackId
     * 
     * @param trackId id of Track to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteTrack(long trackId) {
    	// First, delete all of the TrackPoints
    	mDb.delete(
    			"TrackPoint", 
    			"tid=?", new String[] {Long.toString(trackId)});
    	// Now, delete the Track
    	return mDb.delete(
    			"Track", 
    			"id=?", new String[] {Long.toString(trackId)}) > 0;
    }
    
    /**
     * Delete the Waypoint with the given pointId
     * 
     * @param pointId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteWaypoint(long pointId) {
    	return mDb.delete(
    			"Point", 
    			"id=?", new String[] {Long.toString(pointId)}) > 0;
    }
    
    /**
     * Return a Cursor over the list of all Tracks in the database
     * 
     * @return Cursor over all tracks
     */
    public Cursor getAllTracks() {
    	return mDb.query(
    			"Track", 
    			new String[] {"id", "title", "created", "modified"}, 
    			null, null, null, null, null);
    }
    
    public Cursor getTrack(long trackId) {
    	return mDb.query(
    			true,
    			"Track", 
    			new String[] {"id", "title", "created", "modified"}, 
    			"id=?", new String[] {Long.toString(trackId)}, 
    			null, null, null, null);
    	
    }
    
    /**
     * Return a Cursor over the list of all waypoints in the database
     * 
     * @return Cursor over all waypoints
     */
    public Cursor getAllWaypoints() {
    	return mDb.query(
    			"Point", 
    			new String[] {"id", "title", "lon", "lat", "timestamp"}, 
    			null, null, null, null, null);
    }
    
    public Cursor getWaypoint(long pointId) {
    	Cursor mCursor = mDb.query(
    			true,
    			"Point", 
    			new String[] {"id", "title", "lon", "lat", "timestamp"}, 
    			"id=?", 
    			new String[] {Long.toString(pointId)}, 
    			null, null, null, null);
    	
    	if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    
    /**
     * Update the track using the details provided. The track to be updated is
     * specified using the trackId; a new title will update the current title.
     * 
     * @param trackId id of track to update
     * @param title value to set track's title to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateTrack(long trackId, String title) {
    	// update the Track's title
    	ContentValues values = new ContentValues();
    	values.put("title", title);
        //values.put("modified", new Date().getTime());
        
    	return mDb.update(
    			"Track", 
    			values, 
    			"id=?", new String[] {Long.toString(trackId)}) > 0;
    }
    
    /**
     * Update the track using the details provided. The track to be updated is
     * specified using the trackId; a new location will cause for a new TrackPoint to
     * be created.
     * 
     * @param trackId id of track to update
     * @param title value to set track's title to
     * @param location a new location to be associated to this track
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateTrack(long trackId, Location location) {
    	// update the Track's modified time
    	ContentValues values = new ContentValues();
        values.put("modified", new Date().getTime());
    	
    	mDb.update(
    			"Track", 
    			values, 
    			"id=?", new String[] {Long.toString(trackId)});
    	
    	// make a new TrackPoint
    	return createTrackPoint(trackId, location) > 0;
    }
    
    /**
     * Update the Waypoint using the details provided. The point to be updated is
     * specified using the pointId, and it is altered to use the title provided.
     * 
     * @param pointId id of note to update
     * @param title value to set Waypoint's title to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateWaypoint(long pointId, String title) {
    	// update the Waypoint's title
    	ContentValues values = new ContentValues();
    	values.put("title", title);
        
    	return mDb.update(
    			"Point", 
    			values, 
    			"id=?", new String[] {Long.toString(pointId)}) > 0;
    }
}

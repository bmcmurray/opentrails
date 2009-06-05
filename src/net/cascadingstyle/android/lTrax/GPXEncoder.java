package net.cascadingstyle.android.lTrax;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;

public class GPXEncoder {
	private Map<String, String> rootAttrs;
	private List<List<String>>body;
	private final String template;
	
	public GPXEncoder(InputStream blankTemplate) {
    	StringBuilder template = new StringBuilder();
    	
    	try {
    		for (BufferedReader reader = new BufferedReader(new InputStreamReader(blankTemplate), 16000);
    				reader.ready();) {
    			template.append(reader.readLine());
    		}
    	} catch (Exception e) {	
    		e.printStackTrace();
    	}
    	this.template = template.toString();
		
    	clear();
	}
	
	public void setCreator(String creator){
		rootAttrs.put("creator", creator);
	}
	public void setTitle(String title){
		
		
		
	}
	
	public void clear(){
		body = new ArrayList<List<String>>();
		rootAttrs  = new HashMap<String, String>();
	}
	public int addTrack(String name){
		List<String> track = new ArrayList<String>();
		track.add("trk");
		track.add(name);
		body.add(track);
		return body.size() - 1;
	}
	
	public void addTrackPoint(int trackId, float lat, float lon){
		body.get(trackId).add("<wpt lat='"+lat+"' lon='"+lon+"'/>");
	}
	public String getXML(){
		StringBuilder rootAttrText = new StringBuilder();
		StringBuilder bodyBuilder = new StringBuilder();
		for (String k: rootAttrs.keySet()){
			rootAttrText.append(k+"='"+rootAttrs.get(k)+"' ");
		}
		for (List<String> el: body){
			String e = el.get(0);
			String name = el.get(1);
			bodyBuilder.append("<"+e+">");
			bodyBuilder.append("<name>"+name+"</name>");
			
			for (int i = 2; i < el.size(); i++ ){
				bodyBuilder.append(el.get(i));
			}
			bodyBuilder.append("</"+e+">");
		}
		return String.format(this.template, rootAttrText.toString(), "", bodyBuilder.toString());
	}
	
	public String dbToGpx(LocationDbAdapter db){
		clear();
		Cursor tracks = db.getAllTracks();
		int trkIdCol = tracks.getColumnIndex("id");
		int nameCol = tracks.getColumnIndex("title");
		
		tracks.moveToFirst();
		while(!tracks.isAfterLast()){
			Cursor c = db.getAllTrackPoints(tracks.getLong(trkIdCol));
			
			int trkId = addTrack(tracks.getString(nameCol));
			int latIdx = c.getColumnIndex("lat");
			int lonIdx = c.getColumnIndex("lon");
			
			c.moveToFirst();
			while(!c.isAfterLast()){
				addTrackPoint(trkId, c.getFloat(latIdx), c.getFloat(lonIdx));
				c.moveToNext();
			}
			c.close();
			tracks.moveToNext();
		}
		tracks.close();
		return getXML();
		
	}
}

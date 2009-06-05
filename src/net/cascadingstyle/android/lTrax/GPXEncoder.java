package net.cascadingstyle.android.lTrax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.sax.Element;
import android.util.Log;

public class GPXEncoder {
	private String creator;
	private Map<String, String> rootAttrs = new HashMap<String, String>();
	private List<List<String>>body = new ArrayList<List<String>>();
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
		
	}
	
	public void setCreator(String creator){
		rootAttrs.put("creator", creator);
	}
	public void setTitle(String title){
		
		
		
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
			String e = el.remove(0);
			String name = el.remove(0);
			bodyBuilder.append("<"+e+">");
			bodyBuilder.append("<name>"+name+"</name>");
			for (String item: el){
				bodyBuilder.append(item);
			}
			bodyBuilder.append("</"+e+">");
		}
		return String.format(this.template, rootAttrText.toString(), "", bodyBuilder.toString());
	}
}

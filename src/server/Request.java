package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Request
{
	Integer type = null;
	Double srcLat = null;
	Double srcLon = null;
	Double destLat = null;
	Double destLon = null;
	Integer linija = null;
	String smer = null;
	
	
	public Request() {}

	public Request(Integer type, Double srcLat, Double srcLon, Double destLat, 
			Double destLon, Integer linija, String smer)
	{
		super();
		this.type = type;
		this.srcLat = srcLat;
		this.srcLon = srcLon;
		this.destLat = destLat;
		this.destLon = destLon;
		this.linija = linija;
		this.smer = smer;
	}

	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

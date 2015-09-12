package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Request
{
	Integer type;
	Double srcLat = 0.0;
	Double srcLon = 0.0;
	Double destLat = 0.0;
	Double destLon = 0.0;
	Integer linija = 0;
	String smer = "";
	
	
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

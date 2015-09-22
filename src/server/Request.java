package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Request
{
	public Integer type = null;
	public Double srcLat = null;
	public Double srcLon = null;
	public Double destLat = null;
	public Double destLon = null;
	public Integer linija = null;
	//public String smer = null;
	public String message = null;
	public Double dbVer = null;
	
	public Request() {}

	public Request(Integer type, Double srcLat, Double srcLon, Double destLat, 
			Double destLon, Integer linija, /*String smer,*/ String message)
	{
		this.type = type;
		this.srcLat = srcLat;
		this.srcLon = srcLon;
		this.destLat = destLat;
		this.destLon = destLon;
		this.linija = linija;
		//this.smer = smer;
		this.message = message;
	}

	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

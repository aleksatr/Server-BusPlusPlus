package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Request
{
	int type;
	double srcLat = 0.0;
	double srcLon = 0.0;
	double destLat = 0.0;
	double destLon = 0.0;
	int linija = 0;
	String smer = "";
	
	
	public Request() {}
	
	public Request(int type, double srcLat, double srcLon, double destLat, double destLon, int linija, String smer)
	{
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
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		return gson.toJson(this);
	}
}

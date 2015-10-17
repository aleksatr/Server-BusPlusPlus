package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import datalayer.CSInfo;

public class Request
{
	public Integer type = null;
	public Double srcLat = null;
	public Double srcLon = null;
	public Double destLat = null;
	public Double destLon = null;
	public Integer linija = null;
	//public String message = null;
	public Double dbVer = null;
	
	//crowd sourcing
	/*public Integer crowded = null;			//guzva 
	public Integer stuffy = null;			//zagusljivost
	public Integer stanica = null;			//poslednja stanica za crowd sensing
	public Integer udaljenost = null;		//udaljenost od poslednje stanice za crowd sensing*/
	
	public CSInfo crowdSensing = null;
	
	public Request() {}

	public Request(Integer type, Double srcLat, Double srcLon, Double destLat, 
			Double destLon, Integer linija,
			Double dbVer, CSInfo crowdSensing)
	{
		this.type = type;
		this.srcLat = srcLat;
		this.srcLon = srcLon;
		this.destLat = destLat;
		this.destLon = destLon;
		this.linija = linija;
		this.dbVer = dbVer;
		this.crowdSensing = crowdSensing;
	}
	

	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

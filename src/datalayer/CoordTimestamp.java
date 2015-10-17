package datalayer;

import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CoordTimestamp
{
	public int cas;
	public int minut;
	public double lat;
	public double lon;
	
	public transient LocalDateTime timestamp;
	
	public CoordTimestamp(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
		this.timestamp = LocalDateTime.now();
		this.cas = timestamp.getHour();
		this.minut = timestamp.getMinute();
	}
	
	public CoordTimestamp(double lat, double lon, int cas, int minut)
	{
		this.lat = lat;
		this.lon = lon;
		this.timestamp = null;
		this.cas = cas;
		this.minut = minut;
	}
	
	public CoordTimestamp(CoordTimestamp ct)
	{
		this.lat = ct.lat;
		this.lon = ct.lon;
		this.timestamp = null;
		this.cas = ct.cas;
		this.minut = ct.minut;
	}
	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

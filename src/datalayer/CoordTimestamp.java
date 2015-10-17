package datalayer;

import java.time.LocalDateTime;

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
}

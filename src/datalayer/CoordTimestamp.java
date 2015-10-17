package datalayer;

import java.time.LocalDateTime;

public class CoordTimestamp
{
	public LocalDateTime timestamp = null;
	public double lat;
	public double lon;
	
	public CoordTimestamp(double lat, double lon)
	{
		this.lat = lat;
		this.lon = lon;
		timestamp = LocalDateTime.now();
	}
	
	public CoordTimestamp(double lat, double lon, LocalDateTime timestamp)
	{
		this.lat = lat;
		this.lon = lon;
		this.timestamp = timestamp;
	}
}

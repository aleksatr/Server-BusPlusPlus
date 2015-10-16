package datalayer;

public class CSInfo
{
	public double lat;
	public double lon;
	public int crowded;
	public int stuffy;
	
	public CSInfo(double lat, double lon, int crowded, int stuffy)
	{
		this.lat = lat;
		this.lon = lon;
		this.crowded = crowded;
		this.stuffy = stuffy;
	}
}

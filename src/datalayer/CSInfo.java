package datalayer;

public class CSInfo
{
	public double lat;
	public double lon;
	public int crowded;
	public int stuffy;
	public String brojLinije;
	public String smerLinije;
	public Integer stanica = null;			//poslednja stanica za crowd sensing
	public Integer udaljenost = null;		//udaljenost od poslednje stanice za crowd sensing
	public String message;
	
	public CSInfo(double lat, double lon, int crowded, int stuffy, 
			String brojLinije, String smerLinije, String message)
	{
		this.lat = lat;
		this.lon = lon;
		this.crowded = crowded;
		this.stuffy = stuffy;
		this.brojLinije = brojLinije;
		this.smerLinije = smerLinije;
		this.message = message;
	}
	
	public void usrednji(CSInfo csInfo)
	{
		this.crowded = (this.crowded + csInfo.crowded) / 2;
		this.stuffy = (this.stuffy + csInfo.stuffy) / 2;
		
		this.message += "+busSEPARATOR+" + csInfo.message;
		
		//od koordinata pamti samo najnovije
		this.lat = csInfo.lat;
		this.lon = csInfo.lon;
		this.stanica = csInfo.stanica;
		this.udaljenost = csInfo.udaljenost;
	}
}

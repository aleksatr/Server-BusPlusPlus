package datalayer;

import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CSInfo
{
	public double lat;
	public double lon;
	public double crowded;
	public double stuffy;
	public String brojLinije;
	public String smerLinije;
	public Integer stanica = null;			//poslednja stanica za crowd sensing
	public Integer udaljenost = null;		//udaljenost od poslednje stanice za crowd sensing
	public String message;
	public boolean kontrola;
	
	
	public CSInfo(double lat, double lon, double crowded, double stuffy, 
			String brojLinije, String smerLinije, Integer stanica, Integer udaljenost, String message, boolean kontrola)
	{
		this.lat = lat;
		this.lon = lon;
		this.crowded = crowded;
		this.stuffy = stuffy;
		this.brojLinije = brojLinije;
		this.smerLinije = smerLinije;
		this.stanica = stanica;
		this.udaljenost = udaljenost;
		this.message = message;
		this.kontrola = kontrola;
	}
	
	public void usrednji(CSInfo csInfo)
	{
		this.crowded = (this.crowded + csInfo.crowded) / 2.0;
		this.stuffy = (this.stuffy + csInfo.stuffy) / 2.0;
		
		this.message += "+busSEPARATOR+" + csInfo.message;
		
		//od koordinata pamti samo najnovije
		this.lat = csInfo.lat;
		this.lon = csInfo.lon;
		this.stanica = csInfo.stanica;
		this.udaljenost = csInfo.udaljenost;
		this.kontrola = csInfo.kontrola;
	}
	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

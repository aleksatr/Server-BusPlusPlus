package datalayer;

import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CSInfo
{
	public Double lat = null;
	public Double lon = null;
	public Double crowded = null;
	public Double stuffy = null;
	public String brojLinije = null;
	public String smerLinije = null;
	public Integer stanica = null;			//poslednja stanica za crowd sensing
	public Integer udaljenost = null;		//udaljenost od poslednje stanice za crowd sensing
	public String message = null;
	public Boolean kontrola = null;
	
	//public Integer linijaId = null;			//za klasican red voznje, da se oznaci na koju liniju se odnosi info
	public Integer cas = null;
	public Integer minut = null;
	
	public CSInfo(CSInfo cs)
	{
		this.crowded = cs.crowded;
		this.stuffy = cs.stuffy;
		this.message = cs.message;
	}
	
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

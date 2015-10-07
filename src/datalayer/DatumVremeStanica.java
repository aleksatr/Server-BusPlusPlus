package datalayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DatumVremeStanica
{
	public Integer stanica = null; 		//id stanice na koju se odnosi vreme dolaska
	//public Integer korekcija = null; 	//korekcija za klasican red voznje (vreme koje je potrebno busu od pocetne stanice do stanice stanica)
	public int sekund = 0;
	public int minut = 0;
	public int sat = 0;
	public int dan = 0;
	public int mesec = 0;
	public int godina = 0;
	
	public DatumVremeStanica() {}

	public DatumVremeStanica(Integer stanica, int sekund, int minut, int sat, int dan,
			int mesec, int godina)
	{
		this.stanica = stanica;
		this.sekund = sekund;
		this.minut = minut;
		this.sat = sat;
		this.dan = dan;
		this.mesec = mesec;
		this.godina = godina;
	}
	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
	
}

package strukture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Linija
{
	public String broj = null;
	public String smer = null;
	public String naziv = null;
	public Cvor pocetnaStanica = null;
	
	public Linija() {}
	
	public Linija(String broj, String smer, String naziv, Cvor pocetnaStanica)
	{
		this.broj = broj;
		this.smer = smer;
		this.naziv = naziv;
		this.pocetnaStanica = pocetnaStanica;
	}


	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

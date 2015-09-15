package strukture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Linija
{
	public Integer id = null;
	public String broj = null;
	public String smer = null;
	public String naziv = null;
	public transient Cvor pocetnaStanica = null;
	
	public Linija() {}
	
	public Linija(Integer id, String broj, String smer, String naziv, Cvor pocetnaStanica)
	{
		this.id = id;
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
		//return id + " " +broj +" " +smer + " " +naziv + " <<" + pocetnaStanica.id +">>";
	}
}

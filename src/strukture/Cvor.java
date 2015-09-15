package strukture;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Cvor
{
	public Integer id = null;
	public String naziv = null;
	public Double lat = null;
	public Double lon = null;
	public transient ArrayList<Veza> veze;
	
	public Cvor() {}
	
	public Cvor(Cvor prototype)
	{
		this.id = prototype.id;
		this.naziv = prototype.naziv;
		this.lat = prototype.lat;
	}
	
	public Cvor(Integer id, String naziv, Double lat, Double lon)
	{
		this.id = id;
		this.naziv = naziv;
		this.lat = lat;
		this.lon = lon;
		this.veze = new ArrayList<>();
	}

	public void dodajVezu(Linija linija, Integer weight, Cvor destination)
	{
		if(veze != null)
			veze.add(new Veza(destination, weight, linija));
	}
	
	public Veza vratiVezu(Linija l)
	{	
		if(veze == null)
			return null;
		
		Veza v = null;
		
		for(int i = 0; i < veze.size(); ++i)
		{
			v = veze.get(i);
			if(v.linija == l)
				return v;
		}
		
		return null;
	}
	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
		//return " " + id +" " +  naziv +" " +  lat +" " +  lon;
	}
}

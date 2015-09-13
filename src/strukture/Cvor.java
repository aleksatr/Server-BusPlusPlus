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
	public ArrayList<Veza> veze;
	
	public Cvor() {}
	
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
		veze.add(new Veza(destination, weight, linija));
	}
	
	public Veza vratiVezu(Linija l)
	{
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
	}
}

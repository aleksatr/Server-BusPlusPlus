package strukture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Veza
{
	public Cvor destination = null;
	public Integer weight = null;
	public Linija linija = null;
	
	public Veza() {}
	
	public Veza(Cvor destination, Integer weight, Linija linija)
	{
		this.destination = destination;
		this.weight = weight;
		this.linija = linija;
	}

	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

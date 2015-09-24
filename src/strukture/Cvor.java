package strukture;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Cvor
{
	public Integer id = null;								//jedinstveni id stanice
	public String naziv = null;								//naziv stanice
	public Double lat = null;								//
	public Double lon = null;								//koordinate stanice			
	public transient ArrayList<Veza> veze;					//veze ka drugim stanicama
	
	//pomocne promenljive za algoritme obilaska grafa
	public int status = StruktureConsts.CVOR_NEOBRADJEN; 	//da li je cvor obradjen prilikom obilaska grafa
	public Cvor prethodnaStanica = null;					//sa koje stanice se doslo na ovu stanicu
	public Linija linijom = null;							//kojom linijom se doslo sa prethodne stanice na ovu
	
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
	
	public void resetStatus()
	{
		this.status = StruktureConsts.CVOR_NEOBRADJEN;
		this.prethodnaStanica = null;
		this.linijom = null;
	}
}

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
	
	public transient int matRadni[][] = null;         	//mat[i][j]==-1 znaci prazno
	public transient int matSubota[][] = null;
	public transient int matNedelja[][] = null;
	
	public Linija() {}
	
	public Linija(Integer id, String broj, String smer, String naziv, Cvor pocetnaStanica, int matRadni[][], int matSubota[][], int matNedelja[][])
	{
		this.id = id;
		this.broj = broj;
		this.smer = smer;
		this.naziv = naziv;
		this.pocetnaStanica = pocetnaStanica;
		this.matRadni = matRadni;
		this.matSubota = matSubota;
		this.matNedelja = matNedelja;
	}
	
	public void stampajRedVoznje()
	{
		System.out.println("Linija: " + broj + " smer: " + smer);
		System.out.println("RADNI:");
		for(int i = 0; i < 25; ++i)
		{
			System.out.print(i + ": ");
			for(int j = 0; j < 60; ++j)
				if(matRadni[i][j] != -1)
					System.out.print(" " + matRadni[i][j]);
				else 
					break;
			
			System.out.println();
		}
		
		System.out.println("SUBOTA:");
		for(int i = 0; i < 25; ++i)
		{
			System.out.print(i + ": ");
			for(int j = 0; j < 60; ++j)
				if(matSubota[i][j] != -1)
					System.out.print(" " + matSubota[i][j]);
				else 
					break;
			
			System.out.println();
		}
		
		System.out.println("NEDELJA:");
		for(int i = 0; i < 25; ++i)
		{
			System.out.print(i + ": ");
			for(int j = 0; j < 60; ++j)
				if(matNedelja[i][j] != -1)
					System.out.print(" " + matNedelja[i][j]);
				else 
					break;
			
			System.out.println();
		}
		
	}

	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
		//return id + " " +broj +" " +smer + " " +naziv + " <<" + pocetnaStanica.id +">>";
	}
}

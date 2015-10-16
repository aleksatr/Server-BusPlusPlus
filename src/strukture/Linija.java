package strukture;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import datalayer.CSInfo;

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
	
	//
	public transient CSInfo cSourcedData[][] = null;
	
	private transient double raspodelaBrzina[] =	{ 
														8.3,	//00-01
														8.3,	//01-02
														8.0,	//02-03
														7.0,	//03-04
														6.5,	//04-05
														5.4,	//05-06
														5.0,	//06-07
														4.5,	//07-08
														4.15,	//08-09
														5.0,	//09-10
														5.4,	//10-11
														6.0,	//11-12
														6.0,	//12-13
														5.4,	//13-14
														4.5,	//14-15
														4.15,	//15-16
														5.0,	//16-17
														5.4,	//17-18
														6.0,	//18-19
														6.0,	//19-20
														7.0,	//20-21
														7.5,	//21-22
														8.0,	//22-23
														8.3		//23-24
													};
	//za min walk prioritet linije (heuristka najblize stanice na liniji)
	public transient double prioritet = Double.MAX_VALUE;
	
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
		this.cSourcedData = new CSInfo[25][60];
	}
	
	public void addCSInfo(CSInfo csInfo)
	{
		
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
	
	public ArrayList<Cvor> vratiStaniceNaLiniji()
	{
		ArrayList<Cvor> rezultat = new ArrayList<>();
		
		Veza v = null;
		Cvor c = this.pocetnaStanica;
		
		rezultat.add(c);
		
		while((v = c.vratiVezu(this)) != null)
		{
			c = v.destination;

			rezultat.add(c);
			
			if(c == this.pocetnaStanica)
				break;
		}
		
		return rezultat;
	}
	
	//distanca koju bus na ovoj liniji predje od stanice start do stanice stop
	public double calcDistance(Cvor start, Cvor stop)
	{
		double distance = 0.0;
		
		Veza v = null;
		Cvor c = start;
		
		while((v = c.vratiVezu(this)) != null)
		{
			c = v.destination;
			
			distance += v.weight;
			
			if(c == stop || c == this.pocetnaStanica)
				break;
		}
		
		return distance;
	}

	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
		//return id + " " +broj +" " +smer + " " +naziv + " <<" + pocetnaStanica.id +">>";
	}
}

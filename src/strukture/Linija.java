package strukture;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
	//private static transient Object cSourcedDataLock = new Object();
	//private static transient Object raspodelaBrzinaLock = new Object();
	
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
	
	public double vratiTrenutnuBrzinu()
	{
		LocalDateTime currentTime = LocalDateTime.now();
		
		return raspodelaBrzina[currentTime.getHour()];
	}
	
	public synchronized void dodajBrzinu(double speed)
	{
		LocalDateTime currentTime = LocalDateTime.now();
		int curHour = currentTime.getHour();
		int curMin = currentTime.getMinute();
		raspodelaBrzina[curHour] = raspodelaBrzina[curHour]*0.5 + speed*0.5;
		
		if(curMin > 30 && curHour < 24)
			raspodelaBrzina[curHour+1] = raspodelaBrzina[curHour+1]*0.75 + speed*0.25;
		
		/*System.out.println("CS brzina za liniju " + broj + smer + " u " + curHour + "h prepravljena na " + 
							raspodelaBrzina[curHour]);*/
	}
	
	public synchronized void dodajCSInfo(CSInfo csInfo, DayOfWeek day, int hourIndex, int minuteIndex)
	{
		int mat[][] = null;
		
		if(day == DayOfWeek.SATURDAY)
			mat = this.matSubota;
		else if(day == DayOfWeek.SUNDAY)
			mat = this.matNedelja;
		else
			mat = this.matRadni;
		
		if(this.cSourcedData[hourIndex][minuteIndex] == null)
		{
			/*System.out.println("Kreiran csInfo za liniju " + this.broj + this.smer +
					", za bus u " + hourIndex + ":" + mat[hourIndex][minuteIndex]);*/
			//this.cSourcedData[hourIndex][minuteIndex] = csInfo;
			this.cSourcedData[hourIndex][minuteIndex] = new CSInfo(csInfo.lat, csInfo.lon, csInfo.crowded, csInfo.stuffy, csInfo.brojLinije, csInfo.smerLinije, csInfo.stanica, csInfo.udaljenost, csInfo.message, csInfo.kontrola);
		}
		else
		{
			/*System.out.println("Dodat csInfo za liniju " + this.broj + this.smer +
					", za bus u " + hourIndex + ":" + mat[hourIndex][minuteIndex]);*/
			this.cSourcedData[hourIndex][minuteIndex].usrednji(csInfo);
		}
	}
	
	public String stampajRaspodeluBrzina()
	{
		String raspodela = "";
		for(int i = 0; i < raspodelaBrzina.length; ++i)
			raspodela += "[" + i +"]\t" + raspodelaBrzina[i] + "\n";
		
		return raspodela;
	}
	
	public String stampajCSInfo()
	{
		String cInfo = "";
		DayOfWeek day = LocalDateTime.now().getDayOfWeek();
		int mat[][] = null;
		
		if(day == DayOfWeek.SATURDAY)
			mat = this.matSubota;
		else if(day == DayOfWeek.SUNDAY)
			mat = this.matNedelja;
		else
			mat = this.matRadni;
		
		for(int i = 0; i < 25; ++i)
			for(int j = 0; j < 60; ++j)
				if(cSourcedData[i][j] != null)
					cInfo += "Bus u " + i + ":" + mat[i][j] + " informacije : " + cSourcedData[i][j];
		
		return cInfo;
	}
	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
		//return id + " " +broj +" " +smer + " " +naziv + " <<" + pocetnaStanica.id +">>";
	}
}

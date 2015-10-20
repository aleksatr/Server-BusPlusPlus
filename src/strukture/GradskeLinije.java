package strukture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import datalayer.CSInfo;
import datalayer.CoordTimestamp;
import server.*;

public class GradskeLinije
{
	//public ArrayList<Linija> linije = new ArrayList<>();
	public Linija linije[];		//id linija u bazi odgovara indeksu u ovom nizu
	private Graf graf;
	
	
	public GradskeLinije(int size) 
	{
		linije = new Linija[size];
	}
			
	public GradskeLinije(String grafDBName, String redVoznjeDBName, Graf graf) throws ClassNotFoundException, SQLException, Exception
	{
		int maxId = 0;
		
		this.graf = graf;
		// load the sqlite-JDBC driver using the current class loader
	    Class.forName("org.sqlite.JDBC");
	    
	    Connection connection = null;		//konekcija za bazu bpp.db
	    Connection rvConnection = null;  	//konekcija za bazu red_voznje.db
	    
	    // create a database connection
	    connection = DriverManager.getConnection("jdbc:sqlite:" + grafDBName);
	    rvConnection = DriverManager.getConnection("jdbc:sqlite:" + redVoznjeDBName);
	    Statement statement = connection.createStatement();
	    Statement rvStatement = rvConnection.createStatement();
	    statement.setQueryTimeout(30);  // set timeout to 30 sec.
	    rvStatement.setQueryTimeout(30);
	    
	    ResultSet rs = statement.executeQuery("select max(id) from LINIJA");
	    ResultSet rvRs = null;
	    
	    if(rs.next())
	    {
	    	maxId = rs.getInt(1);
	    }
	    else
	    {
	    	System.out.println("select max(id) from LINIJA; query nije vratio rezultat");
	    	throw new Exception("select max(id) from LINIJA; query nije vratio rezultat");
	    }
	    
	    linije = new Linija[maxId + 1];
	    
	    rs = statement.executeQuery("select * from LINIJA");
	    
	    while(rs.next())
	    {
	    	// read the result set
	    	int id = rs.getInt("id");
	    	String broj = rs.getString("broj");
	    	String smer = rs.getString("smer");
	    	String naziv = rs.getString("naziv");
	    	int stanicaId = rs.getInt("pocetna_stanica_id");
	    	//dummy cvor da sacuva ID za pravi cvor koji se tek kasnije ucitava iz baze i kreira
	    	Cvor pocetnaStanica = new Cvor(stanicaId, null, null, null); 
	    	
	    	int matRadni[][] = new int[25][60];
	    	int matSubota[][] = new int[25][60];
	    	int matNedelja[][] = new int[25][60];
	    	
	    	for(int k = 0; k < 25; ++k)
	    		for(int q = 0; q < 60; ++q)
	    		{
	    			matRadni[k][q] = -1;
	    			matSubota[k][q] = -1;
	    			matNedelja[k][q] = -1;
	    		}
	    	
	    	int index;
	    	
	    	rvRs = rvStatement.executeQuery("select RED_VOZNJE.cas, RADNI_DAN_MINUTA.radni_dan_minuta from RED_VOZNJE, RADNI_DAN_MINUTA where RED_VOZNJE.id = RADNI_DAN_MINUTA.red_voznje_id and RED_VOZNJE.linija='" + broj + "' and RED_VOZNJE.smer='" + smer+ "' ORDER BY RED_VOZNJE.cas, RADNI_DAN_MINUTA.radni_dan_minuta");
	    	
	    	while(rvRs.next())
	    	{
	    		index = 0;
	    		int cas = rvRs.getInt("cas");
	    		int radni_dan_minuta = rvRs.getInt("radni_dan_minuta");
	    		while(matRadni[cas][index] != -1)
	    			++index;
	    		matRadni[cas][index] = radni_dan_minuta;
	    	}
	    		
	    	rvRs = rvStatement.executeQuery("select RED_VOZNJE.cas, SUBOTA_MINUTA.subota_minuta from RED_VOZNJE, SUBOTA_MINUTA where RED_VOZNJE.id = SUBOTA_MINUTA.red_voznje_id and RED_VOZNJE.linija='" + broj + "' and RED_VOZNJE.smer='" + smer + "' ORDER BY RED_VOZNJE.cas, SUBOTA_MINUTA.subota_minuta");
	    	
	    	while(rvRs.next())
	    	{
	    		index = 0;
	    		int cas = rvRs.getInt("cas");
	    		int subota_minuta = rvRs.getInt("subota_minuta");
	    		while(matSubota[cas][index] != -1)
	    			++index;
	    		matSubota[cas][index] = subota_minuta;
	    	}
	    	
	    	rvRs = rvStatement.executeQuery("select RED_VOZNJE.cas, NEDELJA_MINUTA.nedelja_minuta from RED_VOZNJE, NEDELJA_MINUTA where RED_VOZNJE.id = NEDELJA_MINUTA.red_voznje_id and RED_VOZNJE.linija='" + broj + "' and RED_VOZNJE.smer='" + smer+ "' ORDER BY RED_VOZNJE.cas, NEDELJA_MINUTA.nedelja_minuta");
	    	
	    	while(rvRs.next())
	    	{
	    		index = 0;
	    		int cas = rvRs.getInt("cas");
	    		int nedelja_minuta = rvRs.getInt("nedelja_minuta");
	    		while(matNedelja[cas][index] != -1)
	    			++index;
	    		matNedelja[cas][index] = nedelja_minuta;
	    	}
	    		
	    	linije[id] = new Linija(id, broj, smer, naziv, pocetnaStanica, matRadni, matSubota, matNedelja);
	    	//linije[id].stampajRedVoznje();
	    }
	    
	    try
	    {
	    	if(connection != null)
	    		connection.close();
	    	
	    	if(rvConnection != null)
	    		rvConnection.close();
	    	
	    } catch(SQLException e)
	    {
	    	// connection close failed.
	    	ServerLog.getInstance().write(e.getMessage());
	    }
	}

	
	public void resetCSInfo()
	{
		for(Linija l : linije)
			if(l != null)
				l.resetujCSInfo();
	}
	
	public void addCSInfo(CSInfo csInfo)
	{
		ArrayList<Linija> linijeZaProveru = new ArrayList<>();
		LocalDateTime currentTime = LocalDateTime.now();
		
		for(Linija l : linije)
			if(l != null && csInfo.brojLinije.equalsIgnoreCase(l.broj.replace("*", "")) && csInfo.smerLinije.equalsIgnoreCase(l.smer))
				linijeZaProveru.add(l);
		
		int minRazlika = Integer.MAX_VALUE;
		double targetDistance = 0.0;
		boolean minusPredznak = false;
		int hourIndex = 0, minuteIndex = 0;
		DayOfWeek day = LocalDateTime.now().getDayOfWeek();
		Linija targetLinija = null;
		int targetMat[][] = null;
		
		for(Linija l : linijeZaProveru)
		{
			double distance = l.calcDistance(l.pocetnaStanica, graf.vratiCvor(csInfo.stanica)) + csInfo.udaljenost;
			//double brzina;
			double proputovanoVreme = distance / l.vratiTrenutnuBrzinu();
			
			LocalDateTime targetDateTime = LocalDateTime.now();
			LocalDateTime sourceDateTime;
			Date realLifeDate = new Date();

			long realLifeSeconds = realLifeDate.getTime() / 1000;
			long sourceSeconds = realLifeSeconds - (int) proputovanoVreme;
			
			sourceDateTime = targetDateTime.minusDays(((int) proputovanoVreme)/86400);
			sourceDateTime = sourceDateTime.minusHours((((int) proputovanoVreme)/3600)%24);
			sourceDateTime = sourceDateTime.minusMinutes((((int) proputovanoVreme)/60)%60);
			sourceDateTime = sourceDateTime.minusSeconds(((int) proputovanoVreme)%60);
			
			int mat[][] = null;
			boolean found = false;
			
			if(sourceDateTime.getDayOfWeek() == DayOfWeek.SATURDAY)
				mat = l.matSubota;
			else if(sourceDateTime.getDayOfWeek() == DayOfWeek.SUNDAY)
				mat = l.matNedelja;
			else
				mat = l.matRadni;
			
			//provera za bus koji je krenuo kasnije od ocenjenog vremena
			//proveri trenutni cas prvo
			int h = sourceDateTime.getHour();
			for(int i = 0; i < 60 && mat[h][i] != -1; ++i)
				if(mat[h][i] >= sourceDateTime.getMinute())
				{
					found = true;
					int razlika;
					
					if(mat[h][i] == sourceDateTime.getMinute())
						razlika = sourceDateTime.getSecond();
					else
						razlika = 60-sourceDateTime.getSecond() + (mat[h][i]-1-sourceDateTime.getMinute())*60;
					
					if(minRazlika > razlika)
					{
						targetDistance = distance;
						targetMat = mat;
						minusPredznak = true;
						minRazlika = razlika;
						hourIndex = h;
						minuteIndex = i;
						day = sourceDateTime.getDayOfWeek();
						targetLinija = l;
					}
					break;
				}
			
			if(!found)
			{
				++h;
				for(int i = 0; i < 60 && mat[h][i] != -1; ++i)
				{
					found = true;
					int razlika = 60-sourceDateTime.getSecond() + (59-sourceDateTime.getMinute())*60
									+ mat[h][i]*60;
						
					if(minRazlika > razlika)
					{
						targetDistance = distance;
						targetMat = mat;
						minusPredznak = true;
						minRazlika = razlika;
						hourIndex = h;
						minuteIndex = i;
						day = sourceDateTime.getDayOfWeek();
						targetLinija = l;
					}
					
					break;
				}
			}
			
			//provera za bus koji je krenuo ranije od ocenjenog vremena
			h = sourceDateTime.getHour();
			found = false;
			for(int i = 0; i < 60 && mat[h][i] != -1; ++i)
				if(mat[h][i] < sourceDateTime.getMinute())
				{
					found = true;
					int razlika = 60*(sourceDateTime.getMinute() - mat[h][i]) + sourceDateTime.getSecond();
					
					
					if(minRazlika > razlika)
					{
						targetDistance = distance;
						targetMat = mat;
						minusPredznak = false;
						minRazlika = razlika;
						hourIndex = h;
						minuteIndex = i;
						day = sourceDateTime.getDayOfWeek();
						targetLinija = l;
					}

				}
			
			if(!found && h >= 1)
			{
				--h;
				for(int i = 0; i < 60 && mat[h][i] != -1; ++i)
				{
					found = true;
					int razlika = 60*sourceDateTime.getMinute() + sourceDateTime.getSecond()
									+ 60*(60-mat[h][i]);
						
					if(minRazlika > razlika)
					{
						targetDistance = distance;
						targetMat = mat;
						minusPredznak = false;
						minRazlika = razlika;
						hourIndex = h;
						minuteIndex = i;
						day = sourceDateTime.getDayOfWeek();
						targetLinija = l;
					}
					
				}
			}
		}
		
		if(targetLinija != null)
		{
			/*if(targetLinija.cSourcedData[hourIndex][minuteIndex] == null)
			{
				System.out.println("Kreiran csInfo za liniju " + targetLinija.broj + targetLinija.smer +
						", za bus u " + hourIndex + ":" + targetMat[hourIndex][minuteIndex]);
				targetLinija.cSourcedData[hourIndex][minuteIndex] = csInfo;
			}
			else
			{
				System.out.println("Dodat csInfo za liniju " + targetLinija.broj + targetLinija.smer +
						", za bus u " + hourIndex + ":" + targetMat[hourIndex][minuteIndex]);
				targetLinija.cSourcedData[hourIndex][minuteIndex].usrednji(csInfo);
			}*/
			//dodaj za svaku nit!!!
			//targetLinija.dodajCSInfo(csInfo, day, hourIndex, minuteIndex);
			
			double proputovanoVreme = targetDistance / targetLinija.vratiTrenutnuBrzinu();
			
			if(minusPredznak)
				proputovanoVreme -= minRazlika;
			else
				proputovanoVreme += minRazlika;
			
			double speed = targetDistance / proputovanoVreme;
			
			for(ClientWorker worker : Main.workerPool)
			{
				worker.getGradskeLinije().linije[targetLinija.id].dodajCSInfo(csInfo, day, hourIndex, minuteIndex);
				worker.getGradskeLinije().linije[targetLinija.id].dodajBrzinu(speed);
			}
			//targetLinija.dodajBrzinu(speed);
			
			if(csInfo.kontrola)
				Main.dodajKontrolu(new CoordTimestamp(csInfo.lat, csInfo.lon));
		}
		
	}
	
}

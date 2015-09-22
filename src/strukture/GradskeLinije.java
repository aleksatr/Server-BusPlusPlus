package strukture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import server.*;

public class GradskeLinije
{
	//public ArrayList<Linija> linije = new ArrayList<>();
	public Linija linije[];		//id linija u bazi odgovara indeksu u ovom nizu
	
	public GradskeLinije(int size) 
	{
		linije = new Linija[size];
	}
			
	public GradskeLinije(String grafDBName, String redVoznjeDBName) throws ClassNotFoundException, SQLException, Exception
	{
		int maxId = 0;
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

}

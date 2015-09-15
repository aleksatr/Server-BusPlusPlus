package strukture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import server.*;

public class Graf
{
	private ArrayList<Cvor> cvorovi = new ArrayList<>();
	private GradskeLinije gl;
	
	public Graf() {}
	
	public Graf(String grafDBName, String redVoznjeDBName) throws ClassNotFoundException, SQLException, Exception
	{
		int maxId = 0;
		Cvor tempArray[] = null;
		
		gl = new GradskeLinije(grafDBName, redVoznjeDBName);
		
		// load the sqlite-JDBC driver using the current class loader
	    Class.forName("org.sqlite.JDBC");
	    
	    Connection connection = null;
	    
	    // create a database connection
	    connection = DriverManager.getConnection("jdbc:sqlite:" + grafDBName);
	    Statement statement = connection.createStatement();
	    statement.setQueryTimeout(30);  // set timeout to 30 sec.
	    
	    ResultSet rs = statement.executeQuery("select max(id) from STANICA");
	    
	    if(rs.next())
	    {
	    	maxId = rs.getInt(1);
	    }
	    else
	    {
	    	System.out.println("select max(id) from STANICA; query nije vratio rezultat");
	    	throw new Exception("select max(id) from STANICA; query nije vratio rezultat");
	    }
	    
	    tempArray = new Cvor[maxId + 1];
	    
	    rs = statement.executeQuery("select * from STANICA");
	    
	    while(rs.next())
	    {
	    	// read the result set
	    	int id = rs.getInt("id");
	    	String naziv = rs.getString("naziv");
	    	double lat = rs.getDouble("lat");
	    	double lon = rs.getDouble("lon");

	    	tempArray[id] = new Cvor(id, naziv, lat, lon);
	    }
	    
	    for(int i = 0; i < gl.linije.length; ++i)
	    {
	    	if(gl.linije[i] != null)
	    		gl.linije[i].pocetnaStanica = tempArray[gl.linije[i].pocetnaStanica.id];
	    }
	    
	    rs = statement.executeQuery("select * from VEZA");
	    
	    while(rs.next())
	    {
	    	// read the result set
	    	int sourceId = rs.getInt("polazna_stanica_id");
	    	int destId = rs.getInt("dolazna_stanica_id");
	    	int weight = rs.getInt("udaljenost");
	    	int linijaId = rs.getInt("linija_id");

	    	tempArray[sourceId].dodajVezu(gl.linije[linijaId], weight, tempArray[destId]);;
	    }
	    
	    try
	    {
	    	if(connection != null)
	    		connection.close();
	    } catch(SQLException e)
	    {
	    	// connection close failed.
	    	ServerLog.getInstance().write(e.getMessage());
	    }
	    
	    for(int i = 0; i < tempArray.length; ++i)
	    {
	    	if(tempArray[i] != null)
	    	{
	    		cvorovi.add(tempArray[i]);
	    		tempArray[i] = null;
	    	}
	    }
	}
	
	public void pratiLiniju(int linijaId)
	{
		Linija l = gl.linije[linijaId];
		int udaljenost = 0;
		if(l == null)
			return;
		
		Cvor c = l.pocetnaStanica;
		Cvor pocetna = c;
		Veza v = null;
		
		System.out.println("Linija:> " + l.toString());
		System.out.println(c.toString() + " udaljenost = " + udaljenost);
		while((v = c.vratiVezu(l)) != null)
		{
			c = v.destination;
			udaljenost += v.weight;
			System.out.println(c.toString() + " udaljenost = " + udaljenost);
			if(c == pocetna)
				break;
		}
	}
}

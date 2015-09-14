package strukture;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import server.*;

public class GradskeLinije
{
	//public ArrayList<Linija> linije = new ArrayList<>();
	public Linija linije[];
	
	public GradskeLinije(int size) 
	{
		linije = new Linija[size];
	}
			
	public GradskeLinije(String grafDBName, String redVoznjeDBName) throws ClassNotFoundException, SQLException, Exception
	{
		int maxId = 0;
		// load the sqlite-JDBC driver using the current class loader
	    Class.forName("org.sqlite.JDBC");
	    
	    Connection connection = null;
	    
	    // create a database connection
	    connection = DriverManager.getConnection("jdbc:sqlite:" + grafDBName);
	    Statement statement = connection.createStatement();
	    statement.setQueryTimeout(30);  // set timeout to 30 sec.
	    
	    ResultSet rs = statement.executeQuery("select max(id) from LINIJA");
	    
	    if(rs.next())
	    {
	    	maxId = rs.getInt(1);
	    }
	    else
	    	throw new Exception("select max(id) from LINIJA; query nije vratio rezultat");
	    
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
	    	linije[id] = new Linija(id, broj, smer, naziv, pocetnaStanica);
	    	//linije.add(new Linija(id, broj, smer, naziv, pocetnaStanica));
	    	//System.out.println("name = " + rs.getString("name"));
	    	//System.out.println("id = " + rs.getInt("id"));
	    	//int maxId = rs.getInt(1);
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
	}
}

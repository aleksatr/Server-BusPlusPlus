package server;

import datalayer.*;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Response
{
	public Integer type = null; 								//tip request = tip response
	//public Integer najblizaStanica = null; 					//id stanice za klasican red voznje, najbliza stanica na kojoj se ceka trazena linija+smer
	public Integer stanice[] = null; 							//id stanica za napredni red voznje
	public Integer linije[] = null; 							//id linije kojom je putnik stigo do stanice[i], ili -1 ako je iso pesaka
	public ArrayList<DatumVremeStanica> vremenaDolaska = null; 	//vremena dolaska buseva na stanice, za napredni red voznje se salju samo za stanice na kojima se preseda, kad se menja linija
																//za klasican red voznje su tu vreman dolaska na najblize stanice (vise njih zbog 3, 3*, 3**)
	
	public Response() {}
	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}

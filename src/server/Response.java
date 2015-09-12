package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Response
{
	int stanica = 0; //id stanice za klasican red voznje, najbliza stanica na kojoj se ceka trazena linija+smer
	
	public Response() {}
	
	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		return gson.toJson(this);
	}
}

package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import com.google.gson.*;

import strukture.*;

public class ClientWorker implements Runnable
{
	private Socket clientSocket;
	private ServerLog log;
	private Pleaser pleaser = null;
	
	public ClientWorker(Socket client)
	{
		clientSocket = client;
		this.log = ServerLog.getInstance();
	}

	@Override
	public void run()
	{
		InputStream istream = null;
		OutputStream ostream = null;
	    
	    try
	    {
	    	istream = clientSocket.getInputStream();
	    	ostream = clientSocket.getOutputStream();
	    	
	    	pleaser = new Pleaser(clientSocket, istream, ostream);
	    	
	    	pleaser.please();
	    } catch (IOException e) 
	    {
	    	log.write("failed to create pleaser because of istream and ostream");;
	    	log.write(e.getMessage());
	    }
	    finally
	    {
	    	try
			{
		    	clientSocket.close();
		    } catch (IOException e) {
		    	log.write("Exception caught while trying to close client socket");
		    }
	    }
	}
	
}

//logs = gson.fromJson(br, new TypeToken<List<JsonLog>>(){}.getType());

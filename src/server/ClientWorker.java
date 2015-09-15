package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.sql.SQLException;

import com.google.gson.*;

import strukture.*;

public class ClientWorker implements Runnable
{
	private Graf graf;
	private Socket clientSocket = null;
	private ServerLog log;
	private Pleaser pleaser = null;
	private int id;
	private LinkedBlockingQueue<Socket> requestsQueue;
	
	public ClientWorker(int id, LinkedBlockingQueue<Socket> requestsQueue/*Socket client*/)
	{
		this.id = id;
		//clientSocket = client;
		this.log = ServerLog.getInstance();
		this.requestsQueue = requestsQueue;
	}

	@Override
	public void run()
	{
		log.write("Thread[" + id + "]~" + " STARTED");
		try
		{
			graf = new Graf(ServerConsts.SQLITE_GRAF_DB_NAME, ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
		} catch(SQLException e)
		{
			// if the error message is "out of memory", 
		    // it probably means no database file is found
			log.write("Thread[" + id + "]~" + " Exception caught when trying create grafPrototype from " + ServerConsts.SQLITE_GRAF_DB_NAME);
			log.write(e.getMessage());
			log.write("Bus++ thread[" + id + "] terminated");
			
			return;
		} catch(ClassNotFoundException e)
		{
			// if the error message is "out of memory", 
		    // it probably means no database file is found
			log.write("!Thread[" + id + "]~" + " Exception caught when trying create grafPrototype from " + ServerConsts.SQLITE_GRAF_DB_NAME);
			log.write(e + "");
			log.write("!Bus++ thread[" + id + "] terminated");
			
			return;
		} catch(Exception e)
		{
			// if the error message is "out of memory", 
		    // it probably means no database file is found
			log.write("!!Thread[" + id + "]~" + " Exception caught when trying create grafPrototype from " + ServerConsts.SQLITE_GRAF_DB_NAME);
			log.write(e + "");
			log.write("!!Bus++ thread[" + id + "] terminated");
			
			return;
		}
		
		InputStream istream = null;
		OutputStream ostream = null;
		
		while(true)
		{
			try
			{
				clientSocket = requestsQueue.take();
				//System.out.println("Thread["+ id + "] hendluje socket");
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int lineID = 0;
			try
		    {
		    	istream = clientSocket.getInputStream();
		    	ostream = clientSocket.getOutputStream();
		    	
		    	BufferedReader in = null;
		    	PrintWriter out = null;
		    	
		    	in = new BufferedReader(new InputStreamReader(istream));
				out = new PrintWriter(ostream);
				
				String line = null;
				
				try
				{
					line = in.readLine();
					
					if(line == null)
					{
						System.out.println("Thread["+ id + "] line je null");
						return;
					}
					
					//Request rq = gson.fromJson(line, Request.class);
					
					System.out.println(line);
					
					lineID = Integer.parseInt(line);
						
					
					System.out.println("Thread["+ id + "] hendluje request");
					graf.pratiLiniju(lineID);
					
				} catch (IOException e)
				{
					log.write(e.getMessage());
				}
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
		
		
		
		//stavi infinite loop, na pocetak svake iteracije uzmi clientSocket iz reda
		/*InputStream istream = null;
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
	    }*/
	}
	
}

//logs = gson.fromJson(br, new TypeToken<List<JsonLog>>(){}.getType());

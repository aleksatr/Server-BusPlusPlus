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
	
	public ClientWorker(int id, LinkedBlockingQueue<Socket> requestsQueue)
	{
		this.id = id;
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
			log.write("Thread[" + id + "]~" + " Exception caught when trying create grafPrototype from " + ServerConsts.SQLITE_GRAF_DB_NAME + " and " + ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
			log.write(e.getMessage());
			log.write("Bus++ thread[" + id + "] terminated");
			
			return;
		} catch(ClassNotFoundException e)
		{
			log.write("!Thread[" + id + "]~" + " Exception caught when trying create grafPrototype from " + ServerConsts.SQLITE_GRAF_DB_NAME + " and " + ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
			log.write(e + "");
			log.write("!Bus++ thread[" + id + "] terminated");
			
			return;
		} catch(Exception e)
		{
			log.write("!!Thread[" + id + "]~" + " Exception caught when trying create grafPrototype from " + ServerConsts.SQLITE_GRAF_DB_NAME + " and " + ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
			log.write(e + "");
			log.write("!!Bus++ thread[" + id + "] terminated");
			
			return;
		}
		
		pleaser = new Pleaser(this);
		
		
		while(true)
		{
			try
			{
				clientSocket = requestsQueue.take();
			} catch (InterruptedException e)
			{
				log.write("Thread["+ id + "] " + e.getMessage());
			}

			pleaser.please(clientSocket);
		   
		    try
			{
			    clientSocket.close();
			} catch (IOException e) {
			    log.write("Thread["+ id + "] " + "Exception caught while trying to close client socket");
			}
		    
		}
		
	}
	
	public int getId()
	{
		return id;
	}
	
}

//logs = gson.fromJson(br, new TypeToken<List<JsonLog>>(){}.getType());

package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.sql.SQLException;

import com.google.gson.*;

import strukture.*;

public class Main
{
	private static Graf grafPrototype;
	private static LinkedBlockingQueue<Socket> requestsQueue = new LinkedBlockingQueue<>(ServerConsts.REQUEST_QUEUE_SIZE);
	private static ArrayList<Thread> threadPool = new ArrayList<>();
	private static Graf grafPool[] = new Graf[ServerConsts.WORKER_THREAD_POOL_SIZE];
	
	private static void populateThreadPool()
	{
		ClientWorker workerThread = null;
		Thread t = null;
		
		for(int i = 0; i < ServerConsts.WORKER_THREAD_POOL_SIZE; ++i)
		{
			workerThread = new ClientWorker(i);
			t = new Thread(workerThread);
			threadPool.add(t);
			t.start();
		}
	}
	
	private static void createGrafPrototype() throws ClassNotFoundException, SQLException, Exception
	{
		grafPrototype = new Graf(ServerConsts.SQLITE_GRAF_DB_NAME, ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
	}
	
	public static void main(String[] args)
	{
		ServerSocket serverSocket = null;
		ServerLog log = ServerLog.getInstance();
		log.write("Bus++ Server started");
		
		try
		{
			createGrafPrototype();
		} catch(Exception e)
		{
			// if the error message is "out of memory", 
		    // it probably means no database file is found
			log.write("Exception caught when trying create grafPrototype from " + ServerConsts.SQLITE_GRAF_DB_NAME);
			log.write(e.getMessage());
			log.write("Bus++ Server terminated");
			
			log.freeResources();
			System.exit(-1);
		}
		
		populateThreadPool();
		
		try
		{
			serverSocket = new ServerSocket(ServerConsts.PORT);
		} catch(IOException e) 
		{
			log.write("Exception caught when trying to listen on port " + ServerConsts.PORT);
			log.write(e.getMessage());
			log.write("Bus++ Server terminated");
			
			log.freeResources();
			System.exit(-1);
		}
		
		while(true)
		{
			//ClientWorker workerThread = null;
			
			try
			{
				requestsQueue.put(serverSocket.accept());
				/*workerThread = new ClientWorker(serverSocket.accept());
				Thread t = new Thread(workerThread);
				t.start();*/
			} catch(Exception e)
			{
				if(serverSocket != null)
				{
					try
					{
						serverSocket.close();
					} catch (IOException e1)
					{
						log.write("Exception caught while trying to close server socket");
						log.write(e1.getMessage());
					}
				}
				
				log.write("Exception caught while trying to accept connection on: " + ServerConsts.PORT);
				log.write(e.getMessage());
				log.write("Bus++ Server terminated");
				
				log.freeResources();
				System.exit(-1);
			}
		}
		
	}

}


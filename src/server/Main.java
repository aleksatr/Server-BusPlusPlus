package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import com.google.gson.*;

import strukture.*;

public class Main
{
	
	public static void main(String[] args)
	{
		/*Gson gson = new GsonBuilder().create();
		Integer niz[] = new Integer[100];
		
		niz[0] = 0;
		
		niz[10] = 10;
		
		niz[99] = 100;
		
		ArrayList<Integer> wat = new ArrayList<>();*/
		
		/*wat.add(1, 2);
		wat.add(10, 3);
		wat.add(100, 4);*/
		
		//System.out.println(gson.toJson(1));
		
		
		ServerLog log = ServerLog.getInstance();
		ServerSocket serverSocket = null;
		
		log.write("Bus++ Server started");
		
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
			ClientWorker workerThread = null;
			
			try
			{
				workerThread = new ClientWorker(serverSocket.accept());
				Thread t = new Thread(workerThread);
				t.start();
			} catch(IOException e)
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


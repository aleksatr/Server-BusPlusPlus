package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import strukture.*;

public class Main
{
	
	public static void main(String[] args)
	{
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


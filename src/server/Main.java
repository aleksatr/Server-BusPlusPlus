package server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.*;

import datalayer.CoordTimestamp;
import strukture.*;

public class Main
{
	//private static Graf grafPrototype;
	private static LinkedBlockingQueue<Socket> requestsQueue = new LinkedBlockingQueue<>(ServerConsts.REQUEST_QUEUE_SIZE);
	private static ArrayList<Thread> threadPool = new ArrayList<>();
	public static ArrayList<ClientWorker> workerPool = new ArrayList<>();
	//private static Graf grafPool[] = new Graf[ServerConsts.WORKER_THREAD_POOL_SIZE];
	private static ArrayList<CoordTimestamp> kontrole = new ArrayList<>();
	
	private static void populateThreadPool()
	{
		ClientWorker workerThread = null;
		Thread t = null;
		
		for(int i = 0; i < ServerConsts.WORKER_THREAD_POOL_SIZE; ++i)
		{
			workerThread = new ClientWorker(i, requestsQueue);
			t = new Thread(workerThread);
			threadPool.add(t);
			workerPool.add(workerThread);
			t.start();
		}
	}
	
	/*private static Graf createGrafPrototype() throws ClassNotFoundException, SQLException, Exception
	{
		Graf grafPrototype = new Graf(ServerConsts.SQLITE_GRAF_DB_NAME, ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
		return grafPrototype;
	}*/
	
	public static void main(String[] args)
	{
		ServerSocket serverSocket = null;
		ServerLog log = ServerLog.getInstance();
		log.write("Bus++ Server started");
		
		populateThreadPool();
		
		FileReader dbVerInputStream = null;
		BufferedReader dbVerBufferedInput = null;
		
		try
		{
			dbVerInputStream = new FileReader(ServerConsts.DB_VER_FILE);
			dbVerBufferedInput = new BufferedReader(dbVerInputStream);
			
			String verStr = dbVerBufferedInput.readLine();
			ServerConsts.grafDBVer = Double.parseDouble(verStr);
			
			verStr = dbVerBufferedInput.readLine();
			ServerConsts.rVoznjeDBVer = Double.parseDouble(verStr);
			
			verStr = dbVerBufferedInput.readLine();
			ServerConsts.putanjeDBVer = Double.parseDouble(verStr);
		} catch (FileNotFoundException e)
		{
			log.write("Exception caught when trying to open file " + ServerConsts.DB_VER_FILE);
			log.write(e.getMessage());
		} catch (IOException e)
		{
			log.write("Exception caught when trying to read file " + ServerConsts.DB_VER_FILE);
			log.write(e.getMessage());
		} finally
		{
			if(dbVerBufferedInput != null)
				try
				{
					dbVerBufferedInput.close();
				} catch (IOException e)
				{
					log.write("Exception caught when trying to close file " + ServerConsts.DB_VER_FILE);
					log.write(e.getMessage());
				}
		}
		
		
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
			
			try
			{
				requestsQueue.put(serverSocket.accept());
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
	
	public static synchronized void dodajKontrolu(CoordTimestamp kontrola)
	{
		ArrayList<CoordTimestamp> zaBrisanje = new ArrayList<>();
		LocalDateTime trenutnoVreme = LocalDateTime.now();
		
		long trenutnoSeconds = trenutnoVreme.atZone(ZoneId.systemDefault()).toEpochSecond();
		long stariSeconds;
		
		for(CoordTimestamp ct : Main.kontrole)
		{
			stariSeconds = ct.timestamp.atZone(ZoneId.systemDefault()).toEpochSecond();
			if(trenutnoSeconds - stariSeconds > ServerConsts.vremeVazenjaKontrole)
				zaBrisanje.add(ct);
		}
		
		for(CoordTimestamp ct : zaBrisanje)
		{
			//System.out.println("Obrisana kontrola " + ct.lat + "  " + ct.lon + "  " + ct.timestamp);
			ServerLog.getInstance().write("Obrisana kontrola " + ct.lat + "  " + ct.lon + "  " + ct.timestamp);
			Main.kontrole.remove(ct);
		}
	
		Main.kontrole.add(kontrola);
		ServerLog.getInstance().write("Dodata kontrola " + kontrola.lat + "  " + kontrola.lon + "  " + kontrola.timestamp);
		//System.out.println("Dodata kontrola " + kontrola.lat + "  " + kontrola.lon + "  " + kontrola.timestamp);
	}
	
	public static synchronized ArrayList<CoordTimestamp> vratiKopijuKontrola()
	{
		ArrayList<CoordTimestamp> kopija = new ArrayList<>();
		ArrayList<CoordTimestamp> zaBrisanje = new ArrayList<>();
		LocalDateTime trenutnoVreme = LocalDateTime.now();
		
		long trenutnoSeconds = trenutnoVreme.atZone(ZoneId.systemDefault()).toEpochSecond();
		long stariSeconds;
		
		for(CoordTimestamp ct : Main.kontrole)
		{
			stariSeconds = ct.timestamp.atZone(ZoneId.systemDefault()).toEpochSecond();
			if(trenutnoSeconds - stariSeconds > ServerConsts.vremeVazenjaKontrole)
				zaBrisanje.add(ct);
			else
				kopija.add(new CoordTimestamp(ct));
		}
		
		for(CoordTimestamp ct : zaBrisanje)
		{
			ServerLog.getInstance().write("Obrisana kontrola " + ct.lat + "  " + ct.lon + "  " + ct.timestamp);
			//System.out.println("Obrisana kontrola " + ct.lat + "  " + ct.lon + "  " + ct.timestamp);
			Main.kontrole.remove(ct);
		}
		
		/*for(CoordTimestamp ct : Main.kontrole)
			kopija.add(new CoordTimestamp(ct));*/
		
		return kopija;
	}

}


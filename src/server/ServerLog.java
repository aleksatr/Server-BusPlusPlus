package server;

import java.io.*;
import java.time.LocalDateTime;

public class ServerLog
{
	private PrintWriter logStream = null;
	private LocalDateTime time = null;
	private static ServerLog instance = null;
	
	private ServerLog()
	{
		try
		{
			logStream = new PrintWriter(new BufferedWriter(new FileWriter(ServerConsts.LOG_FILE, true)));
		} catch(IOException e)
		{
			System.out.println("Error openig log file!");
			System.out.println(e.toString());
		}
	}
	
	public static ServerLog getInstance()
	{
		if(instance == null) 
			instance = new ServerLog();
		
	      return instance;
	}
	
	public synchronized void write(String s)
	{
		time = LocalDateTime.now();
		
		if(logStream != null)
		{
			logStream.println("[" + time + "]~  " + s);
			logStream.flush();
		}
		else
			System.out.println("[" + time + "]~  " + s);
	}
	
	public synchronized void freeResources()
	{
		if(logStream != null)
			logStream.close();
	}
}

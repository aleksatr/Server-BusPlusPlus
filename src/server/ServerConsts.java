package server;

public final class ServerConsts
{
	public static final int PORT = 4000;
	public static final String LOG_FILE = "bpp.log";
	public static final String SQLITE_GRAF_DB_NAME = "bpp.db";
	public static final String SQLITE_RED_VOZNJE_DB_NAME = "red_voznje.db";
	public static final int REQUEST_QUEUE_SIZE = 100;
	public static final int WORKER_THREAD_POOL_SIZE = 1;
	public static final String DB_VER_FILE = "db.ver";
	public static double grafDBVer = 0;
	public static double rVoznjeDBVer = 0;
	
	private ServerConsts()
	{
		throw new AssertionError();
	}
}


//tip requesta 0 - zahtev za proveru verzije baze sa grafom, nazad se salje response i baza ako je potrebno
//tip requesta 1 - zahtev za proveru verzije baze sa redom voznje, nazad se salje response i baza ako je potrebno


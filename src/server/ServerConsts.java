package server;

public final class ServerConsts
{
	public static final int PORT = 4000;
	public static final int MARIADB_PORT_NUMBER = 3306;
	public static final String MARIADB_USER = "root";
	public static final String MARIADB_PASS = "dahaka";
	public static final String MARIADB_DATABASE_NAME = "PlaninarijumX";
	public static final String LOG_FILE = "bpp.log";
	public static final String SLIKE_PATH = "./slike/";
	public static final String DEFAULT_SLIKA = "NN.png";
	public static final int PING_TIMEOUT = 10; //sec
	public static final String SQLITE_GRAF_DB_NAME = "bpp.db";
	public static final String SQLITE_RED_VOZNJE_DB_NAME = "";
	
	public static final int REQUEST_QUEUE_SIZE = 100;
	public static final int WORKER_THREAD_POOL_SIZE = 10;
	
	private ServerConsts()
	{
		throw new AssertionError();
	}
}

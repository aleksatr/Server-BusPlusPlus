package server;

public final class ServerConsts
{
	public static final int PORT = 4000;
	public static final String LOG_FILE = "bpp.log";
	public static final String SQLITE_GRAF_DB_NAME = "bpp.db";
	public static final String SQLITE_RED_VOZNJE_DB_NAME = "red_voznje.db";
	public static final String SQLITE_PUTANJE_BUSEVA_DB_NAME = "putanje_buseva.db";
	public static final int REQUEST_QUEUE_SIZE = 100;
	public static final int WORKER_THREAD_POOL_SIZE = 10;
	public static final String DB_VER_FILE = "db.ver";
	public static final Double brzinaAutobusa = 5.4;								//brzina autobusa u m/s
	public static final Double brzinaPesaka = 0.7659;								//brzina pesaka u m/s
	public static final Double brzinaPesakaZaMinWalk = 0.0001;						//brzina pesaka u m/s za rezim minimalnog pesacenja
	public static final Double maksimalnaBrzinaAutobusa = 13.9;						//maksimalna dozvoljena brzina u gradu
	public static double grafDBVer = 0;
	public static double rVoznjeDBVer = 0;
	public static double putanjeDBVer = 0;
	public static final int vremeVazenjaKontrole = 900;								//u sekundama
	public static final String passwordZaReinicijalizaciju = "dahaka";
	
	public static final Double raspodelaBrzina[] = { 
														8.3,	//00-01
														8.3,	//01-02
														8.0,	//02-03
														7.0,	//03-04
														6.5,	//04-05
														5.4,	//05-06
														5.0,	//06-07
														4.5,	//07-08
														4.15,	//08-09
														5.0,	//09-10
														5.4,	//10-11
														6.0,	//11-12
														6.0,	//12-13
														5.4,	//13-14
														4.5,	//14-15
														4.15,	//15-16
														5.0,	//16-17
														5.4,	//17-18
														6.0,	//18-19
														6.0,	//19-20
														7.0,	//20-21
														7.5,	//21-22
														8.0,	//22-23
														8.3		//23-24
											 		};
	
	private ServerConsts()
	{
		throw new AssertionError();
	}
}


//tip requesta 0 - zahtev za proveru verzije baze sa grafom, nazad se salje response i baza ako je potrebno
//tip requesta 1 - zahtev za proveru verzije baze sa redom voznje, nazad se salje response i baza ako je potrebno


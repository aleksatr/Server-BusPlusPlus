package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

import javax.sound.sampled.TargetDataLine;

import com.google.gson.*;

import datalayer.DatumVremeStanica;
import strukture.*;


public class RequestHandler
{
	private InputStream istream;
	private OutputStream ostream;
	private Socket clientSocket;
	private ServerLog log;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private Request req = null;
	private ClientWorker owner;
	
	private double matricaUdaljenosti[][] = null; 			//matricaUdaljenosti[startCvor.id][finishCvor.id] = udaljenost start-finish
	
	Gson gson;
	
	public RequestHandler(ClientWorker owner)
	{
		this.owner = owner;
		this.log = ServerLog.getInstance();
		this.gson = new GsonBuilder().create();
		
		//inicijalizacija matrice udaljenosti
		//pomocniNizCvorova = owner.getGraf().getStanice().toArray(new Cvor[owner.getGraf().getStanice().size()]);
		
		ArrayList<Cvor> stanice = owner.getGraf().getStanice();
		int maxId = -1;
		for(int i = 0; i < stanice.size(); ++i)
		{
			Cvor c = stanice.get(i);
			if(c.id > maxId)
				maxId = c.id;
		}
		
		matricaUdaljenosti = new double[maxId + 3][maxId + 3];
		
		for(int i = 0; i < stanice.size(); ++i)
			for(int j = 0; j < stanice.size(); ++j)
			{
				Cvor start = stanice.get(i);
				Cvor finish = stanice.get(j);
				matricaUdaljenosti[start.id][finish.id] = owner.getGraf().calcDistance(start, finish);
			}
	}
	
	public void handle(Socket clientSocket)
	{
		String line = null;
		
		try
		{
			this.clientSocket = clientSocket;
			this.istream = clientSocket.getInputStream();
			this.ostream = clientSocket.getOutputStream();
			this.in = new BufferedReader(new InputStreamReader(istream));
			this.out = new PrintWriter(ostream);
			
			line = in.readLine();
			
			if(line == null)
				return;
			
			log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " REQUEST= " + line);
			
			req = gson.fromJson(line, Request.class);
			
			////////////////////////////////////
			/*Linija l = owner.getGradskeLinije().linije[10]; //linija
			Cvor c = null;
			
			//odstampaj red voznje
			for(int i = 0; i < 25; ++i)
			{
				System.out.print(i + ": ");
				for(int j = 0; j < 60; ++j)
					if(l.matRadni[i][j] != -1)
						System.out.print(l.matRadni[i][j] + ", ");
					else
						break;
				System.out.println();
			}
			for(int i = 0; i < 25; ++i)
			{
				System.out.print(i + ": ");
				for(int j = 0; j < 60; ++j)
					if(l.matSubota[i][j] != -1)
						System.out.print(l.matSubota[i][j] + ", ");
					else
						break;
				System.out.println();
			}
			for(int i = 0; i < 25; ++i)
			{
				System.out.print(i + ": ");
				for(int j = 0; j < 60; ++j)
					if(l.matNedelja[i][j] != -1)
						System.out.print(l.matNedelja[i][j] + ", ");
					else
						break;
				System.out.println();
			}
			
			for(int i = 0; i < owner.getGraf().getStanice().size(); ++i)
			{
				c = owner.getGraf().getStanice().get(i);
				if(c.id == 207) //stanica
					break;
			}
			System.out.println();
			System.out.println("kasnjenje = " + izracunajKasnjenjeLinije2(l, c, ServerConsts.brzinaAutobusa));*/
			///////////////////////////////////
			//System.out.println(calcDistance(43.321124, 21.895838, 43.342670, 21.879702) + " / " + ServerConsts.brzinaPesaka);
			switch(req.type)
			{
			case 0:
				handleRequest0(req);
				break;
			case 1:
				handleRequest1(req);
				break;	
			case 3:
				handleRequest3(req);																	//klasicni red voznje
				break;
			case 4:
				handleRequest4(req);																	//ekonomicni
				break;
			case 5:
				handleRequest5(req);																	//optimalni ekonomicni
				break;
			case 6:
				handleRequest6(req, ServerConsts.brzinaAutobusa, ServerConsts.brzinaPesaka);			//rezim vremenske optimalnosti
				break;
			case 7:
				handleRequest6(req, ServerConsts.brzinaAutobusa, ServerConsts.brzinaPesakaZaMinWalk);	//MIN WALK - rezim minimalnog pesacenja
				break;
			default:
				log.write("Thread["+ owner.getId() + "] " +"Nepoznat request type = " + req.type);
				break;
			}

			
		} catch (IOException e)
		{
			log.write("Thread["+ owner.getId() + "] " + "failed to please");
	    	log.write(e.getMessage());
		} catch (Exception e)
		{
			log.write("Thread["+ owner.getId() + "] " + "Nepoznat request format = " + line);
			log.write("Thread["+ owner.getId() + "] " + "failed to please");
	    	log.write(e.getMessage());
		}
		

	}
	
	//klijent proverava da li ima najnoviju verziju baze bpp.db
	private void handleRequest0(Request req)
	{
		
		if(req.dbVer < ServerConsts.grafDBVer)
		{
			File file = new File(ServerConsts.SQLITE_GRAF_DB_NAME);
			
			out.write((new Response(req.type, null, null, null, null, (int) file.length(), ServerConsts.grafDBVer)).toString() + "\n");
			out.flush();
			
			byte[] fileData = new byte[(int) file.length()];
		    DataInputStream dis;
			try
			{
				dis = new DataInputStream(new FileInputStream(file));
				dis.readFully(fileData);
				ostream.write(fileData);
				ostream.flush();
				dis.close();
			} catch (FileNotFoundException e)
			{
				log.write("Thread [" + owner.getId() + "] Exception caught when trying to open file " + ServerConsts.SQLITE_GRAF_DB_NAME);
				log.write(e.getMessage());
			} catch (IOException e)
			{
				log.write("Thread [" + owner.getId() + "] Exception caught when trying to read file " + ServerConsts.SQLITE_GRAF_DB_NAME);
				log.write(e.getMessage());
			}
		    
		} else
		{
			out.write((new Response(req.type, null, null, null, null, -1, null)).toString() + "\n");
			out.flush();
		}
		
	}
	
	//klijent proverava da li ima najnoviju verziju baze red_voznje.db
	private void handleRequest1(Request req)
	{
			
		if(req.dbVer < ServerConsts.rVoznjeDBVer)
		{
			File file = new File(ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
				
			out.write((new Response(req.type, null, null, null, null, (int) file.length(), ServerConsts.rVoznjeDBVer)).toString() + "\n");
			out.flush();
				
			byte[] fileData = new byte[(int) file.length()];
			DataInputStream dis;
			try
			{
				dis = new DataInputStream(new FileInputStream(file));
				dis.readFully(fileData);
				ostream.write(fileData);
				ostream.flush();
				dis.close();
			} catch (FileNotFoundException e)
			{
				log.write("Thread [" + owner.getId() + "] Exception caught when trying to open file " + ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
				log.write(e.getMessage());
			} catch (IOException e)
			{
				log.write("Thread [" + owner.getId() + "] Exception caught when trying to read file " + ServerConsts.SQLITE_RED_VOZNJE_DB_NAME);
				log.write(e.getMessage());
			}
			    
		} else
		{
			out.write((new Response(req.type, null, null, null, null, -1, null)).toString() + "\n");
			out.flush();
		}
			
	}
	
	//klasican red voznje
	private void handleRequest3(Request req)
	{
		Linija linije[] = owner.getGradskeLinije().linije;
		//Graf g = owner.getGraf();
		//ArrayList<Cvor> stanice = g.getStanice();
		ArrayList<Linija> targetLinije = new ArrayList<>();
		
		if(req.linija<1 || req.linija>=linije.length)
			return;
		
			String brojLinije = linije[req.linija].broj.replace("*", "");
			String smer = linije[req.linija].smer;
		
		Linija l = null;
		for(int i = 0; i < linije.length; ++i)
		{
			if(linije[i] == null)
				continue;
			
			if(brojLinije.equals(linije[i].broj.replace("*", "")) && smer.equalsIgnoreCase(linije[i].smer))
				targetLinije.add(linije[i]);
		}
		
		//Integer minDistance = Integer.MAX_VALUE;
		Double minDistance[] = new Double[targetLinije.size()];
		Cvor minDistanceStanice[] = new Cvor[targetLinije.size()];
		
		Integer linijeId[] = new Integer[targetLinije.size()];
		Integer staniceId[] = new Integer[targetLinije.size()];
		Integer korekcije[] = new Integer[targetLinije.size()];
		
		for(int i = 0; i < targetLinije.size(); ++i)
		{
			minDistance[i] = Double.MAX_VALUE;
			Double d;
			
			l = targetLinije.get(i);
			
			linijeId[i] = l.id;
			
			int predjeniPutBusa = 0;
			int predjeniPutBusaDoNajblizeStanice = 0;
			
			Cvor c = l.pocetnaStanica;
			Cvor pocetna = c;
			Veza v = null;
			
			if((d = calcDistance(req.srcLat, req.srcLon, c.lat, c.lon)) < minDistance[i])
			{
				minDistance[i] = d;
				minDistanceStanice[i] = c;
			}
			
			while((v = c.vratiVezu(l)) != null)
			{
				c = v.destination;
				predjeniPutBusa += v.weight;
				if(c == pocetna)
					break;
				
				if((d = calcDistance(req.srcLat, req.srcLon, c.lat, c.lon)) < minDistance[i])
				{
					minDistance[i] = d;
					minDistanceStanice[i] = c;
					predjeniPutBusaDoNajblizeStanice = predjeniPutBusa;
				}
			}
			
			staniceId[i] = minDistanceStanice[i].id;
			
			korekcije[i] = izracunajKorekciju(l, minDistanceStanice[i], predjeniPutBusaDoNajblizeStanice);
		}
		
		String responseStr = (new Response(req.type, staniceId, linijeId, korekcije, null, null, null)).toString();
		log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr);
		
		out.write(responseStr + "\n");
		out.flush();

	}
	
	//ekonomicni (napredni) rezim
	private void handleRequest4(Request req)
	{
		Graf g = owner.getGraf();
		g.resetujCvorove();
		Cvor stanice[] = g.getStanice().toArray(new Cvor[g.getStanice().size()]);
		Cvor sourceCvor = null;
		Linija linije[] = g.getGradskeLinije().linije;
		double pesacenje[] = new double[stanice.length];
		Double minimalnoPesacenje = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon);
		Integer responseStanice[] = new Integer[2]; //responseStanice[0] najbliza source stanica, responseStanice[1] najbliza destination stanica
		ArrayList<Integer> responseLinije = new ArrayList<>();
		
		for(int i = 0; i < stanice.length; ++i)
		{
			pesacenje[i] = calcDistance(stanice[i], req.srcLat, req.srcLon);
		}
		
		for(int i = 1; i < stanice.length; ++i)
		{
	        double key = pesacenje[i];
	        Cvor pomC = stanice[i];
	        int j = i-1;
	        while((j >= 0) && (pesacenje[j] > key))
	        {
	            pesacenje[j+1] = pesacenje[j];
	            stanice[j+1] = stanice[j];
	            --j;
	        }
	        pesacenje[j+1] = key;
	        stanice[j+1] = pomC;
	    }
		
		for(int i = 0; i<stanice.length && minimalnoPesacenje>pesacenje[i]; ++i)
		{	
			for(int j = 0; j < stanice[i].veze.size(); ++j)
			{
				Cvor c = stanice[i];
				Veza v = c.veze.get(j);
				Linija l = v.linija;
				Double d;
				Cvor destStanica = c;
				Double destPesacenje = calcDistance(c, req.destLat, req.destLon);
				
				//System.out.println("LINIJA = " + l);
				
				while((v = c.vratiVezu(l)) != null)
				{
					c = v.destination;
					
					if(c == l.pocetnaStanica)
						break;
					
					if(destPesacenje > (d = calcDistance(c, req.destLat, req.destLon)))
					{
						destStanica = c;
						destPesacenje = d;
					}
				}
				
				if(destPesacenje + pesacenje[i] < minimalnoPesacenje)
				{
					minimalnoPesacenje = destPesacenje + pesacenje[i];
					sourceCvor = stanice[i];
					responseStanice[0] = stanice[i].id;
					responseStanice[1] = destStanica.id;
					responseLinije = new ArrayList<>();
					responseLinije.add(l.id);
				} else if(destPesacenje + pesacenje[i] == minimalnoPesacenje)
				{
					responseLinije.add(l.id);
				}
				
			}
		}
		
		if(responseLinije.size() > 0)
		{
			Integer responseKorekcije[] = new Integer[responseLinije.size()];
			Integer responseLinijeArray[] = (Integer[]) responseLinije.toArray(new Integer[responseLinije.size()]);
			
			for(int i = 0; i < responseKorekcije.length; ++i)
			{
				responseKorekcije[i] = izracunajKorekciju(linije[responseLinijeArray[i]], sourceCvor);
			}
			
			String responseStr = (new Response(req.type, responseStanice, responseLinijeArray, responseKorekcije, null, null, null)).toString();
			
			log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr);
			out.write(responseStr + "\n");
		}
		else
		{
			String responseStr = (new Response(req.type, null, null, null, null, null, null)).toString();
			
			log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr + " (vise se isplati pesacenje!?)");
			out.write(responseStr + "\n");
		}
		
		out.flush();
	}
	
	//ekonomicni (napredni) rezim B (drugi nacin, sa konstantnim vremenom izvrsenja)
	private void handleRequest4b(Request req)
	{
		Linija linije[] = owner.getGradskeLinije().linije;
		owner.getGraf().resetujCvorove();
		Cvor responseStanice[] = new Cvor[2]; //stanice za response prvo source stanica, drugo destination stanica
		ArrayList<Linija> responseLinije = new ArrayList<>();
		ArrayList<Integer> predjeniPutevi = new ArrayList<>();
		double minimalnoPesacenje = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon);
		double gornjaGranica = minimalnoPesacenje;
		
		for(int i = 0; i < linije.length; ++i)
		{
			if(linije[i] != null)
			{
				Cvor stanica = linije[i].pocetnaStanica;
				Cvor start = null, stop = null;
				Veza v = null;
				int predjeniPut = 0, startPredjeniPut = 0;
				double startnaUdaljenost = gornjaGranica;
				double zavrsnaUdaljenost = gornjaGranica;
				double dS, dZ;
				
				while((v = stanica.vratiVezu(linije[i])) != null)
				{
					predjeniPut += v.weight;
					stanica = v.destination;
					
					dS = calcDistance(stanica, req.srcLat, req.srcLon);
					dZ = calcDistance(stanica, req.destLat, req.destLon);
					//moze da se optimizuje tako sto se jednom izracunaju i zapamte u cvorovima njihove udaljenosti do cilja i starta
					
					/*if(i == 47)
					{
						System.out.println(stanica.toString() + " dS=" + dS + " dZ=" + dZ);
					}*/
					
					if(dS<startnaUdaljenost && nijeLazniStart(stanica, linije[i], startnaUdaljenost, zavrsnaUdaljenost, req))
					{
						startnaUdaljenost = dS;
						zavrsnaUdaljenost = gornjaGranica;
						start = stanica;
						stop = null;
						startPredjeniPut = predjeniPut;
					}
					
					if(dZ < zavrsnaUdaljenost)
					{
						zavrsnaUdaljenost = dZ;
						stop = stanica;
					}
					
					if(stanica == linije[i].pocetnaStanica)
						break;
				}
				
				if(startnaUdaljenost + zavrsnaUdaljenost < minimalnoPesacenje)
				{
					minimalnoPesacenje = startnaUdaljenost + zavrsnaUdaljenost;
					responseStanice[0] = start;
					responseStanice[1] = stop;
					responseLinije = new ArrayList<>();
					predjeniPutevi = new ArrayList<>();
					responseLinije.add(linije[i]);
					predjeniPutevi.add(startPredjeniPut);
				} else if(startnaUdaljenost + zavrsnaUdaljenost == minimalnoPesacenje)
				{
					if(responseStanice[0] == start && responseStanice[1] == stop)
					{
						responseLinije.add(linije[i]);
						predjeniPutevi.add(startPredjeniPut);
					}
				}
				
			}
		}
		
		if(responseLinije.size() > 0)
		{
			Integer responseKorekcije[] = new Integer[responseLinije.size()];
			Integer responseLinijeArray[] = new Integer[responseLinije.size()];
			Integer responseStaniceArray[] = new Integer[responseStanice.length];
			
			for(int i = 0; i < responseKorekcije.length; ++i)
			{
				Linija l = responseLinije.get(i);
				responseKorekcije[i] = izracunajKorekciju(l, responseStanice[0], predjeniPutevi.get(i));
				responseLinijeArray[i] = l.id;
			}
			
			responseStaniceArray[0] = responseStanice[0].id;
			responseStaniceArray[1] = responseStanice[1].id;
			
			String responseStr = (new Response(req.type, responseStaniceArray, responseLinijeArray, responseKorekcije, null, null, null)).toString();
			
			log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr);
			out.write(responseStr + "\n");
		}
		else
		{
			String responseStr = (new Response(req.type, null, null, null, null, null, null)).toString();
			
			log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr + " (vise se isplati pesacenje!?)");
			out.write(responseStr + "\n");
		}
		
		out.flush();
		
	}
	
	private boolean nijeLazniStart(Cvor stanica, Linija linija, double startnaUdaljenost, double zavrsnaUdaljenost, Request req)
	{
		boolean p = false;
		double dS, dZ;
		
		dS = calcDistance(stanica, req.srcLat, req.srcLon);
		
		Veza v = null;
		
		while((v = stanica.vratiVezu(linija)) != null)
		{
			stanica = v.destination;
			
			dZ = calcDistance(stanica, req.destLat, req.destLon);
			
			if(dS + dZ < startnaUdaljenost + zavrsnaUdaljenost)
			{
				p = true;
				break;
			}
			
			if(stanica == linija.pocetnaStanica)
				break;
		}
		
		return p;
	}
	
	//optimalni ekonomicni
	private void handleRequest5(Request req)
	{
		
	}
	
	//A*, koristi se za optimalni i za MIN_WALK
	public void handleRequest6(Request req, double brzinaAutobusa, double brzinaPesacenja)
	{
		boolean nadjenPut = false;
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime tempTime = null;
		
		//napravi pseudo Stanice start i end
		Cvor pseudoStart = new Cvor(-1, "pseudoStart", req.srcLat, req.srcLon);
		Cvor pseudoEnd = new Cvor(-2, "pseudoEnd", req.destLat, req.destLon);
		
		pseudoEnd.heuristika = 0.0;
		pseudoEnd.linijom = null;
		pseudoEnd.prethodnaStanica = pseudoStart;
		pseudoEnd.cenaPutanje = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaPesacenja;
		pseudoStart.heuristika = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaAutobusa;
		pseudoStart.linijom = null;
		pseudoStart.prethodnaStanica = null;
		pseudoStart.cenaPutanje = 0.0;
		
		owner.getGraf().resetujCvorove();
		
		//izvuci sve stanice u niz
		Cvor stanice[] = owner.getGraf().getStanice().toArray(new Cvor[owner.getGraf().getStanice().size()]);
		
		PrioritetnaListaCvorova lista = new PrioritetnaListaCvorova();
		
		//izracunaj heuristike
		for(int i = 0; i < stanice.length; ++i)
		{
			if(stanice[i] != null)
			{
				stanice[i].heuristika = calcDistance(stanice[i], req.destLat, req.destLon)/brzinaAutobusa;
				stanice[i].cenaPutanje = calcDistance(stanice[i], req.srcLat, req.srcLon)/brzinaPesacenja;
				
				stanice[i].linijom = null;
				stanice[i].prethodnaStanica = pseudoStart;
				
				stanice[i].status = StruktureConsts.CVOR_SMESTEN;
			}
			
			lista.pushPriority(stanice[i]);
		}
		
		pseudoStart.status = StruktureConsts.CVOR_OBRADJEN;
		pseudoEnd.status = StruktureConsts.CVOR_SMESTEN;
		lista.pushPriority(pseudoEnd); //dodaj i pseudoEnd u priority list
		
		Cvor radniCvor = null;
		Cvor tempCvor = null;
		ArrayList<Veza> potomciVeze = null;
		Veza v = null;
		Linija l = null;
		long kasnjenje = 0;
		
		while(!lista.isEmpty())
		{
			radniCvor = lista.remove(0);
			
			radniCvor.status = StruktureConsts.CVOR_OBRADJEN;
			
			if(radniCvor == pseudoEnd)
			{
				nadjenPut = true;
				break;
			}
			
			//pokupi decu cvorove i update statistike
			potomciVeze = radniCvor.veze;
			for(int i = 0; i < potomciVeze.size(); ++i)
			{
				v = potomciVeze.get(i);
				tempCvor = v.destination;
				
				if(tempCvor.status == StruktureConsts.CVOR_OBRADJEN)
					continue;
				
				if(v.linija == radniCvor.linijom)
				{
					if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa)
					{
						lista.remove(tempCvor);
						tempCvor.linijom = v.linija;
						tempCvor.prethodnaStanica = radniCvor;
						tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa;
						tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = null;
						lista.pushPriority(tempCvor);
					}
				} else
				{
					if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa + (kasnjenje = izracunajKasnjenjeLinije2(v.linija, radniCvor, brzinaAutobusa)))
					{
						lista.remove(tempCvor);
						tempCvor.linijom = v.linija;
						tempCvor.prethodnaStanica = radniCvor;
						tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa + kasnjenje;
						
						tempTime = currentTime.plusDays(((long)radniCvor.cenaPutanje + kasnjenje)/86400);
						tempTime = tempTime.plusHours((((long)radniCvor.cenaPutanje + kasnjenje)/3600)%24);
						tempTime = tempTime.plusMinutes((((long)radniCvor.cenaPutanje + kasnjenje)/60)%60);
						tempTime = tempTime.plusSeconds(((long)radniCvor.cenaPutanje + kasnjenje)%60);
						DatumVremeStanica vremeDolaska = new DatumVremeStanica(radniCvor.id, tempTime.getSecond(), tempTime.getMinute(), tempTime.getHour(), tempTime.getDayOfWeek().getValue(), tempTime.getMonthValue(), tempTime.getYear());
						tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = vremeDolaska;
						
						lista.pushPriority(tempCvor);
					}
				}
			}
			
			//obradi pesacenje do svih stanica
			for(int j = 0; j < stanice.length; ++j)
			{
				if(stanice[j] != radniCvor && stanice[j].status != StruktureConsts.CVOR_OBRADJEN)
				{
					double udaljenost = matricaUdaljenosti[radniCvor.id][stanice[j].id];
					if(stanice[j].cenaPutanje > radniCvor.cenaPutanje + udaljenost/brzinaPesacenja)
					{
						lista.remove(stanice[j]);
						stanice[j].linijom = null;
						stanice[j].prethodnaStanica = radniCvor;
						stanice[j].cenaPutanje = radniCvor.cenaPutanje + udaljenost/brzinaPesacenja;
						stanice[j].vremeDolaskaAutobusaNaPrethodnuStanicu = null;
						lista.pushPriority(stanice[j]);
					}
				}
			}
			//pesacenje do cilja jer on nije u nizu stanice[]
			double udaljenost = calcDistance(radniCvor, pseudoEnd.lat, pseudoEnd.lon);
			if(pseudoEnd.cenaPutanje > radniCvor.cenaPutanje + udaljenost/brzinaPesacenja)
			{
				lista.remove(pseudoEnd);
				pseudoEnd.linijom = null;
				pseudoEnd.prethodnaStanica = radniCvor;
				pseudoEnd.cenaPutanje = radniCvor.cenaPutanje + udaljenost/brzinaPesacenja;
				lista.pushPriority(pseudoEnd);
			}
		}
		
		if(nadjenPut)
		{
			Cvor c = pseudoEnd;
			
			Response response = new Response();
			response.type = req.type;
			response.size = (int) pseudoEnd.cenaPutanje;	//procenjena cena putovanja
			ArrayList<Integer> responseStanice = new ArrayList<>();
			ArrayList<Integer> responseLinije = new ArrayList<>();
			ArrayList<DatumVremeStanica> responseVremenaDolaska = new ArrayList<>();
			
			while(c != null)
			{
				responseStanice.add(c.id);
				
				if(c.linijom != null)
					responseLinije.add(c.linijom.id);
				else
					responseLinije.add(null);
				
				if(c.vremeDolaskaAutobusaNaPrethodnuStanicu != null)
					responseVremenaDolaska.add(c.vremeDolaskaAutobusaNaPrethodnuStanicu);
				
				//moze da se doda u response i Estimated Time of Arrival (dodao sam ga u Response.size)
				c = c.prethodnaStanica;
			}
			
			response.stanice = new Integer[responseStanice.size()];
			response.linije = new Integer[responseLinije.size()];
			response.vremenaDolaska = new ArrayList<>();
			
			int arraySize = responseStanice.size();
			for(int i = arraySize-1; i >= 0; --i)
			{
				response.stanice[arraySize-1-i] = responseStanice.get(i);
				response.linije[arraySize-1-i] = responseLinije.get(i);
			}
			
			arraySize = responseVremenaDolaska.size();
			if(arraySize > 0)
			{
				for(int i = arraySize-1; i >= 0; --i)
					response.vremenaDolaska.add(responseVremenaDolaska.get(i));
			}
			else
				response.vremenaDolaska = null;
			
			String responseStr = response.toString();
			
			log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr);
			out.write(responseStr + "\n");
		}
		else
		{
			String responseStr = (new Response(req.type, null, null, null, null, null, null)).toString();
			
			log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr + " (A* nije naso resenje!?)");
			out.write(responseStr + "\n");
		}
		
		out.flush();
	}
	
	public void handleRequest6Test(Request req, double brzinaAutobusa, double brzinaPesacenja)
	{
		log.write("A* START");
		boolean nadjenPut = false;
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime tempTime = null;
		//napravi pseudo Stanice start i end
		//int id = matricaUdaljenosti[0].length - 1;
		Cvor pseudoStart = new Cvor(-1, "pseudoStart", req.srcLat, req.srcLon);
		//--id;
		Cvor pseudoEnd = new Cvor(-2, "pseudoEnd", req.destLat, req.destLon);
		
		pseudoEnd.heuristika = 0.0;
		pseudoEnd.linijom = null;
		pseudoEnd.prethodnaStanica = pseudoStart;
		pseudoEnd.cenaPutanje = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaPesacenja;
		pseudoStart.heuristika = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaAutobusa;
		pseudoStart.linijom = null;
		pseudoStart.prethodnaStanica = null;
		pseudoStart.cenaPutanje = 0.0;
		
		owner.getGraf().resetujCvorove();
		
		//izvuci sve stanice u niz
		Cvor stanice[] = owner.getGraf().getStanice().toArray(new Cvor[owner.getGraf().getStanice().size()]);
		
		PrioritetnaListaCvorova lista = new PrioritetnaListaCvorova();
		
		//izracunaj heuristike
		for(int i = 0; i < stanice.length; ++i)
		{
			if(stanice[i] != null)
			{
				stanice[i].heuristika = calcDistance(stanice[i], req.destLat, req.destLon)/brzinaAutobusa;
				stanice[i].cenaPutanje = calcDistance(stanice[i], req.srcLat, req.srcLon)/brzinaPesacenja;
				
				stanice[i].linijom = null;
				stanice[i].prethodnaStanica = pseudoStart;
				
				stanice[i].status = StruktureConsts.CVOR_SMESTEN;
			}
			
			lista.pushPriority(stanice[i]);
		}
		
		pseudoStart.status = StruktureConsts.CVOR_OBRADJEN;
		pseudoEnd.status = StruktureConsts.CVOR_SMESTEN;
		lista.pushPriority(pseudoEnd); //dodaj i pseudoEnd u priority list
		
		Cvor radniCvor = null;
		Cvor tempCvor = null;
		ArrayList<Veza> potomciVeze = null;
		Veza v = null;
		Linija l = null;
		long kasnjenje = 0;
		
		while(!lista.isEmpty())
		{
			radniCvor = lista.remove(0);
			
			radniCvor.status = StruktureConsts.CVOR_OBRADJEN;
			
			if(radniCvor == pseudoEnd)
			{
				nadjenPut = true;
				break;
			}
			
			//pokupi decu cvorove i update statistike
			potomciVeze = radniCvor.veze;
			for(int i = 0; i < potomciVeze.size(); ++i)
			{
				v = potomciVeze.get(i);
				tempCvor = v.destination;
				
				if(tempCvor.status == StruktureConsts.CVOR_OBRADJEN)
					continue;
				
				if(v.linija == radniCvor.linijom)
				{
					if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa)
					{
						lista.remove(tempCvor);
						tempCvor.linijom = v.linija;
						tempCvor.prethodnaStanica = radniCvor;
						tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa;
						tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = null;
						lista.pushPriority(tempCvor);
					}
				} else
				{
					if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa + (kasnjenje = izracunajKasnjenjeLinije2(v.linija, radniCvor, brzinaAutobusa)))
					{
						lista.remove(tempCvor);
						tempCvor.linijom = v.linija;
						tempCvor.prethodnaStanica = radniCvor;
						tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa + kasnjenje;
						
						tempTime = currentTime.plusDays(((long)radniCvor.cenaPutanje + kasnjenje)/86400);
						tempTime = tempTime.plusHours((((long)radniCvor.cenaPutanje + kasnjenje)/3600)%24);
						tempTime = tempTime.plusMinutes((((long)radniCvor.cenaPutanje + kasnjenje)/60)%60);
						tempTime = tempTime.plusSeconds(((long)radniCvor.cenaPutanje + kasnjenje)%60);
						DatumVremeStanica vremeDolaska = new DatumVremeStanica(radniCvor.id, tempTime.getSecond(), tempTime.getMinute(), tempTime.getHour(), tempTime.getDayOfWeek().getValue(), tempTime.getMonthValue(), tempTime.getYear());
						tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = vremeDolaska;
						
						lista.pushPriority(tempCvor);
					}
				}
			}
			
			//obradi pesacenje do svih stanica
			for(int j = 0; j < stanice.length; ++j)
			{
				if(stanice[j] != radniCvor && stanice[j].status != StruktureConsts.CVOR_OBRADJEN)
				{
					double udaljenost = matricaUdaljenosti[radniCvor.id][stanice[j].id];
					if(stanice[j].cenaPutanje > radniCvor.cenaPutanje + udaljenost/brzinaPesacenja)
					{
						lista.remove(stanice[j]);
						stanice[j].linijom = null;
						stanice[j].prethodnaStanica = radniCvor;
						stanice[j].cenaPutanje = radniCvor.cenaPutanje + udaljenost/brzinaPesacenja;
						stanice[j].vremeDolaskaAutobusaNaPrethodnuStanicu = null;
						lista.pushPriority(stanice[j]);
					}
				}
			}
			//pesacenje do cilja jer on nije u nizu stanice[]
			double udaljenost = calcDistance(radniCvor, pseudoEnd.lat, pseudoEnd.lon);
			if(pseudoEnd.cenaPutanje > radniCvor.cenaPutanje + udaljenost/brzinaPesacenja)
			{
				lista.remove(pseudoEnd);
				pseudoEnd.linijom = null;
				pseudoEnd.prethodnaStanica = radniCvor;
				pseudoEnd.cenaPutanje = radniCvor.cenaPutanje + udaljenost/brzinaPesacenja;
				lista.pushPriority(pseudoEnd);
			}
		}
		
		if(nadjenPut)
		{
			Cvor c = pseudoEnd;
			System.out.println("");
			System.out.println("RESENJE:");
			while(c != null)
			{
				System.out.println(c + " --Linijom-- " + c.linijom + " --Cena-- " + c.cenaPutanje);
				if(c.vremeDolaskaAutobusaNaPrethodnuStanicu != null)
				{
					System.out.println(c.vremeDolaskaAutobusaNaPrethodnuStanicu.sat + ":" + c.vremeDolaskaAutobusaNaPrethodnuStanicu.minut + ":" + c.vremeDolaskaAutobusaNaPrethodnuStanicu.sekund);
				}
				c = c.prethodnaStanica;
			}
		} else
		{
			System.out.println("Nisam uspeo da nadjem put :(");
		}
		log.write("A* END");
	}
	
	//rezim minimalnog pesacenja (napredni) (nedovrsen, stari pristup)
	private void handleRequest8(Request req)
	{
		Graf g = owner.getGraf();
		g.resetujCvorove();
		Cvor stanice[] = g.getStanice().toArray(new Cvor[g.getStanice().size()]);
		Cvor sourceCvor = null;
		Linija linije[] = g.getGradskeLinije().linije;
		double startPesacenje[] = new double[stanice.length];
		double endPesacenje[] = new double[stanice.length];

		ArrayList<Cvor> startStanice = new ArrayList<>();
		ArrayList<Cvor> endStanice = new ArrayList<>();

		for(int i = 0; i < stanice.length; ++i)
		{
			startPesacenje[i] = calcDistance(stanice[i], req.srcLat, req.srcLon);
			endPesacenje[i] = calcDistance(stanice[i], req.destLat, req.destLon);
		}

		//sortiraj stanice po blizini startne pozicije
		for(int i = 1; i < stanice.length; ++i)
		{
			double key = startPesacenje[i];
			Cvor pomC = stanice[i];
			int j = i-1;
			while((j >= 0) && (startPesacenje[j] > key))
			{
				startPesacenje[j+1] = startPesacenje[j];
				stanice[j+1] = stanice[j];
				--j;
			}
			startPesacenje[j+1] = key;
			stanice[j+1] = pomC;
		}


		//izaberi top 5 startnih tanica
		int k = 0;
		ArrayList<Linija> skupljeneLinije = new ArrayList<>();
		while(startStanice.size() < StruktureConsts.MIN_PESACENJE_START_NUM)
		{
			boolean p = false;
			Linija l = null;
			for(int j = 0; j < stanice[k].veze.size(); ++j)
			{
				if(!skupljeneLinije.contains((l = stanice[k].veze.get(j).linija)))
				{
					p = true;
					skupljeneLinije.add(l); 
				}
			}

			if(p)
				startStanice.add(stanice[k]);

			++k;
		}

		//////////////////////////////////////////////////////////////////////////////////

		//soritraj stanice po udaljenosti od cilja, neopadajuci
		for(int i = 1; i < stanice.length; ++i)
		{
			double key = endPesacenje[i];
			Cvor pomC = stanice[i];
			int j = i-1;
			while((j >= 0) && (endPesacenje[j] > key))
			{
				endPesacenje[j+1] = endPesacenje[j];
				stanice[j+1] = stanice[j];
				--j;
			}
			endPesacenje[j+1] = key;
			stanice[j+1] = pomC;
		}

		//izaberi top 5 end stanica
		k = 0;
		skupljeneLinije = new ArrayList<>();
		while(endStanice.size() < StruktureConsts.MIN_PESACENJE_END_NUM)
		{
			boolean p = false;
			Linija l = null;
			for(int j = 0; j < stanice[k].veze.size(); ++j)
			{
				if(!skupljeneLinije.contains((l = stanice[k].veze.get(j).linija)))
				{
					p = true;
					skupljeneLinije.add(l); 
				}
			}

			if(p)
				endStanice.add(stanice[k]);

			++k;
		}

		/////////////////////////////////////////////////////////////////////////////////////

		boolean nasoPut = false;

		for(int s = 0; s<StruktureConsts.MIN_PESACENJE_START_NUM && !nasoPut; ++s)
			for(int e = 0; e<StruktureConsts.MIN_PESACENJE_END_NUM && !nasoPut; ++s)
				nasoPut = g.BFS(startStanice.get(s), endStanice.get(e));

	}
	
	//ovo je depricated :D (zaglupi A* zbog zaokruzivanja prilikom prevodjenja iz sec u min i hour)
	//kasnjenje u sekundama
	private double izracunajKasnjenjeLinije(Linija l, Cvor c, double brzinaAutobusa)
	{
		LocalDateTime realLifeTime = LocalDateTime.now();
		int realLifeHour = realLifeTime.getHour();
		int realLifeMinute = realLifeTime.getMinute();
		int realLifeSecond = realLifeTime.getSecond();
		DayOfWeek realLifeDay = realLifeTime.getDayOfWeek();
		
		double secondsToWaitForBus = -1.0;
		
		int busTravelSeconds = izracunajKorekciju(l, c);
		int busTravelMinutes = busTravelSeconds / 60;
		int busTravelHours = busTravelSeconds / 3600;
		
		//System.out.println("Bus travel seconds = " + busTravelSeconds);
		
		int futureShiftSeconds = (int) c.cenaPutanje;
		
		int cvorHours = realLifeHour;
		int cvorMinutes = realLifeMinute;
		int cvorSeconds = realLifeSecond;
		DayOfWeek cvorDay = realLifeDay;
		
		DayOfWeek targetDay = cvorDay;
		int targetHour = cvorHours, targetMinute = cvorMinutes;
		
		int sourceCvorHours, sourceCvorMinutes;
		DayOfWeek sourceCvorDay;
		
		cvorSeconds += futureShiftSeconds;
		
		cvorMinutes += cvorSeconds / 60;
		
		///////////proveri da ne pravi problem ovo?!?!
		/*if(cvorSeconds % 60 > 30)
			++cvorMinutes;*/
		
		cvorHours += cvorMinutes / 60;
		
		while(cvorHours > 24)
		{
			cvorDay = cvorDay.plus(1);
			cvorHours -= 24;
		}
		
		sourceCvorDay = cvorDay;
		sourceCvorHours = cvorHours;
		sourceCvorMinutes = cvorMinutes - busTravelMinutes;
		
		while(sourceCvorMinutes < 0)
		{
			sourceCvorMinutes += 60;
			sourceCvorHours -= 1;
		}
		
		sourceCvorHours -= busTravelHours;
		while(sourceCvorHours < 0)
		{
			sourceCvorHours += 24;
			sourceCvorDay = sourceCvorDay.minus(1);
		}
		
		int mat[][] = null;
		if(sourceCvorDay == DayOfWeek.SUNDAY)
		{
			int i = 0, h = sourceCvorHours;
			DayOfWeek d = sourceCvorDay;
			mat = l.matNedelja;
			while(secondsToWaitForBus == -1)
			{
				if(mat[h][i] != -1)
				{
					if(sourceCvorDay.getValue() != d.getValue() || h > sourceCvorHours || mat[h][i] >= sourceCvorMinutes)
					{
						targetDay = d;
						targetHour = h;
						targetMinute = mat[h][i];
						
						secondsToWaitForBus = 0;
					}
				} else
				{
					++h;
					if(h > 24)
					{
						d = d.plus(1);
						h -= 24;
						if(d == DayOfWeek.SATURDAY)
							mat = l.matSubota;
						else if(d == DayOfWeek.SUNDAY)
							mat = l.matNedelja;
						else
							mat = l.matRadni;
					}
					i = -1;
				}
				
				++i;
			}
		} else if(sourceCvorDay == DayOfWeek.SATURDAY)
		{
			int i = 0, h = sourceCvorHours;
			DayOfWeek d = sourceCvorDay;
			mat = l.matSubota;
			while(secondsToWaitForBus == -1)
			{
				if(mat[h][i] != -1)
				{
					if(sourceCvorDay.getValue() != d.getValue() || h > sourceCvorHours || mat[h][i] >= sourceCvorMinutes)
					{
						targetDay = d;
						targetHour = h;
						targetMinute = mat[h][i];
						
						secondsToWaitForBus = 0;
					}
				} else
				{
					++h;
					if(h > 24)
					{
						d = d.plus(1);
						h -= 24;
						if(d == DayOfWeek.SATURDAY)
							mat = l.matSubota;
						else if(d == DayOfWeek.SUNDAY)
							mat = l.matNedelja;
						else
							mat = l.matRadni;
					}
					i = -1;
				}
				
				++i;
			}
		} else
		{
			int i = 0, h = sourceCvorHours;
			DayOfWeek d = sourceCvorDay;
			mat = l.matRadni;
			while(secondsToWaitForBus == -1)
			{
				if(mat[h][i] != -1)
				{
					if(sourceCvorDay.getValue() != d.getValue() || h > sourceCvorHours || mat[h][i] >= sourceCvorMinutes)
					{
						targetDay = d;
						targetHour = h;
						targetMinute = mat[h][i];
						
						secondsToWaitForBus = 0;
					}
				} else
				{
					++h;
					if(h > 24)
					{
						d = d.plus(1);
						h -= 24;
						if(d == DayOfWeek.SATURDAY)
							mat = l.matSubota;
						else if(d == DayOfWeek.SUNDAY)
							mat = l.matNedelja;
						else
							mat = l.matRadni;
					}
					i = -1;
				}
				
				++i;
			}
		}
		
		if(targetDay.getValue() != cvorDay.getValue())
		{
			int minuti = 60 - cvorMinutes;
			if(minuti > 0)
				cvorHours++;
			int sati = 24 - cvorHours;
			sati += targetHour;
			minuti += targetMinute;
			
			sati += minuti / 60;
			minuti %= 60;
			secondsToWaitForBus = sati*3600 + minuti*60 + busTravelMinutes*60;
			
			DayOfWeek tempDay = cvorDay;
			if(tempDay.plus(1) != targetDay)
			{
				tempDay = tempDay.plus(1);
				while(tempDay != targetDay)
				{
					tempDay = tempDay.plus(1);
					secondsToWaitForBus += 24*3600;
				}
			}
			
		} else
		{
			int minuti = targetMinute + targetHour*60 - cvorMinutes - cvorHours*60;
			
			secondsToWaitForBus = minuti*60 + busTravelMinutes*60;
		}
		
		return secondsToWaitForBus; //vracamo rezultat u sekundama
	}
	
	//kasnjenje linije u sekundama, za cvor c, u odredjeno vreme, za liniju l
	@SuppressWarnings("deprecation")
	private long izracunajKasnjenjeLinije2(Linija l, Cvor c, double brzinaAutobusa)
	{
		//double secondsToWaitForBus = 0.0;
		
		//LocalDateTime realLifeDateTime = LocalDateTime.now();
		LocalDateTime targetDateTime = LocalDateTime.now();
		Date realLifeDate = new Date();
		//Date targetDate = null;
		//Date sourceDate = null;
		
		long realLifeSeconds = realLifeDate.getTime() / 1000;
		
		long busTravelSeconds = izracunajKorekciju(l, c);
		
		long futureShiftSeconds = (long) c.cenaPutanje;
		
		long cvorSeconds = realLifeSeconds + futureShiftSeconds;
		
		long sourceSeconds = cvorSeconds - busTravelSeconds;
		
		//targetDateTime = targetDateTime.plusSeconds(futureShiftSeconds);
		//targetDateTime = targetDateTime.minusSeconds(busTravelSeconds);
		targetDateTime = targetDateTime.plusDays((futureShiftSeconds-busTravelSeconds)/86400);
		targetDateTime = targetDateTime.plusHours(((futureShiftSeconds-busTravelSeconds)/3600)%24);
		targetDateTime = targetDateTime.plusMinutes(((futureShiftSeconds-busTravelSeconds)/60)%60);
		targetDateTime = targetDateTime.plusSeconds((futureShiftSeconds-busTravelSeconds)%60);
		//sourceDate = new Date(sourceSeconds*1000);
		//targetDate = new Date(sourceSeconds*1000);
		
		//long bla = 0;
		long targetSeconds = sourceSeconds;
		//targetSeconds -= (targetDate.getMinutes()*60 + targetDate.getSeconds());
		targetSeconds -= (targetDateTime.getMinute()*60 + targetDateTime.getSecond());
		
		int mat[][] = null;
		boolean found = false;
		//provera za nedelju
		
		/*if(targetDate.getDay() == 0)
			mat = l.matNedelja;
		else if(targetDate.getDay() == 6)
			mat = l.matSubota;
		else
			mat = l.matRadni;*/
		
		if(targetDateTime.getDayOfWeek() == DayOfWeek.SATURDAY)
			mat = l.matSubota;
		else if(targetDateTime.getDayOfWeek() == DayOfWeek.SUNDAY)
			mat = l.matNedelja;
		else
			mat = l.matRadni;
		targetDateTime = targetDateTime.minusMinutes(targetDateTime.getMinute());
		targetDateTime = targetDateTime.minusSeconds(targetDateTime.getSecond());
		int i = 0, h = targetDateTime.getHour();
		
		while(!found)
		{
			if(mat[h][i] != -1)
			{
				if(targetSeconds + mat[h][i]*60 > sourceSeconds)
				{
					found = true;
					targetSeconds += mat[h][i]*60;
					targetDateTime = targetDateTime.plusMinutes(mat[h][i]);
					/*System.out.println();
					System.out.println("Bus travel seconds = " + busTravelSeconds);
					System.out.println("Hvatam bus u " + targetDateTime.getDayOfWeek() + " koji krece u " + h + ":" + mat[h][i]);
					System.out.println();*/
				}
			}
			else
			{
				targetSeconds += 3600;
				targetDateTime = targetDateTime.plusHours(1);
				//bla += 3600;
				++h;
				if(h == 25)
				{
					h = 0;
					targetSeconds -= 3600;
					//targetDateTime = targetDateTime.plusDays(1);
					targetDateTime = targetDateTime.minusHours(targetDateTime.getHour());
					if(targetDateTime.getDayOfWeek() == DayOfWeek.SATURDAY)
						mat = l.matSubota;
					else if(targetDateTime.getDayOfWeek() == DayOfWeek.SUNDAY)
						mat = l.matNedelja;
					else
						mat = l.matRadni;
				}
				i = -1;
			}
			++i;
		}
		
		//targetDateTime = targetDateTime.plusDays(busTravelSeconds/86400);
		//targetDateTime = targetDateTime.plusHours((busTravelSeconds/3600)%24);
		//targetDateTime = targetDateTime.plusMinutes((busTravelSeconds/60)%60);
		//targetDateTime = targetDateTime.plusSeconds(busTravelSeconds%60);
		
		//DatumVremeStanica vremeDolaska = new DatumVremeStanica(c.id, targetDateTime.getSecond(), targetDateTime.getMinute(), targetDateTime.getHour(), targetDateTime.getDayOfWeek().getValue(), targetDateTime.getMonthValue(), targetDateTime.getYear());
		//c.vremeDolaskaAutobusa = vremeDolaska;
		
		//System.out.println("bla=" + bla);
		return targetSeconds - sourceSeconds;
	}
	
	//predjeni put do stanice stanica, linijom linija [vreme!?!?]
	private int izracunajKorekciju(Linija linija, Cvor stanica, int predjeniPut)
	{
		return (int) (predjeniPut/ServerConsts.brzinaAutobusa);
	}

	private int izracunajKorekciju(Linija linija, Cvor targetStanica)
	{
		int predjeniPut = 0;
		
		if(linija == null || targetStanica==null)
			return Integer.MAX_VALUE;
		
		if(linija.pocetnaStanica == targetStanica)
			return izracunajKorekciju(linija, targetStanica, 0);
		
		Cvor pocetnaStanica = linija.pocetnaStanica;
		Cvor tempStanica = pocetnaStanica;
		Veza v = null;
		
		while((v = tempStanica.vratiVezu(linija)) != null)
		{
			predjeniPut += v.weight;
			
			if((v.destination == pocetnaStanica) || (v.destination == targetStanica))
				break;
			
			tempStanica = v.destination;
		}
		
		return izracunajKorekciju(linija, targetStanica, predjeniPut);
	}
	
	public static double calcDistance(Cvor stanica, double lat2, double long2)
	{
	    double a, c;

	    a = Math.sin((lat2 - stanica.lat)*Math.PI/360) * Math.sin((lat2 - stanica.lat)*Math.PI/360) +
	    	Math.sin((long2 - stanica.lon)*Math.PI/360) * Math.sin((long2 - stanica.lon)*Math.PI/360) * Math.cos(lat2 * Math.PI/180) * Math.cos(stanica.lat * Math.PI/180);

	    c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	    return 6371000 * c;
	}
	
	public double calcDistance(Cvor cvor1, Cvor cvor2)
	{
	    return matricaUdaljenosti[cvor1.id][cvor2.id];
	}
	
	public static double calcDistance(double lat1, double long1, double lat2, double long2)
	{
	    double a, c;

	    a = Math.sin((lat2 - lat1)*Math.PI/360) * Math.sin((lat2 - lat1)*Math.PI/360) +
	    	Math.sin((long2 - long1)*Math.PI/360) * Math.sin((long2 - long1)*Math.PI/360) * Math.cos(lat2 * Math.PI/180) * Math.cos(lat1 * Math.PI/180);

	    c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	    return 6371000 * c;
	}
}

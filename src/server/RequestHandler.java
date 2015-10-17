package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

import javax.sound.sampled.TargetDataLine;

import com.google.gson.*;

import datalayer.CSInfo;
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
	
	private CSInfo tempInfo = null;
	
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
			
			switch(req.type)
			{
			case 0:
				handleRequest0(req);		//bpp.db
				break;
			case 1:
				handleRequest1(req);		//red_voznje.db
				break;	
			case 2:
				handleRequest2(req);		//putanje_buseva.db
				break;	
			case 3:
				handleRequest3(req);		//klasicni red voznje
				break;
			case 4:
				handleRequest4(req);		//ekonomicni
				break;
			case 5:
				handleRequest5(req, ServerConsts.brzinaPesaka);			//optimalni ekonomicni																	//optimalni ekonomicni
				break;
			case 6:
				handleRequest6(req, ServerConsts.brzinaPesaka);			//rezim vremenske optimalnosti
				break;
			case 7:
				handleRequest6(req, ServerConsts.brzinaPesakaZaMinWalk);	//MIN WALK - rezim minimalnog pesacenja
				break;
			case 10:
				handleRequest10(req);										//crowd sensing
				break;
			default:
				log.write("Thread["+ owner.getId() + "] " +"Nepoznat request type = " + req.type);
				break;
			}

			
		} catch (IOException e)
		{
			log.write("Thread["+ owner.getId() + "] " + "failed to please");
	    	log.write(e.getMessage()); e.printStackTrace();
		} catch (Exception e)
		{
			log.write("Thread["+ owner.getId() + "] " + "Nepoznat request format = " + line);
			log.write("Thread["+ owner.getId() + "] " + "failed to please"); e.printStackTrace();
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
	
	//klijent proverava da li ima najnoviju verziju baze putanje_buseva.db
	private void handleRequest2(Request req)
	{

		if(req.dbVer < ServerConsts.putanjeDBVer)
		{
			File file = new File(ServerConsts.SQLITE_PUTANJE_BUSEVA_DB_NAME);

			out.write((new Response(req.type, null, null, null, null, (int) file.length(), ServerConsts.putanjeDBVer)).toString() + "\n");
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
				log.write("Thread [" + owner.getId() + "] Exception caught when trying to open file " + ServerConsts.SQLITE_PUTANJE_BUSEVA_DB_NAME);
				log.write(e.getMessage());
			} catch (IOException e)
			{
				log.write("Thread [" + owner.getId() + "] Exception caught when trying to read file " + ServerConsts.SQLITE_PUTANJE_BUSEVA_DB_NAME);
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
		ArrayList<CSInfo> targetCrowdInfo = new ArrayList<>();
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
			
			//ubaci i crowd sensing
			targetCrowdInfo.addAll(this.getCrowdInfo(l, korekcije[i]));
		}
		
		String responseStr = (new Response(req.type, staniceId, linijeId, korekcije, null, null, null, targetCrowdInfo)).toString();
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
	private void handleRequest5(Request req, double brzinaPesacenja)
	{
		Linija linije[] = owner.getGradskeLinije().linije;
		owner.getGraf().resetujCvorove();
		
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime tempTime = null;
		
		int linijeResenja[][] = new int[linije.length][5]; //start_stanica.id, end_stanica.id, cena_puta, vreme pesacenja do starta, kasnjenje linije na tu stanicu
		CSInfo crowdInformations[] = new CSInfo[linije.length];
		
		for(int i = 0; i < linije.length; ++i)
		{
			if(linije[i] != null)
			{
				Cvor stanica = linije[i].pocetnaStanica;
				Cvor start = null, stop = null;
				Veza v = null;
				int predjeniPut = 0, startPredjeniPut = 0, endPredjeniPut = 0;
				double startnaUdaljenost = Double.MAX_VALUE;//gornjaGranica;
				double zavrsnaUdaljenost = Double.MAX_VALUE;//gornjaGranica;
				double dS, dZ;
				
				while((v = stanica.vratiVezu(linije[i])) != null)
				{
					predjeniPut += v.weight;
					stanica = v.destination;
					
					dS = calcDistance(stanica, req.srcLat, req.srcLon);
					dZ = calcDistance(stanica, req.destLat, req.destLon);
					//moze da se optimizuje tako sto se jednom izracunaju i zapamte u cvorovima njihove udaljenosti do cilja i starta
					
					if(dS<startnaUdaljenost && nijeLazniStart(stanica, linije[i], startnaUdaljenost, zavrsnaUdaljenost, req))
					{
						startnaUdaljenost = dS;
						zavrsnaUdaljenost = Double.MAX_VALUE;;
						start = stanica;
						stop = null;
						startPredjeniPut = predjeniPut;
					}
					
					if(dZ < zavrsnaUdaljenost)
					{
						zavrsnaUdaljenost = dZ;
						stop = stanica;
						endPredjeniPut = predjeniPut;
					}
					
					if(stanica == linije[i].pocetnaStanica)
						break;
				}
				
				linijeResenja[i][0] = start.id;
				linijeResenja[i][1] = stop.id;
				if(start != stop)
				{
					linijeResenja[i][2] = (int) ((startnaUdaljenost + zavrsnaUdaljenost)/brzinaPesacenja + (endPredjeniPut - startPredjeniPut)/this.brzinaAutobusa(linije[i])); //na ovo treba da se doda jos i vreme cekanja busa na stanici
					linijeResenja[i][3] = (int) (startnaUdaljenost/brzinaPesacenja);
	
					start.cenaPutanje = startnaUdaljenost/brzinaPesacenja;
					
					linijeResenja[i][4] = (int) izracunajKasnjenjeLinije2(linije[i], start/*, this.brzinaAutobusa(linije[i])*/);
					crowdInformations[i] = tempInfo;	
					linijeResenja[i][2] += linijeResenja[i][4];
				} else
					linijeResenja[i][2] = Integer.MAX_VALUE;
			}
		}
		
		int minCena = Integer.MAX_VALUE;
		ArrayList<Integer> responseLinije = new ArrayList<>();
		//responseLinije[i] odgovara kao start stanica responseStanice[2*i] i kao end stanica responseStanice[2*i+1]
		ArrayList<Integer> responseStanice = new ArrayList<>();
		ArrayList<DatumVremeStanica> vremenaDolaska = new ArrayList<>();
		ArrayList<Integer> responseKorekcije = new ArrayList<>();
		
		int cenaPesacenja;
		
		for(int i = 0; i < linije.length; ++i)
			if(linije[i] != null && linijeResenja[i][2] < minCena)
				minCena = linijeResenja[i][2];
		
		if((cenaPesacenja = (int) (calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaPesacenja)) < minCena)
			minCena = cenaPesacenja;
		
		for(int i = 0; i < linije.length; ++i)
			if(linije[i] != null && linijeResenja[i][2] <= 1.5 * minCena)
			{
				responseLinije.add(linije[i].id);
				responseKorekcije.add(linijeResenja[i][2]);
				responseStanice.add(linijeResenja[i][0]);
				responseStanice.add(linijeResenja[i][1]);
				
				tempTime = currentTime.plusDays((linijeResenja[i][3] + linijeResenja[i][4])/86400);
				tempTime = tempTime.plusHours(((linijeResenja[i][3] + linijeResenja[i][4])/3600)%24);
				tempTime = tempTime.plusMinutes(((linijeResenja[i][3] + linijeResenja[i][4])/60)%60);
				tempTime = tempTime.plusSeconds((linijeResenja[i][3] + linijeResenja[i][4])%60);
				DatumVremeStanica vremeDolaska = new DatumVremeStanica(linijeResenja[i][0], linije[i].id, tempTime.getSecond(), tempTime.getMinute(), tempTime.getHour(), tempTime.getDayOfWeek().getValue(), tempTime.getMonthValue(), tempTime.getYear(), crowdInformations[i]);
				vremenaDolaska.add(vremeDolaska);
			}
		
		if(cenaPesacenja <= 1.5 * minCena)
		{
			responseLinije.add(null);
			responseKorekcije.add(cenaPesacenja);
		}
		
		String responseStr = (new Response(req.type, responseStanice.toArray(new Integer[responseStanice.size()]), responseLinije.toArray(new Integer[responseLinije.size()]), responseKorekcije.toArray(new Integer[responseKorekcije.size()]), vremenaDolaska, null, null)).toString();
		
		log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr);
		out.write(responseStr + "\n");

		out.flush();
	}
	
	//A*, koristi se za optimalni i za MIN_WALK
	public void handleRequest6(Request req, double brzinaPesacenja)
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
		pseudoStart.heuristika = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/ServerConsts.brzinaAutobusa;
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
				stanice[i].heuristika = calcDistance(stanice[i], req.destLat, req.destLon)/ServerConsts.brzinaAutobusa;
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
		
		//izracunaj prioritete linija (koliko linija vodi blizu cilju) ako je zahtevan min walk
		//ovaj korak mora posle racunanja heuristike, jer pretpostavlja da je heuristika izracunata za svaki cvor
		if(req.type == 7)
			izracunajPrioriteteLinija(owner.getGradskeLinije());
		
		
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
				
				double brzinaAutobusa = this.brzinaAutobusa(v.linija);
				
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
					/*if(req.type == 7)
						kasnjenje = 10;		//za min_walk je kasnjenje konstanta
					else
						kasnjenje = izracunajKasnjenjeLinije2(v.linija, radniCvor, brzinaAutobusa);*/
					
					if(req.type == 7)
						kasnjenje = (long) v.linija.prioritet;		//za min_walk se koristi prioritet linije
					else
						kasnjenje = izracunajKasnjenjeLinije2(v.linija, radniCvor/*, brzinaAutobusa*/);
					
					if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa + kasnjenje)
					{
						lista.remove(tempCvor);
						tempCvor.linijom = v.linija;
						tempCvor.prethodnaStanica = radniCvor;
						tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa + kasnjenje;
						
						if(req.type == 6) //ako je MIN_WALK ne racunaj vremena dolaska autobusa na stanicu
						{
							tempTime = currentTime.plusDays(((long)radniCvor.cenaPutanje + kasnjenje)/86400);
							tempTime = tempTime.plusHours((((long)radniCvor.cenaPutanje + kasnjenje)/3600)%24);
							tempTime = tempTime.plusMinutes((((long)radniCvor.cenaPutanje + kasnjenje)/60)%60);
							tempTime = tempTime.plusSeconds(((long)radniCvor.cenaPutanje + kasnjenje)%60);
							DatumVremeStanica vremeDolaska = new DatumVremeStanica(radniCvor.id, v.linija.id, tempTime.getSecond(), tempTime.getMinute(), tempTime.getHour(), tempTime.getDayOfWeek().getValue(), tempTime.getMonthValue(), tempTime.getYear(), tempInfo);
							tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = vremeDolaska;
						}
						else
							tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = null;
						
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
					
					//ovo je korekcija za MIN_WALK (treba, popraviti)
					if(req.type == 7 && radniCvor.linijom != null)
						udaljenost += 150;
					
					
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
			if(req.type == 7)
				minWalkPostProcessing(pseudoStart, pseudoEnd);
			
			Cvor c = pseudoEnd;
			
			Response response = new Response();
			response.type = req.type;
			
			if(req.type == 6)
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
	
	//rezim minimalnog pesacenja (napredni) (nedovrsen, stari pristup, sad to radi handleRequest6)
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
	
	//crowd sensing
	private void handleRequest10(Request req)
	{
		/*Linija targetLinija = owner.getGradskeLinije().linije[req.linija];
		
		if(targetLinija != null)
		{
			CSInfo csInfo = new CSInfo(req.srcLat, req.srcLon, req.crowded, req.stuffy, 
										targetLinija.broj.replace("*", ""), targetLinija.broj, req.message);
			owner.getGradskeLinije().addCSInfo(csInfo);
		}*/
		owner.getGradskeLinije().addCSInfo(req.crowdSensing);
	}
	
	//zahtevaju se informacije o polozaju kontrole
	private void handleRequest11(Request req)
	{
		
	}
	
	//kasnjenje linije u sekundama, za cvor c, u odredjeno vreme, za liniju l
	@SuppressWarnings("deprecation")
	private long izracunajKasnjenjeLinije2(Linija l, Cvor c)
	{
		LocalDateTime targetDateTime = LocalDateTime.now();
		Date realLifeDate = new Date();
		
		long realLifeSeconds = realLifeDate.getTime() / 1000;
		
		long busTravelSeconds = izracunajKorekciju(l, c);
		
		long futureShiftSeconds = (long) c.cenaPutanje;
		
		long cvorSeconds = realLifeSeconds + futureShiftSeconds;
		
		long sourceSeconds = cvorSeconds - busTravelSeconds;
		
		targetDateTime = targetDateTime.plusDays((futureShiftSeconds-busTravelSeconds)/86400);
		targetDateTime = targetDateTime.plusHours(((futureShiftSeconds-busTravelSeconds)/3600)%24);
		targetDateTime = targetDateTime.plusMinutes(((futureShiftSeconds-busTravelSeconds)/60)%60);
		targetDateTime = targetDateTime.plusSeconds((futureShiftSeconds-busTravelSeconds)%60);
		

		long targetSeconds = sourceSeconds;

		targetSeconds -= (targetDateTime.getMinute()*60 + targetDateTime.getSecond());
		
		int mat[][] = null;
		boolean found = false;
		
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
					
					if(l.cSourcedData[h][i] != null)
						tempInfo = new CSInfo(l.cSourcedData[h][i]);
					else
						tempInfo = null;
				}
			}
			else
			{
				targetSeconds += 3600;
				targetDateTime = targetDateTime.plusHours(1);
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
		
		return targetSeconds - sourceSeconds;
	}
	
	private void minWalkPostProcessing(Cvor pseudoStart, Cvor pseudoEnd)
	{
		double koeficijentUbrzanjaBuseva = 1.0;
		ArrayList<Cvor> putanja = new ArrayList<>();

		Cvor c = pseudoEnd;
		while(c != null)
		{
			putanja.add(0, c);
			
			c = c.prethodnaStanica;
		}
		
		double akumuliranaCena = 0.0;
		
		for(int i = 1; i < putanja.size()-1; ++i)
		{
			c = putanja.get(i);
			
			if(i > 1)
			{
				if(c.linijom != null)
					akumuliranaCena += c.prethodnaStanica.vratiVezu(c.linijom).weight/(koeficijentUbrzanjaBuseva * ServerConsts.brzinaAutobusa);
				else
					akumuliranaCena += calcDistance(c.prethodnaStanica, c)/ServerConsts.brzinaPesaka;
				
				c.cenaPutanje = c.heuristika*ServerConsts.brzinaAutobusa/ServerConsts.brzinaPesaka
								+ akumuliranaCena;
			}
			else
				c.cenaPutanje = c.heuristika*ServerConsts.brzinaAutobusa/ServerConsts.brzinaPesaka;
		}
		
		double minCena = Double.MAX_VALUE;
		int minI = -1;
		
		for(int i = 1; i < putanja.size()-1; ++i)
		{
			c = putanja.get(i);
			
			if(minCena > c.cenaPutanje)
			{
				minCena = c.cenaPutanje;
				minI = i;
			}
		}
		
		if(minI != -1)
			pseudoEnd.prethodnaStanica = putanja.get(minI);
		
		//---------------------------------obradjen je kraj puta
		
		ArrayList<Cvor> obradjenaPutanja = new ArrayList<>();
		ArrayList<Linija> obradjeneLinije = new ArrayList<>();
		
		c = pseudoEnd;
		while(c != null)
		{
			c.heuristika = calcDistance(pseudoStart.lat, pseudoStart.lon, c.lat, c.lon)/ServerConsts.brzinaPesaka;
			
			obradjenaPutanja.add(0, c);
			obradjeneLinije.add(0, c.linijom);
			
			c = c.prethodnaStanica;
		}
		
		Linija l;
		akumuliranaCena = 0.0;
		//ovo ovde je jedan veliki znak pitanja :D
		for(int i = 1; i < obradjenaPutanja.size()-1; ++i)
		{
			c = obradjenaPutanja.get(i);
			l = obradjeneLinije.get(i+1);
			
			if(l != null)
				akumuliranaCena += c.vratiVezu(l).weight/ServerConsts.brzinaAutobusa;
			else
				akumuliranaCena += calcDistance(c, obradjenaPutanja.get(i+1))/ServerConsts.brzinaPesaka;
		
			c.cenaPutanje = c.heuristika - akumuliranaCena; //pogotovo minus :)
		}
		
		minCena = Double.MAX_VALUE;
		minI = -1;
		
		for(int i = 1; i < obradjenaPutanja.size()-1; ++i)
		{
			c = obradjenaPutanja.get(i);
			
			if(minCena > c.cenaPutanje)
			{
				minCena = c.cenaPutanje;
				minI = i;
			}
		}
		
		if(minI != -1)
		{
			c = obradjenaPutanja.get(minI).prethodnaStanica = pseudoStart;
			c.linijom = null;
		}
		
		//---------------------------obradjen pocetak puta
		
	}
	
	private void izracunajPrioriteteLinija(GradskeLinije gradskeLinije)
	{
		Veza v = null;
		Cvor c = null;
		
		for(Linija l : gradskeLinije.linije)
			if(l != null)
			{
				c = l.pocetnaStanica;
				l.prioritet = c.heuristika;
				
				while((v = c.vratiVezu(l)) != null)
				{
					c = v.destination;
					
					if(c == l.pocetnaStanica)
						break;
					
					if(c.heuristika < l.prioritet)
						l.prioritet = c.heuristika;
				}
			}
	}
	
	private double brzinaAutobusa(Linija linija)
	{
		if(linija == null)
			return ServerConsts.brzinaPesaka;
		else
			return linija.vratiTrenutnuBrzinu();
	}
	
	public ArrayList<CSInfo> getCrowdInfo(Linija l, int korekcija)
	{

		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DAY_OF_WEEK);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);

		ArrayList<CSInfo> cInfo = new ArrayList<>();
		hour -= korekcija / 3600;
		minute -= (korekcija % 3600) / 60;
		
		while(minute < 0)
		{
			hour--;
			minute += 60;
		}
		
		if(hour < 0)
		{
			hour += 24;
			day--;
		}

		if(day == Calendar.SUNDAY)
        {

            for (int i = hour; i < 25 && i <= now.get(Calendar.HOUR_OF_DAY); i++)
            {
                int j = 0;
                while ((j < 60) && (l.matNedelja[i][j] != -1))
                {
                    if(minute <= l.matNedelja[i][j] && minute <= now.get(Calendar.HOUR_OF_DAY))
                    {
                    	CSInfo newCSI = null;
                    	if(l.cSourcedData[i][j] != null)
                    	{
                    		newCSI = new CSInfo(l.cSourcedData[i][j]);
                    		newCSI.cas = i;
                    		newCSI.minut = l.matNedelja[i][j];
                    		cInfo.add(newCSI);
                    	}
                    }
                        //vremena.add(i * 100 + l.matNedelja[i][j]);
                    j++;
                }

                minute = -1;
            }
        }else if(day == Calendar.SATURDAY)
        {
            for (int i = hour; i < 25 && i <= now.get(Calendar.HOUR_OF_DAY); i++)
            {
                int j = 0;
                while ((j < 60) && (l.matSubota[i][j] != -1))
                {
                    if (minute <= l.matSubota[i][j] && minute <= now.get(Calendar.HOUR_OF_DAY))
                    {
                    	CSInfo newCSI = null;
                    	if(l.cSourcedData[i][j] != null)
                    	{
                    		newCSI = new CSInfo(l.cSourcedData[i][j]);
                    		newCSI.cas = i;
                    		newCSI.minut = l.matSubota[i][j];
                    		cInfo.add(newCSI);
                    	}
                    }
                    j++;
                }

                minute = -1;
            }

            for(int i = 0; i < 3 && i <= now.get(Calendar.HOUR_OF_DAY); i++)
            {
                int j = 0;
                while ((j < 60) && (l.matNedelja[i][j] != -1))
                {
                    if (minute <= l.matNedelja[i][j] && minute <= now.get(Calendar.HOUR_OF_DAY))
                    {
                    	CSInfo newCSI = null;
                    	if(l.cSourcedData[i][j] != null)
                    	{
                    		newCSI = new CSInfo(l.cSourcedData[i][j]);
                    		newCSI.cas = i;
                    		newCSI.minut = l.matNedelja[i][j];
                    		cInfo.add(newCSI);
                    	}
                    }
                    j++;
                }
            }
        }else if(day == Calendar.FRIDAY)
        {
            for (int i = hour; i < 25 && i <= now.get(Calendar.HOUR_OF_DAY); i++)
            {
                int j = 0;
                while ((j < 60) && (l.matRadni[i][j] != -1))
                {
                    if (minute <= l.matRadni[i][j] && minute <= now.get(Calendar.HOUR_OF_DAY))
                    {
                    	CSInfo newCSI = null;
                    	if(l.cSourcedData[i][j] != null)
                    	{
                    		newCSI = new CSInfo(l.cSourcedData[i][j]);
                    		newCSI.cas = i;
                    		newCSI.minut = l.matRadni[i][j];
                    		cInfo.add(newCSI);
                    	}
                    }
                    j++;
                }

                minute = -1;
            }

            for(int i = 0; i < 3 && i <= now.get(Calendar.HOUR_OF_DAY); i++)
            {
                int j = 0;
                while ((j < 60) && (l.matSubota[i][j] != -1))
                {
                    if (minute <= l.matSubota[i][j] && minute <= now.get(Calendar.HOUR_OF_DAY))
                    {
                    	CSInfo newCSI = null;
                    	if(l.cSourcedData[i][j] != null)
                    	{
                    		newCSI = new CSInfo(l.cSourcedData[i][j]);
                    		newCSI.cas = i;
                    		newCSI.minut = l.matSubota[i][j];
                    		cInfo.add(newCSI);
                    	}
                    }
                    j++;
                }
            }
        }
        else
        {
            for (int i = hour; i < 25 && i <= now.get(Calendar.HOUR_OF_DAY); i++)
            {
                int j = 0;
                while ((j < 60) && (l.matRadni[i][j] != -1))
                {
                    if (minute <= l.matRadni[i][j] && minute <= now.get(Calendar.HOUR_OF_DAY))
                    {
                    	CSInfo newCSI = null;
                    	if(l.cSourcedData[i][j] != null)
                    	{
                    		newCSI = new CSInfo(l.cSourcedData[i][j]);
                    		newCSI.cas = i;
                    		newCSI.minut = l.matRadni[i][j];
                    		cInfo.add(newCSI);
                    	}
                    }
                    j++;
                }

                minute = -1;
            }
        }
		return cInfo;
	}
	
	//predjeni put do stanice stanica, linijom linija [vreme!?!?]
	private int izracunajKorekciju(Linija linija, Cvor stanica, int predjeniPut)
	{
		return (int) (predjeniPut/this.brzinaAutobusa(linija));
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
		if(cvor1.id > 0 && cvor2.id > 0)
			return matricaUdaljenosti[cvor1.id][cvor2.id];
		else
			return calcDistance(cvor1.lat, cvor1.lon, cvor2.lat, cvor2.lon);
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

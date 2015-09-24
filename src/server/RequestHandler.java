package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;

import com.google.gson.*;

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
	Gson gson;
	
	public RequestHandler(ClientWorker owner)
	{
		this.owner = owner;
		this.log = ServerLog.getInstance();
		this.gson = new GsonBuilder().create();
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
				handleRequest0(req);
				break;
			case 1:
				handleRequest1(req);
				break;	
			case 3:
				handleRequest3(req);
				break;
			case 4:
				handleRequest4(req);
				break;
			case 5:
				handleRequest5(req);
				break;
			default:
				log.write("Thread["+ owner.getId() + "] " +"Nepoznat request type = " + req.type);
				break;
			}

			
		} catch (IOException e)
		{
			log.write("Thread["+ owner.getId() + "] " + "failed to please");;
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
		Cvor stanice[] = g.getStanice().toArray(new Cvor[g.getStanice().size()]);
		Cvor sourceCvor = null;
		Linija linije[] = g.getGradskeLinije().linije;
		double pesacenje[] = new double[stanice.length];
		Double minimalnoPesacenje = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon);
		Integer responseStanice[] = new Integer[2]; //responseStanice[0] najbliza source stanica, responseStanice[1] najbliza destination stanica
		ArrayList<Integer> responseLinije = null;
		
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
		
		Integer responseKorekcije[] = new Integer[responseLinije.size()];
		Integer responseLinijeArray[] = (Integer[]) responseLinije.toArray(new Integer[responseLinije.size()]);
		
		for(int i = 0; i < responseKorekcije.length; ++i)
		{
			responseKorekcije[i] = izracunajKorekciju(linije[responseLinijeArray[i]], sourceCvor);
		}
		
		String responseStr = (new Response(req.type, responseStanice, responseLinijeArray, responseKorekcije, null, null, null)).toString();
		
		log.write("Thread [" + owner.getId() + "] client=" +clientSocket.getInetAddress().toString()+ " RESPONSE= " + responseStr);
		
		out.write(responseStr + "\n");
		out.flush();
	}

	//rezim minimalnog pesacenja (napredni)
	private void handleRequest5(Request req)
	{
		Graf g = owner.getGraf();
		Cvor stanice[] = g.getStanice().toArray(new Cvor[g.getStanice().size()]);
		Cvor sourceCvor = null;
		Linija linije[] = g.getGradskeLinije().linije;
		double startPesacenje[] = new double[stanice.length];
		double endPesacenje[] = new double[stanice.length];
		
		ArrayList<Cvor> startStanice = new ArrayList<>();
		ArrayList<Cvor> endStanice = new ArrayList<>();
		
		LinkedList<Cvor> linkedQueue = new LinkedList<>();
		
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
		int s = 0, e = 0;
		while(!nasoPut)
		{
			g.resetujCvorove();
			//pomBFS(start, end);
		}
		
	}
	
	//pomocna funkcija za rezim minimalnog pesacenja (napredni), obilazak po sirini
	//ostavlja izmenjenu strukturu grafa, vraca true ako je nadje put
	private boolean pomBFS(Cvor start, Cvor end)
	{
		boolean nasoPut = false;
		
		
		return nasoPut;
	}
	
	
	//predjeni put do stanice stanica, linijom linija
	private int izracunajKorekciju(Linija linija, Cvor stanica, int predjeniPut)
	{
		return (int) (predjeniPut/ServerConsts.brzinaAutobusa);
	}

	private int izracunajKorekciju(Linija linija, Cvor targetStanica)
	{
		int predjeniPut = 0;
		
		if(linija == null || targetStanica==null)
			return Integer.MAX_VALUE;
		
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*private void pleaseRequest0()
	{
		String line = null;
		NovaOsoba no = null;
		boolean succ = false;
		int fileSize = 0;
		int id = -1;
		
		try
		{
			if((line = in.readLine()) != null)
				no = gson.fromJson(line, NovaOsoba.class);
			else
				return;
			
			succ = DataLayer.dodajOsobu(no.getUser(), no.getPass(), no.getIme(), no.getPrezime(), no.getUser()+no.getSlika(), no.getBrTelefona());
			
			fileSize = no.getVelicinaSlike();
			
			if(fileSize > 0)
				succ &= acceptPicture(fileSize, no.getUser()+no.getSlika());
			
			id = DataLayer.loginCheck(no.getUser(), no.getPass());
			
			if(succ)
				out.write("true\n" + id +"\n");
			else
				out.write("false\n");
			
			out.flush();
		} catch (IOException e)
		{
			log.write(e.getMessage());
		}
		
	}
	
	private boolean acceptPicture(int fileSize, String fName)
	{
		int totalBytesRead = 0;
		byte[] data = null;
		BufferedOutputStream fStream = null;
		
		data = new byte[fileSize];
		
		try
		{
			while (totalBytesRead < fileSize) 
			{
				int bytesRemaining = fileSize - totalBytesRead;
				int bytesRead;

				bytesRead = istream.read(data, totalBytesRead, bytesRemaining);

				if (bytesRead == -1) 
				{
					log.write("SOCKET CLOSED! before picture sending was finished");
					break; // socket has been closed
				}
	
				totalBytesRead += bytesRead;
			}
			
			fStream = new BufferedOutputStream(new FileOutputStream(ServerConsts.SLIKE_PATH + fName));
			
			fStream.write(data);
			fStream.flush();
			fStream.close();
		} catch (Exception e)
		{
			log.write(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	//vrati osobu
	private void pleaseRequest1()
	{
		String line = null;
		int id = -1;
		
		try
		{
			if((line = in.readLine()) != null)
				id = Integer.parseInt(line);
			else
				return;
			
			OsobaPlus op = DataLayer.getOsobaPlus(id);
			String buf = op.toString();
			
		    File file = new File(ServerConsts.SLIKE_PATH + op.getSlika());
		    
		    out.write(buf + "\n" + (int) file.length() + "\n");
			out.flush();
		    
		    byte[] fileData = new byte[(int) file.length()];
		    DataInputStream dis = new DataInputStream(new FileInputStream(file));
		    dis.readFully(fileData);
			ostream.write(fileData);
			ostream.flush();
			dis.close();
		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}
	
	//vrati sve questove za osobu
	private void pleaseRequest2()
	{
		String line = null;
		int id = -1;
		
		try
		{
			if((line = in.readLine()) != null)
				id = Integer.parseInt(line);
			else
				return;
			
			ArrayList<Quest> questovi = DataLayer.getAllQuestsOsoba(id);
			
			String buf = gson.toJson(questovi);
			
			out.write(buf);
			out.flush();
		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}
	
	//vrati sve osobe sa poenima i sortirane po broju poena
	private void pleaseRequest3()
	{
		ArrayList<OsobaReducedPlus> osobe = DataLayer.getAllOsobeSorted();
		
		String buf = gson.toJson(osobe);
		
		out.write(buf);
		out.flush();
	}
	
	//vrati sve prijatelje osobe
	private void pleaseRequest4()
	{
		String line = null;
		int id = -1;
		
		try
		{
			if((line = in.readLine()) != null)
				id = Integer.parseInt(line);
			else
				return;
			
			ArrayList<OsobaReduced> prijatelji = DataLayer.getAllPrijateljiOsobe(id);
			
			String buf = gson.toJson(prijatelji);
			
			out.write(buf);
			out.flush();
		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}
	
	//vrati sve planine
	private void pleaseRequest5()
	{
		ArrayList<Planina> planine = DataLayer.getAllPlanine();
		
		String buf = gson.toJson(planine);
		
		out.write(buf);
		out.flush();
	}
	
	//login check
	private void pleaseRequest6()
	{
		String user = "";
		String pass = "";
		int id = -1;
		
		try
		{
			if((user = in.readLine()) != null)
				if((pass = in.readLine()) != null)
					id = DataLayer.loginCheck(user, pass);
			
		} catch (IOException e)
		{
			log.write(e.getMessage());
		} finally
		{
			out.write(Integer.toString(id));
			out.flush();
		}
	}
	
	//questovi i mesta
	private void pleaseRequest7()
	{
		String questName,placeNumberS,planinaIDS,osobaIDS;
		int placeNumber,planinaID,osobaID;
		placeNumber = -1;
		planinaID = -1;
		osobaID = -1;
		
		try
		{
			if((questName = in.readLine()) != null)
				if((planinaIDS = in.readLine()) != null)
					if((osobaIDS = in.readLine()) != null)
						if((placeNumberS = in.readLine()) != null)
						{
							placeNumber = Integer.parseInt(placeNumberS);
							planinaID = Integer.parseInt(planinaIDS);
							osobaID = Integer.parseInt(osobaIDS);
						}
			int questID = DataLayer.addQuest(questName, planinaID, osobaID, placeNumber);
			if(questID == -1)
			{
				out.write("false");
				out.flush();
				return; 
			}
			NovoMesto novoMesto = null;
			for(int i = 0; i < placeNumber; i++)
			{
				String line = in.readLine();
				novoMesto = gson.fromJson(line, NovoMesto.class);
				boolean b = DataLayer.addMesto(novoMesto, questID);
				if(!b)
				{
					DataLayer.DeleteQuest(questID);
					out.write("false");
					out.flush();
					return; 
				}
			}
		} catch (IOException e)
		{
			log.write(e.getMessage());
			out.write("false");
			out.flush();
			return; 
		} 
		catch(Exception e)
		{
			log.write(e.getMessage());
			out.write("false");
			out.flush();
			return; 
		}
		
		out.write("true");
		out.flush();
	}
	
	//dodavanje prijateljstva
	private void pleaseRequest8()
	{
		int id1 = -1;
		int id2 = -1;
		String line = "";
		boolean succ = false;
			
		try
		{
			if((line = in.readLine()) != null)
			{
				id1 = Integer.parseInt(line);
				if((line = in.readLine()) != null)
				{
					id2 = Integer.parseInt(line);
					succ = DataLayer.dodajPrijateljstvo(id1, id2);
				}
			}
				
		} catch (IOException e)
		{
			log.write(e.getMessage());
		} finally
		{
			if(succ)
				out.write("true");
			else
				out.write("false");
			out.flush();
		}
	}
		
	//vrati sve questove za osobu
	private void pleaseRequest9()
	{
		String line = null;
		int id = -1;
		
		try
		{
			if((line = in.readLine()) != null)
				id = Integer.parseInt(line);
			else
				return;
				
			ArrayList<Quest> questovi = DataLayer.getAllQuestsPlanina(id);
				
			String buf = gson.toJson(questovi);
				
			out.write(buf);
			out.flush();
		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}
	
	//ping
	private void pleaseRequest10()
	{
		String line = null;
		OsobaMesto ping = null;
			
		try
		{
			if((line = in.readLine()) != null)
				ping = gson.fromJson(line, OsobaMesto.class);
			else
				return;
			
			if(ping.getId() < 1)
				return;
			
			updateOsobaOnline(ping);
					
		} catch (Exception e)
		{
			log.write("pleaseRequest10() / PING failed");
			log.write(e.getMessage());
		}
	}
	
	public synchronized void updateOsobaOnline(OsobaMesto osoba)
	{
		osoba.setIp(clientSocket.getRemoteSocketAddress().toString());
		
		synchronized(Main.onlineOsobe)
		{
			int pos = Main.onlineOsobe.indexOf(osoba);
			
			if(pos == -1)
			{
				osoba.settStamp(System.currentTimeMillis() / 1000L);
				Main.onlineOsobe.add(osoba);
			}
			else
			{
				OsobaMesto o = Main.onlineOsobe.get(pos);
				o.settStamp(System.currentTimeMillis() / 1000L);
				o.setLat(osoba.getLat());
				o.setLon(osoba.getLon());
				o.setIp(osoba.getIp());
			}
		}
	}
	
	//->osoba_id, quest_id <-sva_mesta za taj quest, dokle_je_stigo 
	private void pleaseRequest11()
	{
		String line1 = null;
		String line2 = null;
		int osoba_id = -1;
		int quest_id = -1;
				
		try
		{
			if((line1 = in.readLine())!=null && (line2 = in.readLine())!=null)
			{
				osoba_id = Integer.parseInt(line1);
				quest_id = Integer.parseInt(line2);
			}
			else
			{
				log.write("pleaseRequest11(" + osoba_id + ", " + quest_id + ") failed");
				return;
			}
			
			ArrayList<NovoMesto> mesta = DataLayer.getAllMestaQuest(quest_id);
			int progress = DataLayer.getOsobaProgressQuest(osoba_id, quest_id);
			
			String buf = gson.toJson(mesta) + "\n" + progress;
			
			out.write(buf);
			out.flush();
						
		} catch (Exception e)
		{
			log.write("pleaseRequest11(" + osoba_id + ", " + quest_id + ") failed");
			log.write(e.getMessage());
		}
	}
	
	//vrati sve prijatelje osobe online
	private void pleaseRequest12()
	{
		String line = null;
		int id = -1;
			
		try
		{
			if((line = in.readLine()) != null)
				id = Integer.parseInt(line);
			else
				return;
			
			ArrayList<OsobaReduced> prijatelji = DataLayer.getAllPrijateljiOsobe(id);
			ArrayList<OnlinePrijatelj> onlinePrijatelji = new ArrayList<>();
			
			synchronized(Main.onlineOsobe)
			{
				for(int i = 0; i < prijatelji.size(); ++i)
				{
					OsobaReduced or = prijatelji.get(i);
					OsobaMesto om = new OsobaMesto(or.getId(), 0.0, 0.0, null);
					int pos = Main.onlineOsobe.indexOf(om);
					if(pos != -1)
					{
						OsobaMesto o = Main.onlineOsobe.get(pos);
						if((System.currentTimeMillis()/1000L-o.gettStamp())<(ServerConsts.PING_TIMEOUT*2.0))
							onlinePrijatelji.add(new OnlinePrijatelj(o.getId(), or.getUser(), o.getLat(), o.getLon(), o.getIp()));
						else
							Main.onlineOsobe.remove(pos);
					}
				}
			}
			
			String buf = gson.toJson(onlinePrijatelji);
			
			out.write(buf);
			out.flush();
		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}
	
	//update napredak na questu i poene
	private void pleaseRequest13()
	{
		String line1 = null;
		String line2 = null;
		String line3 = null;
		String line4 = null;
		int osoba_id = -1;
		int quest_id = -1;
		int brMesta = -1;
		int poeni = 0;
				
		try
		{
			if((line1 = in.readLine())!=null && (line2 = in.readLine())!=null && (line3 = in.readLine())!=null && (line4 = in.readLine())!=null)
			{
				osoba_id = Integer.parseInt(line1);
				quest_id = Integer.parseInt(line2);
				brMesta = Integer.parseInt(line3);
				poeni = Integer.parseInt(line4);
			}
			else
				return;
			
			DataLayer.updateNapredak(osoba_id, quest_id, brMesta, poeni);
			
		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}
	
	//zapocni quest
	private void pleaseRequest14()
	{
		int osoba_id = -1;
		int quest_id = -1;
		String line = null;
		boolean succ = false;
			
		try
		{
			if((line = in.readLine()) != null)
			{
				osoba_id = Integer.parseInt(line);
				if((line = in.readLine()) != null)
				{
					quest_id = Integer.parseInt(line);
					succ = DataLayer.zapocniQuest(osoba_id, quest_id);
				}
			}
				
		} catch (IOException e)
		{
			log.write(e.getMessage());
		} finally
		{
			if(succ)
				out.write("true");
			else
				out.write("false");
			out.flush();
		}
	}
	
	//vrati sve online prijatelje u nekom radijusu (dodaj i mesta u radijusu)
	private void pleaseRequest15()
	{
		String line1 = null;
		String line2 = null;
		String line3 = null;
		String line4 = null;
		int id = -1;
		double lat = 0.0;
		double lon = 0.0;
		double distance = 0.0;
		
		try
		{
			if((line1 = in.readLine())!=null && (line2 = in.readLine())!=null && (line3 = in.readLine())!=null && (line4 = in.readLine())!=null)
			{
				id = Integer.parseInt(line1);
				lat = Double.parseDouble(line2);
				lon = Double.parseDouble(line3);
				distance = Double.parseDouble(line4);
			}
			else
				return;
			
			ArrayList<OsobaReduced> prijatelji = DataLayer.getAllPrijateljiOsobe(id);
			ArrayList<OnlinePrijatelj> onlinePrijatelji = new ArrayList<>();
			
			synchronized(Main.onlineOsobe)
			{
				for(int i = 0; i < prijatelji.size(); ++i)
				{
					OsobaReduced or = prijatelji.get(i);
					OsobaMesto om = new OsobaMesto(or.getId(), 0.0, 0.0, null);
					int pos = Main.onlineOsobe.indexOf(om);
					if(pos != -1)
					{
						OsobaMesto o = Main.onlineOsobe.get(pos);
						if((System.currentTimeMillis()/1000L-o.gettStamp())<(ServerConsts.PING_TIMEOUT*1.5) && (calcDistance(lat, lon, o.getLat(), o.getLon()) < distance))
							onlinePrijatelji.add(new OnlinePrijatelj(o.getId(), or.getUser(), o.getLat(), o.getLon(), o.getIp()));
						else
							Main.onlineOsobe.remove(pos);
					}
				}
			}
			
			String buf1 = gson.toJson(onlinePrijatelji);
			String buf2 = gson.toJson(DataLayer.getAllMestaURadijusu(lat, lon, distance));
			String buf3 = gson.toJson(DataLayer.getAllOsobaRadiQuestForOsoba(id));
			
			out.write(buf1 + "\n" + buf2 + "\n" + buf3 + "\n");
			out.flush();
		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}
	
	private void pleaseRequest16()
	{
		String line = null;
		int questId = -1;
		
		try
		{
			if((line = in.readLine()) != null)
				questId = Integer.parseInt(line);
			else
				return;
			
			Quest q = DataLayer.getQuest(questId);
			Planina p = DataLayer.getPlanina(q.getPlanina_id());
			String buf = q.toString() + "\n" + p.toString() + "\n";
			
			out.write(buf);
			out.flush();

		} catch (Exception e)
		{
			log.write(e.getMessage());
		}
	}*/
	
	public static double calcDistance(Cvor stanica, double lat2, double long2)
	{
	    double a, c;

	    a = Math.sin((lat2 - stanica.lat)*Math.PI/360) * Math.sin((lat2 - stanica.lat)*Math.PI/360) +
	    	Math.sin((long2 - stanica.lon)*Math.PI/360) * Math.sin((long2 - stanica.lon)*Math.PI/360) * Math.cos(lat2 * Math.PI/180) * Math.cos(stanica.lat * Math.PI/180);

	    c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	    return 6371000 * c;
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

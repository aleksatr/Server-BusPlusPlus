package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import com.google.gson.*;
import strukture.*;


public class Pleaser
{
	private InputStream istream;
	private OutputStream ostream;
	private Socket clientSocket;
	private ServerLog log;
	private int requestType = -1;
	private BufferedReader in = null;
	private PrintWriter out = null;
	Gson gson;
	
	public Pleaser(Socket clientSocket, InputStream istream, OutputStream ostream)
	{
		this.clientSocket = clientSocket;
		this.log = ServerLog.getInstance();
		this.istream = istream;
		this.ostream = ostream;
		this.gson = new GsonBuilder().serializeNulls().create();
		
		in = new BufferedReader(new InputStreamReader(istream));
		out = new PrintWriter(ostream);
	}
	
	public void please()
	{
		String line = null;
		
		try
		{
			line = in.readLine();
			
			if(line == null)
				return;
			
			Request rq = gson.fromJson(line, Request.class);
			
			System.out.println(line);
			
			/*if((line = in.readLine()) != null)
				requestType = Integer.parseInt(line);
			else
				return;*/
			
			/*switch(requestType)
			{
			case 0:
				pleaseRequest0();
				break;
			case 1:
				pleaseRequest1();
				break;
			case 2:
				pleaseRequest2();
				break;
			case 3:
				pleaseRequest3();
				break;
			case 4:
				pleaseRequest4();
				break;
			case 5:
				pleaseRequest5();
				break;
			case 6:
				pleaseRequest6();
				break;
			case 7:
				pleaseRequest7();
				break;
			case 8:
				pleaseRequest8();
				break;
			case 9:
				pleaseRequest9();
				break;
			case 10:
				pleaseRequest10();
				break;
			case 11:
				pleaseRequest11();
				break;
			case 12:
				pleaseRequest12();
				break;
			case 13:
				pleaseRequest13();
				break;
			case 14:
				pleaseRequest14();
				break;
			case 15:
				pleaseRequest15();
				break;
			case 16:
				pleaseRequest16();
				break;
			default:
				log.write("Nepoznat requestType = " + requestType);
				break;
			}*/
			
		} catch (IOException e)
		{
			log.write(e.getMessage());
		}

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
	
	public static double calcDistance(double lat1, double long1, double lat2, double long2)
	{
	    double a, c;

	    a = Math.sin((lat2 - lat1)*Math.PI/360) * Math.sin((lat2 - lat1)*Math.PI/360) +
	    	Math.sin((long2 - long1)*Math.PI/360) * Math.sin((long2 - long1)*Math.PI/360) * Math.cos(lat2 * Math.PI/180) * Math.cos(lat1 * Math.PI/180);

	    c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	    return 6371000 * c;
	}
	
}

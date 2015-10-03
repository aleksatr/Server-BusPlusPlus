package strukture;

import java.util.ArrayList;

public class PrioritetnaListaCvorova
{
	private ArrayList<Cvor> lista = null;
	
	public PrioritetnaListaCvorova()
	{
		lista = new ArrayList<>();
	}
	
	public boolean isEmpty()
	{
		return lista.isEmpty();
	}
	
	public int size()
	{
		return lista.size();
	}
	
	public Cvor get(int index)
	{
		return lista.get(index);
	}
	
	public boolean contains(Cvor c)
	{
		return lista.contains(c);
	}
	
	public Cvor remove(int index)
	{
		return lista.remove(index);
	}
	
	public boolean remove(Cvor c)
	{
		return lista.remove(c);
	}
	
	public void pushPriority(Cvor element)
	{
		Cvor c = null;
		boolean dodato = false;
		
		for(int i = 0; i < lista.size(); ++i)
		{
			c = lista.get(i);
			if(c.cenaPutanje + c.heuristika > element.cenaPutanje + element.heuristika)
			{
				lista.add(i, element);
				dodato = true;
				break;
			}
		}
		
		if(!dodato)
			lista.add(element);
	}
	
	public boolean add(Cvor c)
	{
		return lista.add(c);
	}
	
}

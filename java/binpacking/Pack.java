package binpacking;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Pack class, which stores the actual bin packing algorithms, as well as manages
 * the printing that it must do. It also stores the list of numbers to add to 
 * the bins, which are used by the algorithms.
 * 
 * @author Guido
 */

public class Pack
{  
    /**
     * Comparator class for Bins. Used on a PriorityQueue so that the smallest
     * used space bin is at the top.
     */
    private class reverse implements Comparator<Bin>
    {
        @Override
        public int compare(Bin lhs, Bin rhs)
        {
            int l = lhs.getCurrentSize();
            int r = rhs.getCurrentSize();

            if(l == r) 
                return 0;
            else if(l > r)
                return 1;
            else 
                return -1;
        }
    }
    
    private final LinkedList<Integer> items;
    private final LinkedList<Bin> bins;
    private final int sizeOfBins;
    
    public Pack(LinkedList<Integer> i)
    { 
        items = i; 
        bins = new LinkedList<>();
        sizeOfBins = 1000000000;
    }
    
    public Pack(LinkedList<Integer> i, int sb)
    { 
        items = i; 
        bins = new LinkedList<>();
        sizeOfBins = sb;
    }
    
    /**
     * Next Fit Algorithm:
     * 
     * Place the item in the last bin seen. If it does not fit there, then
     * create a new bin after it and place it there.
     */
    
    public void nextFit()
    {
        reset(); // clear arraylist of bins (for printing purposes)
        
        bins.add(new Bin(sizeOfBins));
        
        int i = 0;
        
        for(int e : items)
        {
            if(!bins.getLast().add(e))
            {
                bins.add(new Bin(sizeOfBins, e));
                i++;
            }
        }
    }
    
    /**
     * Worst Fit Algorithm:
     * 
     * Place the item in the bin that has the most space. If no bin is found,
     * then create a new bin and add the item to it.
     */
    
    public void worstFit()
    {
        reset(); // clear arraylist of bins (for printing purposes)
        
        PriorityQueue<Bin> pq = new PriorityQueue<>(items.size(), new reverse());
       
        pq.add(new Bin(sizeOfBins));
        
        for(int i : items)
        {
            Bin b = pq.remove();
            
            if(!b.add(i))
                pq.add(new Bin(sizeOfBins, i));
            
            pq.add(b);
        }
        
        Iterator it = pq.iterator();
        
        while(it.hasNext())
            bins.add((Bin)it.next());
    }
    
    /**
     * Best Fit Algorithm:
     * 
     * Place the items in the bins that are the most full, but still has enough
     * space for the items. Uses a Navigable TreeSet for the ceiling and flooring
     * operations that it has.
     */
    
    public void bestFit()
    {
        reset(); // clear arraylist of bins (for printing purposes)
        
        NavigableSet<Bin> ns = new TreeSet<>();
        ns.add(new Bin(sizeOfBins));
        
        for(Integer item : items)
        {
            Bin b = new Bin(sizeOfBins, sizeOfBins - item);
            
            if((b = ns.floor(b)) != null)
            {
                ns.remove(b);
                b.add(item);
                ns.add(b);
            } 
            else
            {
                ns.add(new Bin(sizeOfBins, item));
            }
        }
        
        Iterator it = ns.iterator();
        
        while(it.hasNext())
            bins.add((Bin)it.next());
    }
       
    /**
     * First Fit Algorithm:
     * 
     * Place the bin in the first bin that has space for it. Although it can be
     * done in O(N * N), O(N log N) is possible with the use of tournament trees.
     * A separate object of a class TournamentTree is created to handle this
     * algorithm separately.
     */
    
    public void firstFit()
    {
        reset();
        
        TournamentTree tt = new TournamentTree(sizeOfBins);
        tt.addList(items);
        
        Bin[] bn = tt.getBins();
        
        for(Bin b : bn)
            if(b.getCurrentSize() != 0)
                bins.add(b);
    }
   
    @Override
    public String toString()
    {
        String output = "The number of bins used: " + bins.size() + "\n";
        
        int a = Math.min(10, bins.size());
        
        Iterator it = bins.iterator();
        
        for(int i = 0 ; i < a ; i++)
            output += "Bin " + (i + 1) + ":\t" + ((Bin)it.next()).getContents() + "\n";
        
        return output;
    }
    
    private void reset()
    { 
        bins.clear(); 
        Bin.numberOfBins = 0;
    }
    
    public int getIdeal()
    { 
        double total = 0;
        
        for(int i : items)
            total += i;
        
        return (int)Math.ceil(total/sizeOfBins);
    }
    
    public LinkedList<Bin> getBins(){ return bins; }
    public LinkedList<Integer> getItems(){ return items; }
    public int getSize(){ return sizeOfBins; }
}

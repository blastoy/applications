package binpacking;

import java.util.LinkedList;

/**
 * Main bin class, which stores all the information that makes a bin. Includes
 * total size amongst other descriptions. Also stores the contents of each
 * bin in a LinkedList. Everything is pretty self explanatory.
 * 
 * @author Guido
 */

public class Bin implements Comparable<Bin>
{
    private int currentSize;
    private final int maxSize;
    private final int binNumber;
    protected static int numberOfBins = 0;
    private final LinkedList<Integer> contents;
    
    public Bin(int ms)
    {
        maxSize = ms;
        currentSize = 0;
        binNumber = numberOfBins++;
        contents = new LinkedList<>();
    }
    
    public Bin(int ms, int cs)
    {
        maxSize = ms;
        currentSize = cs;
        binNumber = numberOfBins++;
        contents = new LinkedList<>();
        contents.add(cs);
    }
    
    public boolean add(int e)
    {
        if(currentSize + e > maxSize)
            return false;
        
        currentSize += e;
        contents.add(e);
        
        return true;
    }
    
    @Override
    public int compareTo(Bin rhs)
    {
        if(currentSize == rhs.currentSize)
            return binNumber - rhs.binNumber;
        else
            return currentSize - rhs.currentSize;
    }
    
    @Override
    public String toString()
    {
        return "" + currentSize;
    }
    
    public boolean canFit(int e){ return maxSize - currentSize - e > -1; }
    
    public LinkedList<Integer> getContents(){ return contents; }
    public int getCurrentSize(){ return currentSize; }
    public int getMaxSize(){ return maxSize; }
    public int getBinNumber(){ return binNumber; }
    public int getNumberOfBins(){ return numberOfBins; }
}

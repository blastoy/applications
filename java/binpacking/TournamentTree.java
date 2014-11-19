package binpacking;

import java.util.LinkedList;

/**
 * The main tournament tree class, which contains all the methods to correctly
 * manage a binary heap representation of a complete tree. This simplifies
 * finding the first bin that has space for an item to a faster running time,
 * and only duplicates in size when needed.
 * 
 * @author Guido
 */

public class TournamentTree
{
    private Bin[] participants;
    private Integer[] winners;
    private int size;
    private final int sizeOfBins;
    private int numberOfPB;    
    
    /**
     * Initial size of the tree is 4, which is 4 phantom bins. 2 was not used
     * because 2 bins is exceptionally easy to fill, and bins do not take
     * much space. This saves the system an array resizing step.
     * 
     * @param sb the maximum size of a bin
     */
    
    public TournamentTree(int sb)
    {
        size = 4;
        
        sizeOfBins = sb;
        numberOfPB = 4;
        
        participants = new Bin[size];
        winners = new Integer[size];
        
        for(int i = 0 ; i < size ; i++)
            participants[i] = new Bin(sizeOfBins);
        
        winners[0] = -1;
        updateAll();
    }
    
    /**
     * Update function starts at a given node and works it's way up the tree
     * to solve any discrepancies in the winners once a new element is added.
     * 
     * @param start starting node index in the winners tree
     */
    
    public void update(int start)
    {
        int index = 2 * start;
            
        index -= size;

        if(participants[index].getCurrentSize() < participants[index + 1].getCurrentSize())
            winners[start] = index;
        else
            winners[start] = index + 1;

        while(true)
        {
            if(start % 2 == 0)
                start = start / 2;
            else
                start = (start - 1) / 2;

            index = 2 * start;

            if(participants[winners[index]].getCurrentSize() < participants[winners[index + 1]].getCurrentSize())
                winners[start] = winners[index];
            else
                winners[start] = winners[index + 1];

            if(start == 1)
                break;
        }
    }
    
    /**
     * Unlike update, this method does not take any parameters but instead updates
     * the WHOLE tree to create new winners connections. Usually used when the
     * tree needs to be resized, or when the tree is just created. All other
     * procedures are done with update() instead.
     */
    
    public final void updateAll()
    {
        for(int i = size - 1 ; i > 0 ; i--)
        {
            int index = 2 * i;
            
            if(index >= size)
            {
                index -= size;
                
                if(participants[index].getCurrentSize() < participants[index + 1].getCurrentSize())
                    winners[i] = index;
                else
                    winners[i] = index + 1;
            }
            else
            {
                if(participants[winners[index]].getCurrentSize() < participants[winners[index + 1]].getCurrentSize())
                    winners[i] = winners[index];
                else
                    winners[i] = winners[index + 1];
            }
        }
    }
    
    /**
     * Given an element, this method finds it's way t the left-most bin in the
     * tree that the element can fit. If this element fills the last empty
     * bin currently within the tree, the tree is resized.
     * 
     * @param e the element to add to the first bin with space 
     */
    
    public void add(int e)
    {
        int root = 1, index;
        
        while(true)
        {
            index = 2 * root;
            
            if(index > size - 1)
                break;
            
            if(participants[winners[index]].canFit(e))
                root = index;
            else
                root = index + 1;
        }
        
        index -= size;
        
        if(participants[index].canFit(e))
        {
            if(participants[index].getCurrentSize() == 0)
                numberOfPB--;
            
            participants[index].add(e);
        }
        else
        {
            if(participants[index + 1].getCurrentSize() == 0)
                numberOfPB--;
            
            participants[index + 1].add(e);
        }
        
        if(numberOfPB == 0)
            doubleTheSize();
        else
            update(root);
    }
    
    public void addList(LinkedList<Integer> items)
    {
        for(int i : items)
            add(i);
    }
    
    public void doubleTheSize()
    {
        int start = size;
        size *= 2;
        numberOfPB = size - start;
        
        Bin[] new_participants = new Bin[size];
        
        System.arraycopy(participants, 0, new_participants, 0, start);
        
        for(int i = start ; i < size ; i++)
            new_participants[i] = new Bin(sizeOfBins);
        
        participants = new_participants;
        winners = new Integer[size];
        
        winners[0] = -1;
        updateAll();
    }
    
    public Bin[] getBins(){ return participants; }
}

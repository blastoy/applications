package binpacking;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

/**
 * The following program performs some of the common bin packing algorithms
 * in two formats, online and offline, with running time on each as well as
 * bin content results and bin amounts. The algorithms are: next fit, worst
 * fit, best fit, and first fit.
 * 
 * @author Guido
 */

public class Test
{
   /**
    * Processes and reads a file for input. Stores records as numbers in a 
    * LinkedList to be accessed by the algorithms later.
    * 
    * @param file name of the file to be opened
    * @return a list containing all the records in the file (as numbers)
    * @throws IOException 
    */ 
    
    public static LinkedList<Integer> processFile(String file) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        LinkedList<Integer> list = new LinkedList<>();
     
        String line = br.readLine();
       
        do{ list.add(Integer.parseInt(line)); }
        while((line = br.readLine()) != null);
        
        return list;
    }
    
    /**
     * Knows how to handle 'Pack' objects, and performs algorithms on the objects
     * as well as prints the result. 
     * 
     * @param p Pack object to process
     * @param s for printing purposes only
     */
    
    public static void packingAlgorithms(Pack p, String s)
    {
        long startTime, endTime; 
                
        startTime = System.currentTimeMillis();
        System.out.println(s + " Next Fit:");
        p.nextFit();
        System.out.print(p);
        endTime = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (endTime - startTime) + " ms.\n");

        startTime = System.currentTimeMillis();
        System.out.println(s + " Worst Fit:");
        p.worstFit();
        System.out.print(p);
        endTime = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (endTime - startTime) + " ms.\n");
        
        startTime = System.currentTimeMillis();
        System.out.println(s + " Best Fit:");
        p.bestFit();
        System.out.print(p);
        endTime = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (endTime - startTime) + " ms.\n");
        
        startTime = System.currentTimeMillis();
        System.out.println(s + " First Fit:");
        p.firstFit();
        System.out.print(p);
        endTime = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (endTime - startTime) + " ms.\n");
    }
    
    /**
     * Creates a Package object, which contains the list of items to be used
     * by each algorithm as well as the bin packing algorithms themselves.
     * 
     * @param args files to process 
     */
    
    public static void main(String[] args)
    {
        if(args.length < 1)
        {
            System.out.println("USAGE: ");
            System.exit(0);
        }
        
        try
        {
            System.out.println("I used a Tournament Tree for First Fit!\n");
            
            for(String s : args)
            {
                Pack p = new Pack(processFile(s));
                System.out.println(s + ":\nIdeal number of bins: " + p.getIdeal() + "\n");

                packingAlgorithms(p, "Online");

                LinkedList<Integer> list = p.getItems();
                Collections.sort(list, Collections.reverseOrder());

                p = new Pack(list);
                packingAlgorithms(p, "Offline");
                
                System.out.println("-----------------------------------\n");
            }
        } 
        catch(IOException e)
        {
            System.out.println(args[0] + ": There was a problem processing this file.");
        }
    }
}

package maze;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
* This program finds the shortest path in a maze file formatted like so:
*	4 4
*	0 0 W
*       0 1 E
*       ..
* where the first line is the dimensions of the maze and each consecutive line 
* contains the position of a 'square' in the maze and the walls around the 
* square. Boundary maze walls are redundant and are not included in the file. 
*
* @author	Guido Ruiz
* @version	1.0
* @since	2014-24-14
*/ 

public class Test
{
   /**
   * This method validates arguments provided by the console and creates maze
   * objects to process each file with given penalties.
   * 
   * @param args  consists of penalties and file names
   * @usage -p [penalties] -f [files]
   */ 
    
    public static void main(String[] args)
    {
        ArrayList<Integer> penalties = new ArrayList<>();
        ArrayList<String> files = new ArrayList<>();

        boolean atPenalties = false;
        boolean atFiles = false;

        for(String s : args)
        {
           if(s.equals("-p"))
           {
               atPenalties = true;
               atFiles = false;
               continue;
           }
           
           if(s.equals("-f"))
           {
               atPenalties = false;
               atFiles = true;
               continue;
           }

           if(atPenalties)
           {
                try
                {
                    penalties.add(Integer.parseInt(s));
                }
                catch(NumberFormatException e)
                {
                    System.out.println("Argument \"" + s + "\" is not a valid penalty.");
                }
           }
           
           if(atFiles)
               files.add(s);
        }

        if(penalties.isEmpty() || files.isEmpty())
        {
            System.out.println("Usage: -p <penalties> -f <files>\nExample: -p 1 2 -f data.txt data2.txt");
        }
        else
        {
            for(String f : files)
            {
                try
                {
                    Maze m = new Maze(f);

                    for(int i : penalties)
                        m.computeDistances(i);
                }
                catch(FileNotFoundException e)
                {
                    System.out.println(f + ": File does not exist.");
                }
                
                System.out.print("\n");
            }
        }
    }
}

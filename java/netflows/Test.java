package networkflows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * This program finds the minimum k, in which k is the preference of Person A
 * to Person B, such that every person has a match. In this case, males to 
 * women, although could be applied to any scenario. The main algorithm used
 * in this program is Ford Fulkerson's Algorithm, with a queue based BFS.
 * 
 * The file must be in a specific format, shown by one of the sample "friends"
 * within this source code. 
 * 
 * @author Guido
 */

public class Test
{
    /**
     * This function creates an adjacency graph of the data designated within
     * a specific formatted file. The graph is then passed towards an object
     * class to perform operations on it.
     * 
     * @param fileName the name of the file to open
     * @return FordFulkerson object with the created graph
     * @throws IOException if file is not found
     */
    public static FordFulkerson processFile(String fileName) throws IOException, IndexOutOfBoundsException
    {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        
        HashMap<String, Integer>peopleToNum = new HashMap<>();
        HashMap<Integer, String>numToPeople = new HashMap<>();
        
        String line = br.readLine();
        String[] elems = line.split(":");
        String[] prefs = elems[1].split(",");
        
        int bachelors = prefs.length;
        int totalVertices = 2 + (2 * bachelors);
        int[][] graph = new int[totalVertices][totalVertices];
        
        int maleIndex = 1;
        int femaleIndex = bachelors + 1;
        
        peopleToNum.put(elems[0], maleIndex);
        numToPeople.put(maleIndex, elems[0]);

        for(int i = 0 ; i < prefs.length ; i++)
        {
            graph[maleIndex][femaleIndex] = i + 1;
            
            peopleToNum.put(prefs[i], femaleIndex);
            numToPeople.put(femaleIndex++, prefs[i]);
        }
        
        while((line = br.readLine()) != null)
        {
            if(line.equals(""))
                break;
            
            elems = line.split(":");
            prefs = elems[1].split(",");
            
            if(prefs.length != bachelors)
                throw new IOException();
            
            peopleToNum.put(elems[0], ++maleIndex);
            numToPeople.put(maleIndex, elems[0]);
        
            for(int i = 0 ; i < prefs.length ; i++)
                graph[maleIndex][peopleToNum.get(prefs[i])] = i + 1;
        }
        
        while((line = br.readLine()) != null)
        {
            if(line.equals(""))
                continue;
            
            elems = line.split(":");
            prefs = elems[1].split(",");
            
            if(prefs.length != bachelors)
                throw new IOException();
        
            for(int i = 0 ; i < prefs.length ; i++)
                graph[peopleToNum.get(elems[0])][peopleToNum.get(prefs[i])] = i + 1;     
        }
        
        return new FordFulkerson(graph, numToPeople, totalVertices, bachelors);
    }
    
    public static void main(String[] args)
    {
        System.out.println("I did the extra credit for this assignment\n");
        
        for(String s : args)
        {
            try
            {   
                FordFulkerson fg = processFile(s);
                
                long startTime = System.currentTimeMillis();
                
                System.out.println(s + ":");
                System.out.print(fg.findMax());
                
                long endTime = System.currentTimeMillis();
                
                System.out.println("Elapsed time: " + (endTime - startTime) + " ms.\n");
            }
            catch(IOException | ArrayIndexOutOfBoundsException e)
            {
                System.out.println(s + ":\nThis file is not formatted correctly!\n");
            }
        }
    }
}

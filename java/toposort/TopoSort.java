package toposort;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.LinkedHashSet;
import java.util.Iterator;

/**
* This program finds a topological sort of a file formatted like so:
*	1 2
*	3 4
* where each line contains two elements, and element two depends on element one.
* It also finds a cycle if a topological sort is impossible.
*
* @author	Guido Ruiz
* @version	1.0
* @since	2014-09-11
*/ 

public class TopoSort
{
   /**
   * If a topological sort was not found, this method finds any cycle within the graph.
   * @param  adjacents  HashMap of each object and its adjacent objects (new objects that depend on the original object).
   * @param  indegrees  HashMap of every object and its amount of vertices that depend on it.
   * @return Nothing.
   */ 

    private static String fileName;

    private static void findCycle(Map<String, List<String>> adjacents, Map<String, Integer> indegrees)
    {
        Map<String, Integer> badIndegrees = new HashMap<>();
        Map<String, List<String>> incomings = new HashMap<>();
        LinkedHashSet<String> used = new LinkedHashSet<>();

        List<String> allValues;
        List<String> incToV;

        for(String key : adjacents.keySet())  // This function reverses the adjacency map.
        {
            allValues = adjacents.get(key);

            for(String value : allValues)
            {
                incToV = incomings.get(value);

                if(incToV == null)
                {
                    incToV = new ArrayList<>();
                    incomings.put(value, incToV);
                }

                incToV.add(key);
            }
        }

        int v;

        for(String k : indegrees.keySet())  // This function grabs all indegrees > 0.
        {
            v = indegrees.get(k);

            if(v != 0)
                badIndegrees.put(k, v);
        }

        String aKey = badIndegrees.keySet().iterator().next();  // Get random key in a cycle.
        String lastVal = null;

        boolean done = false;
        boolean needNewKey;

        while(!done)
        {
            needNewKey = true;
            used.add(aKey);
            allValues = incomings.get(aKey);

            for(String val : allValues)
            {
                if(used.contains(val))  // Find if the key is within the used set.
                {
                    done = true;
                    lastVal = val;
                }
                if(needNewKey && !done)  // If not, find a new key that is incoming to this key.
                {
                    if(badIndegrees.get(val) != null)
                    {
                        aKey = val;
                        needNewKey = false;
                    }
                }
            }
        }

        LinkedList<String> list = new LinkedList<>(used);
        Iterator<String> itr = list.descendingIterator();

        String s = itr.next();

        System.out.print(fileName + ": graph has a cycle: " + lastVal + "->");

        while(!s.equals(lastVal))
        {
            System.out.print(s + "->");
            s = itr.next();
        }

        System.out.println(lastVal);
    }

   /**
   * Finds a topological order, if exists, of a given properly formatted file.
   * All credit goes to Mark Weiss for the below algorithm to find a topological order.
   * @param  file  This is a given string with the file path of a file to open. 
   * @exception IOException on BufferedReader.
   * @return Nothing.
   */ 

    private static void findTopo(String file) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));

        LinkedList<String> queue = new LinkedList<>();
        List<String> order = new ArrayList<>();
        List<String> adjToV;
        Map<String, Integer> indegrees = new HashMap<>();
        Map<String, List<String>> adjacents = new HashMap<>();

        String v, w;
        Integer count, vcount;
        int lineCount = 1;

        String line = br.readLine();
        String[] results;

        while(line != null)
        {
            results = line.split(" ");

            if(results.length == 2 && !(results[0].equals(results[1])))  // Syntax error: More than one space, or two equal objects.
            {
                v = results[0];  // Credit to Mark Weiss for the algorithm below:
                w = results[1];

                adjToV = adjacents.get(v);

                if(adjToV == null)
                {
                    adjToV = new ArrayList<>();
                    adjacents.put(v, adjToV);
                }

                adjToV.add(w);

                count = indegrees.get(w);

                if(count == null)
                    indegrees.put(w, 1);
                else
                    indegrees.put(w, count + 1);

                vcount = indegrees.get(v);

                if(vcount == null)
                    indegrees.put(v, 0);
            }
            else
                System.out.print(fileName + ": Line " + lineCount + " - \"" + line + "\" has bad synyax and was skipped.\n");
        
            lineCount++;
            line = br.readLine();
        }

        for(Map.Entry<String, Integer> entry : indegrees.entrySet())
        {
            if(entry.getValue() == 0)
                queue.add(entry.getKey());
        }

        List<String> adj;

        while(!queue.isEmpty())
        {
            v = queue.removeFirst();
            order.add(v);

            adj = adjacents.get(v);

            if(adj == null)
                continue;

            for(String c : adj)
            {
                int newVal = indegrees.get(c) - 1;
                indegrees.put(c, newVal);

                if(newVal == 0)
                    queue.addLast(c);
            }
        }

        if(order.size() == indegrees.size())
            System.out.println(fileName + ": " + order);
        else
            findCycle(adjacents, indegrees);    
    }

    /**
    * Main function that processes multiple files and attempts to find a topological sort.
    * @param  args  Takes as many files as given.
    */ 

    public static void main(String[] args) 
    {			
        long startTime, endTime;

        for(String s : args)
        {
            startTime = System.nanoTime();
            fileName = s;  // Global variable to print the file name

            try
            {
                findTopo(s);
            }
            catch(IOException e)
            {
                System.out.println(fileName + ": Unable to open!");
            }

            endTime = System.nanoTime();
            System.out.println(fileName + ": time: " + (double)((endTime - startTime)/1000000000.0) + "\n");
        }	
    }
}
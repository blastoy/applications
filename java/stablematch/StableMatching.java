package stablematching;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Stack;

public class StableMatching 
{
    public static void findMatch(LinkedHashMap<String, Male> males, LinkedHashMap<String, Female> females)
    {
        Stack<String> st = new Stack<>();
        
        for(String s : males.keySet())
            st.push(s);
        
        while(!st.isEmpty())
        {
            String man = st.pop();
            
            Female proposal = females.get(males.get(man).getPreffered());
            males.get(man).setDate(proposal.getName());
            
            boolean rejected = proposal.isBetter(man);
            
            if(rejected)
            {
                String single = proposal.getRejectedDate();
                
                st.push(single);
                males.get(single).setDate(null);
            }
        }
        
        System.out.println("Everybody matched with top " + Person.k + " preferences:");
        
        for(Male m : males.values())
            System.out.println(m.getName() + ": matched to " + m.getMyDate() + " (Rank " + m.getTries() + ")");
        
        for(Female f : females.values())
            System.out.println(f.getName() + ": matched to " + f.getMyDate() + " (Rank " + (f.getMyDateRank() + 1) + ")");
    }
    
    public static void processFile(String file) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(file));
        
        LinkedHashMap<String, Male> males = new LinkedHashMap<>();
        LinkedHashMap<String, Female> females = new LinkedHashMap<>();
        
        String line;
        String[] elems;
        
        boolean atMales = true;
        
        while((line = br.readLine()) != null)
        {
            if(line.equals(""))
            {
                atMales = false;
                continue;
            }

            ArrayList<String> list = new ArrayList<>();

            elems = line.split(":");
            list.addAll(Arrays.asList(elems[1].split(",")));

            if(atMales)
                males.put(elems[0], new Male(elems[0], list));
            else
                females.put(elems[0], new Female(elems[0], list)); 
        }

        long startTime = System.currentTimeMillis();
        findMatch(males, females);
        long endTime = System.currentTimeMillis();

        System.out.println("Elapsed time: " + (endTime - startTime) + " ms.");
    }
    
    public static void main(String[] args)
    {  
        for(String s : args)
        {
            try
            {
                System.out.println(s + ":");
                processFile(s);
            }
            catch(IOException e)
            {
                System.out.println("There was a problem processing this file.");
            }
            
            System.out.print("\n");
        } 
    }
}
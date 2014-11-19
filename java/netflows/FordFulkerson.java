package networkflows;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class performs all the operations within specific formatted files to
 * find the minimum k required for all individuals to have a pair as described
 * by the main class.
 * 
 * @author Guido
 */

public class FordFulkerson
{   
    private final int[] parent;
    private final boolean[] visited;
    private final int totalVertices, bachelors;
    private final HashMap<Integer, String> numToPeople;
    private final int[][] graph, resGraph;
    
    /**
     * This constructor handles the initialization of the object, as well as 
     * creates a residual graph template that connects the start node to all
     * males and all women to the end node.
     * 
     * @param g the adjacency matrix of preferences
     * @param ntp number to male map to get the name of a number within the graph
     * @param tv total number of vertices
     * @param b the number of bachelors (just the men)
     */
    
    public FordFulkerson(int[][] g, HashMap<Integer, String> ntp, int tv, int b)
    {
        graph = g;
        numToPeople = ntp;
        totalVertices = tv;
        bachelors = b;
        
        parent = new int[totalVertices];
        visited = new boolean[totalVertices];
        
        resGraph = new int[totalVertices][totalVertices];
        
        for(int i = 1 ; i <= bachelors ; i++)
            resGraph[0][i] = 1;    
        
        for(int i = totalVertices - 2; i >= totalVertices - bachelors - 1 ; i--)
            resGraph[i][totalVertices - 1] = 1;
    }
    
    /**
     * This is a BFS function that attempts to find a path given a certain start
     * and end node within an adjacency graph.
     * 
     * @return true if a path was found, false if not 
     */

    public boolean hasPath()
    {
        int start = 0;
        int sink = totalVertices - 1;
        
        for(int i = start ; i <= sink ; i++)
        {
            parent[i] = -1;
            visited[i] = false;
        }
 
        LinkedList<Integer> queue = new LinkedList<>();
        
        visited[start] = true;
        queue.add(start);
        
        while(!queue.isEmpty())
        { 
            int u = queue.remove();
            
            for(int v = 1 ; v <= sink ; v++)
            {
                if(resGraph[u][v] == 1 && !visited[v])
                {
                    parent[v] = u;
                    visited[v] = true;
                    queue.add(v);
                }
            }
        }
        
        return visited[sink];
    }
    
    /**
     * This function is the main Fulkerson algorithm, that reverses paths once
     * they have been found as well as removing the found path. Because maximum
     * flow is always 1 in bipartite matching for each pair, calculating max flow 
     * is as simple as incrementing the counter for each path found.
     * 
     * This code also creates residual graphs given K, and adds new paths to 
     * the existing residual graph instead of creating a new graph all together.
     * 
     * @param k the k to calculate flow for
     * @return the maximum flow of a given k 
     */
    
    public int fulkerson(int k)
    {   
        for(int i = 1 ; i < bachelors + 1 ; i++)
        {
            for(int j = bachelors + 1 ; j < totalVertices ; j++)
            {
                if((resGraph[i][j] == 0) && (resGraph[j][i] == 0) 
                    && (graph[i][j] > 0 && graph[i][j] <= k) 
                    && (graph[j][i] > 0 && graph[j][i] <= k))
                        resGraph[i][j] = 1;
            }
        }
       
        int maxFlow = 0;
        
        while(hasPath())
        {
            maxFlow++;
            
            for(int v = totalVertices - 1 ; v != 0 ; v = parent[v])
            {
                int u = parent[v];
                resGraph[u][v] -= 1;
                resGraph[v][u] += 1;
            }	
        }
 
        return maxFlow;
    }
    
    /**
     * This function, when called, handles the running of the Fulkerson algorithm
     * and prints the pairs once they have been found using the residual graph
     * left at the end of the operation.
     * 
     * @return the string to print
     */
    
    public String findMax()
    {   
        int maxFlow = 0, k = 0;
        
        do
            maxFlow += fulkerson(++k);
        while(maxFlow < bachelors);
                
        StringBuilder maleMatches = new StringBuilder();
        StringBuilder femaleMatches = new StringBuilder();
        
        maleMatches.append("Everybody matched with top ").append(k).append(" preferences:\n");
        
        for(int i = 1 ; i < bachelors + 1 ; i++)
        {
            for(int j = bachelors + 1 ; j < totalVertices ; j++)
            {
                if(resGraph[j][i] == 1)
                {
                    maleMatches
                        .append(numToPeople.get(i))
                        .append(": matched to ").append(numToPeople.get(j))
                        .append(" (Rank ").append(graph[i][j]).append(")\n");
            
                    femaleMatches
                        .append(numToPeople.get(j))
                        .append(": matched to ").append(numToPeople.get(i))
                        .append(" (Rank ").append(graph[j][i]).append(")\n");
                }
            }
        }
        
        return maleMatches.append(femaleMatches).toString();
    }
}
package maze;

/**
* This is a PQEntry class, mostly used for LinkedList to store variables needed
* to calculate the shortest path of the maze. The class extends Comparable, and
* overrides the compareTo method to be used in computations within LinkedLists.
* 
* @author	Guido Ruiz
* @version	1.0
* @since	2014-24-14
*/  

public class PQEntry implements Comparable<PQEntry>
{
    private int dist;
    private Square sq;

    PQEntry(Square s, int d)
    {
        sq = s;
        dist = d;
    }

    public void setSquare(Square s)
    {
        sq = s;
    }
    
    public void setDistance(int d)
    {
        dist = d;
    }
    
    public Square getSquare()
    {
        return sq;
    }
    
    public int getDistance()
    {
        return dist;
    }
    
    @Override
    public int compareTo(PQEntry other)
    {
        return dist - other.dist;
    }
}

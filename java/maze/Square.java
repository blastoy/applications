package maze;

/**
* This is a square class, which consists of x and y, the position in the 2d
* array where this square will be placed, and up, down, left, right as booleans
* for if there are walls in those directions. This method also stores a
* temporary distance value and the square that was behind this square when
* the shortest path calculation takes place (to print the shortest path).
* 
* @author	Guido Ruiz
* @version	1.0
* @since	2014-24-14
*/  

public class Square
{
    private boolean up, down, left, right;
    private boolean alreadyDone;

    private final int x;
    private final int y;
    private int dist;

    private Square previous;

    Square(int posx, int posy)
    {
        x = posx;
        y = posy;

        up = false;
        down = false;
        left = false;
        right = false;

        alreadyDone = false;
    }

    public boolean hasWall(int dir)
    {
        switch(dir)
        {
            case 1:
                if(up)
                    return true;
                break;
            case 2:
                if(down)
                    return true;
                break;
            case 3:
                if(left)
                    return true;
                break;
            case 4:
                if(right)
                    return true;
                break;
            default:
        }

        return false;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getDistance()
    {
        return dist;
    }

    public boolean getAlreadyDone()
    {
        return alreadyDone;
    }

    public boolean[] getWalls()
    {
        boolean[] result = {up, down, left, right};
        return result;
    }

    public Square getPrevious()
    {
        return previous;
    }

    public void setDistance(int d)
    {
        dist = d;
    }

    public void setAlreadyDone(boolean flag)
    {
        alreadyDone = flag;
    }

    public void setWalls(boolean n, boolean s, boolean w, boolean e)
    {
        up = n;
        down = s;
        left = w;
        right = e;
    }

    public void setPrevious(Square prev)
    {
        previous = prev;
    }

    @Override
    public String toString() 
    {
        String output = "" + x + " " + y + " ";

        if(up)
            output += "N";
        if(down)
            output += "S";
        if(left)
            output += "E";
        if(right)
            output += "W";

        return output;
    }
}
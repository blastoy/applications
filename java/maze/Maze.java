package maze;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
* This class takes a file name as a constructor, and sets up the maze using
* the contents of the file. After the file is created, several methods can be
* used to view detail about the maze.
* 
* Credits for the algorithm to find the shortest path go to Mark Weiss.
* @see computeDistances()
* 
* @author	Guido Ruiz
* @version	1.0
* @since	2014-24-14
*/   

final class Maze
{
    public static final int INFINITY = Integer.MAX_VALUE/3;
    private int rows, cols;
    
    boolean valid;
    
    private Square[][] maze;
    private String fileName;
    
    /**
    * Constructor which opens the file for reading (BufferedReader).
    * 
    * @param s contains the file name of the file provided as a string.
    * @exception FileNotFoundException is thrown if the file is not found.
    */ 
    
    Maze(String s) throws FileNotFoundException
    {
        fileName = s;
        BufferedReader br = new BufferedReader(new FileReader(s));
       
        try
        {
            processFile(br);
        }
        catch(IOException err)
        {
            System.out.println(fileName + ": Unable to read this file (IOException).");
        }
    }
    
    /**
    * This method reads and processes the file to construct the maze. 
    * 
    * @param br the BufferedReader provided by the constructor.
    * @exception IOException if the format of the file is not compatible.
    */ 
    
    public void processFile(BufferedReader br) throws IOException
    {
        String line;
        String[] elems;

        int lineNum = 0;

        if((line = br.readLine()) != null)
        {
            lineNum++;
            elems = line.split(" ");

            if(elems.length != 2)
            {
                System.out.println(fileName + ": Error on line 0: \"" + line + "\" (Too Many Arguments).");
                return;
            }

            try
            {
                rows = Integer.parseInt(elems[0]);
                cols = Integer.parseInt(elems[1]);
            }
            catch(NumberFormatException e)
            {
                System.out.println(fileName + ": Error on line 0: \"" + line + "\" (Invalid Dimensions).");
                return;
            }

            if(rows < 1 || cols < 1)
            {
                System.out.println(fileName + ": Error on line 0: \"" + line + "\" (Minimum Size is 0).");
                return;
            }

            maze = new Square[rows][cols];
            initMaze();
        }

        int i;
        int x = 0;
        int y = 0;
        boolean north, south, west, east;
        boolean skip; //true if the line should be skipped

        String walls;

        while((line = br.readLine()) != null)
        {
            skip = false;
            elems = line.split(" ");
            lineNum++;
            
            if(elems.length != 3)
            {
                skip = true;
                System.out.println(fileName + ": Error on line " + lineNum + ": \"" + line + "\" (Invalid Syntax).");
            }
            else
            {
                try
                {
                    x = Integer.parseInt(elems[0]);
                    y = Integer.parseInt(elems[1]);

                    if(x > rows - 1 || y > cols - 1 || x < 0 || y < 0)
                    {
                        skip = true;
                        System.out.println(fileName + ": Error on line " + lineNum + ": \"" + line + "\" (Out Of Bounds).");
                    }
                }
                catch(NumberFormatException e)
                {
                    System.out.println(fileName + ": Error on line 0: \"" + line + "\" (Invalid Dimensions).");
                
                }
            }

            if(!skip)
            {
                north = south = west = east = false;

                walls = elems[2];

                for(i = 0 ; i < walls.length() ; i++)
                {
                    switch(walls.charAt(i))
                    {
                        case 'N':
                            north = true;
                            break;
                        case 'S':
                            south = true;
                            break;
                        case 'W':
                            west = true;
                            break;
                        case 'E':
                            east = true;
                            break;
                        default:
                            System.out.println(fileName + ": Invalid character \"" + walls.charAt(i) + "\" on line " + lineNum + ": \"" + line + "\" (Skipped).");
                    }
                }

                updateWalls(x, y, north, south, west, east);
            }
        }

        valid = true;    
    }
    
    /**
    * This method instantiates the maze with empty values (no walls), so that
    * the empty entries in the maze can then be updated as values are found
    * for them in the file.
    */ 

    public void initMaze()
    {
        for(int i = 0 ; i < rows ; i++)
            for(int j = 0 ; j < cols ; j++)
                maze[i][j] = new Square(i, j);
    }
    
    /**
    * This method updates a square given a position and new wall booleans.
    * 
    * @param x the x position of the square in the 2d array.
    * @param y the y position of the square in the 2d array.
    * @param n true if there is a wall north of the square.
    * @param s true if there is a wall south of the square.
    * @param w true if there is a wall west of the square.
    * @param e true if there is a wall east of the square.
    */ 
    
    public void updateWalls(int x, int y, boolean n, boolean s, boolean w, boolean e)
    {
        maze[x][y].setWalls(n, s, w, e);
    }
    
    /**
    * This algorithm finds the shortest path from [0][0] to [rows-1][columns-1] 
    * in the maze of type Square[][] 2d array.
    * 
    * @author       Mark Weiss
    * @param penalty is the penalty of going through a wall.
    */ 

    public void computeDistances(int penalty)
    {
        if(!valid)
            return;
        
        Square start = maze[0][0];
        
        for(Square[] s : maze)
        {
            for(Square ss : s)
            {
                ss.setDistance(INFINITY);
                ss.setAlreadyDone(false);
            }
        }

        start.setDistance(0);

        PriorityQueue<PQEntry> pq = new PriorityQueue<>();
        pq.add(new PQEntry(start, 0));

        while(!pq.isEmpty())
        {
            PQEntry e = pq.remove();
            Square v = e.getSquare();

            if(v.getAlreadyDone())
                continue;
      
            v.setAlreadyDone(true);

            for(Square w : getAdjacents(v))
            {
                int cvw = 1;

                if(isWall(v, w))
                    cvw += penalty;

                if(v.getDistance() + cvw < w.getDistance())
                {
                    w.setDistance(v.getDistance() + cvw);
                    pq.add(new PQEntry(w, w.getDistance()));

                    w.setPrevious(v);
                }
            }   
        }	
        
        String result = "";
        int steps = 0;

        Square e = maze[rows - 1][cols - 1];
        Square s;

        LinkedList<Square> list = new LinkedList<>();
        list.add(e);

        while((s = e.getPrevious()) != null)
        {
            steps++;
            list.add(s);
            e = s;	
        }

        Iterator<Square> itr = list.descendingIterator();
        int wallsKnocked = 0;
        
        s = itr.next();
        
        while(itr.hasNext())
        {
            e = itr.next();

            if(isWall(s, e))
                wallsKnocked++;
            
            int[] directions = getDirection(s, e); // Returns direction from S to E [0], and E to S [1].

            switch(directions[0])
            {
                case 1:
                    result += "N";
                    break;
                case 2:
                    result += "S";
                    break;
                case 3:
                    result += "W";
                    break;
                case 4:
                    result += "E";
                    break;
                default:
            }
            
            s = e;
        }

        System.out.println(fileName + ": The shortest path with [" + penalty + " penalty] knocks down [" + wallsKnocked + " walls] and takes [" + steps + " steps].");      
        System.out.println(fileName + ": " + result);
    }
    
    /**
    * This method checks if there is a wall between two squares.
    * 
    * @param v the 'start' square.
    * @param w the 'end' square.
    * @return true if there is a wall, false if not.
    */ 
    
    public boolean isWall(Square v, Square w)
    {
        int[] directions = getDirection(v, w);
        
        return v.hasWall(directions[0]) || w.hasWall(directions[1]);
    }
    
    /**
    * This method tells the direction between two squares, i.e. the direction
    * you must take from one square (UP, DOWN, LEFT, RIGHT) to find the other
    * square in the 2d array.
    * 
    * @param v contains the 'start' square.
    * @param w contains the 'end' square.
    * @return an integer array with the direction v must take to find w as the
    * first element as an integer, and the direction w must take to find v as the second
    * element as an integer. 
    * 
    * In the array: 
    *   1 - North (UP) 
    *   2 - South (DOWN) 
    *   3 - West (LEFT)
    *   4 - East (Right)
    */ 
    
    public int[] getDirection(Square v, Square w)
    {
        int direction_v, direction_w;

        int v_x = v.getX();
        int v_y = v.getY();

        int w_x = w.getX();
        int w_y = w.getY();

        if(v_y == w_y) // either north or south
        {
            if(w_x < v_x) 
            {
                direction_v = 1; // north
                direction_w = 2;
            }
            else
            {
                direction_v = 2;  // south
                direction_w = 1;
            }
        }
        else // either west or east
        {
            if(w_y < v_y) 
            {
                direction_v = 3; // west
                direction_w = 4;
            }
            else
            {	
                direction_v = 4; // east
                direction_w = 3;
            }
        }

        int[] result = {direction_v, direction_w};

        return result; 
    }
    
    public Square[][] getMaze()
    {
        return maze;
    }
    
    /**
    * This method returns all squares that are adjacent to a given square, also
    * considering boundaries of the maze so that no result is out of bounds.
    * 
    * @param s is the square of which to find the adjacent squares to.
    * @return a list of squares that are adjacent to 's'.
    */ 
    
    public List<Square> getAdjacents(Square s)
    {
        List<Square> adj = new ArrayList<>();

        int s_x = s.getX();
        int s_y = s.getY();

        if(s_x > 0 && s_y > 0 && s_x < rows - 1 && s_y < cols - 1) // Anywhere inside the boundaries
        {
            adj.add(maze[s_x - 1][s_y]);
            adj.add(maze[s_x + 1][s_y]);
            adj.add(maze[s_x][s_y - 1]);
            adj.add(maze[s_x][s_y + 1]);
        }
        else if(s_x == 0 && s_y == 0) // Top-Left Corner
        {
            adj.add(maze[0][1]);
            adj.add(maze[1][0]);
        }
        else if(s_x == rows - 1 && s_y == cols - 1) // Bottom-Right Corner
        {
            adj.add(maze[rows - 1][cols - 2]);
            adj.add(maze[rows - 2][cols - 1]);
        }
        else if(s_x == rows - 1 && s_y == 0) // Bottom-Left Corner
        {
            adj.add(maze[rows - 1][1]);
            adj.add(maze[rows - 2][0]);
        }
        else if(s_x == 0 && s_y == cols - 1) //Top-Right Corner
        {
            adj.add(maze[0][cols - 2]);
            adj.add(maze[1][cols - 1]);
        }
        else if(s_x == 0)
        {
            adj.add(maze[s_x][s_y + 1]);
            adj.add(maze[s_x][s_y - 1]);
            adj.add(maze[s_x + 1][s_y]);
        }
        else if(s_x == rows - 1)
        {
            adj.add(maze[s_x][s_y + 1]);
            adj.add(maze[s_x][s_y - 1]);
            adj.add(maze[s_x - 1][s_y]);
        }
        else if(s_y == 0)
        {
            adj.add(maze[s_x + 1][s_y]);
            adj.add(maze[s_x - 1][s_y]);
            adj.add(maze[s_x][s_y + 1]);
        }
        else if(s_y == cols - 1)
        {
            adj.add(maze[s_x + 1][s_y]);
            adj.add(maze[s_x - 1][s_y]);
            adj.add(maze[s_x][s_y - 1]);
        }
        return adj;
    }	

    @Override
    public String toString()
    {
        String s = "";
        
        for(int i = 0 ; i < rows ; i++)
            for(int j = 0 ; j < cols ; j++)
                s += maze[i][j] + "\n";
        
        return s;
    }
}
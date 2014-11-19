package stablematching;

import java.util.ArrayList;

public class Person 
{
    protected String name;
    protected ArrayList<String> preferences;
    protected String myDate = null;
    protected static int k = 1;
    
    Person(String n, ArrayList<String> p)
    {
        name = n;
        preferences = p;
    }
 
    public String getMyDate()
    {
        return myDate;
    }
    
    public String getName()
    {
        return name;
    }
    
    public ArrayList<String> getPrefferences()
    {
        return preferences;
    }
    
    public int getK()
    {
        return k;
    }
}
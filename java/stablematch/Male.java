package stablematching;

import java.util.ArrayList;

public class Male extends Person
{
    private int tries = 0;
    
    Male(String n, ArrayList<String> p)
    {
        super(n, p);
    }
    
    public void setDate(String date)
    {
        myDate = date;
    }
    
    public String getPreffered()
    {
        String result = preferences.get(tries++);
        
        if(tries > k)
            k = tries;
        
        return(result);
    }
    
    public int getTries()
    {
        return tries;
    }
}

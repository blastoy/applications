package sieveoferatosthenes;

public class SieveOfEratosthenes
{
    /**
     * This function is an implementation of Sieve Of Eratosthenes algorithm
     * which finds all primes from 2 to n. The n must be 2 or greater. The 
     * algorithm does this by assuming all numbers from 2 to n are prime(true), 
     * and picking a number starting at 2 to n and making all multiples of 2, 
     * i.e: 4, 6, 8, 10, ..., (final number less than n) as not prime(false). 
     * After 2, the algorithm picks up the next non-false number, or 3, and does
     * the same. When the algorithm reaches 4, it is skipped, because it is 
     * marked as prime by 2 and goes directly to 5 which was not marked as false. 
     * by 2 or 3. This continues all the way to sqrt(n) (10 for n = 100), in 
     * which then whatever is left as true is a prime number.
     * 
     * @param n high bound to find the primes of.
     * @return a boolean array with array[i] true if 'i' is a prime number.
     */
    
    public static boolean[] getPrimes(int n)
    {
        if(n < 2) // n must be 2 or greater
            return null;
        
        boolean[] result = new boolean[n + 2];
        
        for(int k = 2 ; k <= n ; k++) // initialize all elements in array to true
            result[k] = true;
    
        for(int i = 2 ; i <= Math.sqrt(n) ; i++)
            if(result[i]) // this 'if' within the 'for' makes the 'for' traverse i as: 2, 3, 5, 7... to sqrt(n)
                for(int j = i * i ; j <= n ; j += i) // e.g. for 2: 4, 8, 10, ... are not prime
                    result[j] = false;
   
        return result;   
    }
    
    public static void main(String[] args)
    {
        //int n = Integer.parseInt(args[0]);
        int n = 100;
        
        boolean[] ans = getPrimes(n);
        
        System.out.println("The prime numbers are:");
        
        for(int i = 2 ; i <= n ; i++)
            if(ans[i])
                System.out.println(i + " ");
    }   
}

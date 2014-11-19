package dynamicprogramming;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Dynamic Programming is a controlled brute force method of solving problems where
 * greedy algorithms do not work, and where recursion, if not done carefully, can
 * have a large complexity. When overlapping subproblems occur in recursion, dynamic
 * programming stores already calculated values in a table and are used whenever
 * needed.
 * 
 * @author Guido
 */

public class Test
{
    /**
     * This function finds the smallest of three different numbers. It is used
     * to find the largest square submatrix.
     * 
     * @param a int
     * @param b int
     * @param c int
     * @return smallest of a, b, and c
     */
    public static int findMin3(int a, int b, int c)
    {
        int r = Math.min(a, b);
        return Math.min(r, c);
    }

    /**
     * Finds the largest square submatrix in a given matrix of 0s and 1s.
     * 
     * @param s the name of the file to open
     * @throws IOException 
     */
    
    @SuppressWarnings({"empty-statement", "ManualArrayToCollectionCopy", "ConvertToTryWithResources"})
    public static void largestSquareSubmatrix(String s) throws IOException
    {
        // get the number of lines in the file (rows)
        LineNumberReader reader = new LineNumberReader(new FileReader(s));
        while((reader.readLine()) != null);
        int size_r = reader.getLineNumber();
        reader.close();

        // get the number of chars in a line (cols)
        BufferedReader br = new BufferedReader(new FileReader(s));
        String line = br.readLine();
        line = line.trim();
        int size_c = line.length();

        // declare the matrix
        int[][] matrix = new int[size_r][size_c];

        for(int i = 0 ; i < size_c ; i++)
            matrix[0][i] = line.charAt(i) - '0';

        int row = 1;

        // put the file into the matrix
        while((line = br.readLine()) != null)
        {
            for(int i = 0 ; i < size_c ; i++)
                matrix[row][i] = line.charAt(i) - '0';

            row++;
        }

        int[][] result = new int[size_r][size_c];
        
        // fill in the first row and column of the result
        for(int i = 0 ; i < size_r ; i++)
            result[i][0] = matrix[i][0];
        
        for(int i = 0 ; i < size_c ; i++)
            result[0][i] = matrix[0][i];
        
        // if 1, minimum of top, left, right + 1
        for(int i = 1 ; i < size_r ; i++)
            for(int j = 1 ; j < size_c ; j++)
                if(matrix[i][j] == 1)
                    result[i][j] = findMin3(result[i][j - 1], result[i - 1][j], result[i - 1][j - 1]) + 1;
          
        int max = result[0][0], max_r = 0, max_c = 0;
        
        // find the largest number in the result
        for(int i = 0 ; i < size_r ; i++)
        {
            for(int j = 0 ; j < size_c ; j++)
            {
                if(max < result[i][j])
                {
                    max = result[i][j];
                    max_r = i;
                    max_c = j;
                }
            }
        }
        
        // print the result matrix
        for(int i = max_r ; i > max_r - max ; i--)
        {
            for(int j = max_c ; j > max_c - max ; j--)
                System.out.print(matrix[i][j] + " ");
            
            System.out.println("");
        }
    }

    public static void longestCommonSubsequence(String s) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(s));
        
        String first = br.readLine();
        String second = br.readLine();
        
        int fl = first.length();
        int sl = second.length();
          
        int[][] table = new int[fl + 1][sl + 1];
        
        for(int i = 1 ; i < fl + 1 ; i++) // create the table
        {
            for(int j = 1 ; j < sl + 1 ; j++)
            {
                if(first.charAt(i - 1) == second.charAt(j - 1)) // diagonal if same + 1
                    table[i][j] = table[i - 1][j - 1] + 1;
                else
                    table[i][j] = Math.max(table[i - 1][j], table[i][j - 1]); // max of up and left if not
            }
        }
        
        int index = table[fl][sl];
        char[] lcs = new char[Math.max(fl, sl)];
        int i = fl, j = sl;

        for(;;) // traverse the table and print the result
        {
            if(i == 0 || j == 0)
                break;
            
            if(first.charAt(i - 1) == second.charAt(j - 1))
            {
                lcs[index - 1] = first.charAt(i - 1);
                i--;
                j--;
                index--; 
            }

            else if(table[i - 1][j] > table[i][j - 1])
                i--;
            else
                j--;
        }

        for(char c : lcs)
           System.out.print(c);

        System.out.println("");
    } 
    
    public static void coinChange(int[] set, int n)
    {
        int[] table = new int[n + 1]; // dynamically store previous calculated values
        
        for(int i = 1 ; i < n + 1 ; i++)
        {
            int min = i; // assume minimum is 1, 1, 1, ... (n times)
            
            for(int j = 0 ; j < set.length ; j++) // for all coins in the set
                if(set[j] <= i) // coin has to be smaller or equal to the change we want
                    min = Math.min(min, (table[i - set[j]] + 1)); // assumed value, or table minimum
            
            table[i] = min;
            System.out.println(i + "\t" + min);
        }
    }
    
    public static void main(String[] args)
    {
        try
        {
            System.out.println("1. Largest Square Submatrix:");
            largestSquareSubmatrix(args[0]);
            
            System.out.println("-----\n\n2. Longest Common Subsequence:");
            longestCommonSubsequence(args[1]);
            
            int[] coinSet = {1, 5, 10, 18, 25};
            int n = 100;
            
            System.out.println("-----\n\n3. Coin Change:\nN\tMin");
            coinChange(coinSet, n);
        }
        catch(IOException e)
        {
            System.out.println("Error!");
        }
    }

}

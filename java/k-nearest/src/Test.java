import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;

public class Test {	
	private static BufferedReader br;
	private static final double INFINITY = Double.MAX_VALUE;
	
	public static String[] loadFile(String file_path) {
		// Stores each line of the file
		ArrayList<String> al = new ArrayList<>();
		
		try {
			// Open the file
			br = new BufferedReader(new FileReader(file_path));
			
			String line;
			
			// Add each line to an ArrayList
			while((line = br.readLine()) != null) al.add(line);
			
			//Return an Array
			return al.toArray(new String[al.size()]);
		} catch(IOException e) {
			System.out.println("ERROR: " + e);
		}
		
		return null;
	}
	
	public static Candidate findPopular(Candidate[] elems) {
		int[] frequencies = new int[MAX_DIGIT_LABEL]; // stores frequencies
		
		int elems_len = elems.length; // amt of candidates
		
		for(int i = 0 ; i < elems_len ; i++) // increment per occurence of candidate
		    frequencies[(int)elems[i].getLabel()]++; 
	
		int max_freq = 0;
		int max_index = -1;
		
		for(int i = 0 ; i < elems_len ; i++) { // grab most frequent candidate
			int f = frequencies[(int)elems[i].getLabel()];
			if(max_freq < f || max_index == -1) { // also handles ties
				max_freq = f;
				max_index = i;
			}
		}
		
		return elems[max_index]; // return most frequent candidate
	}
	
	public static boolean useK(Candidate[] candidates, String test_line) {
		String[] test_elems = test_line.split(" "); // get answer for this line
		int answer = Integer.parseInt(test_elems[0]);
		
		if(LOG_EVERYTHING) System.out.print("Answer: " + answer);
		int classification = (int)findPopular(candidates).getLabel(); // find popular out of all Ks
		if(LOG_EVERYTHING) System.out.print(", classified as: " + classification);
		
		return classification == answer; // return correct or incorrect
	}
	
	public static Candidate[] findK(String[] train_arr, int train_arr_len, String test_line, int k) {	
		if(k < 1) return null; // only positive k's allowed
		
		String[] train_elems, test_elems = test_line.split(" "); // arrays to store each indiv num of each line
		
		int array_min_len = (train_arr_len == 0) ? train_arr.length : train_arr_len; // how many lines of training data we have (max or specified)
		int elems_min_len = test_elems.length - 1; // how many difference calculations we'll do per line
		
		Candidate k1 = new Candidate(0, INFINITY); // stores k1 for easy access. Distance init to INFINITY
		Candidate[] distances = new Candidate[array_min_len]; // stores all other values of k (unsorted) including k1
		
		for(int i = 0 ; i < array_min_len ; i++) { // for every line in training data
			train_elems = train_arr[i].split(" "); // split line into individual numbers
			
			distances[i] = new Candidate(Double.parseDouble(train_elems[0])); // initiate new class that stores distance and class
			
			for(int j = 1 ; j < elems_min_len ; j++) // add abs(train - test) to distance for each element
				distances[i].addDistance(Math.abs(Double.parseDouble(train_elems[j]) - Double.parseDouble(test_elems[j])));
			
			if(distances[i].getDistance() < k1.getDistance()) { // replace k1 if this distance is shorter (again, for easy access)
				k1.setDistance(distances[i].getDistance());  
				k1.setLabel(distances[i].getLabel());
			}
		}
		
		if(k == 1) return new Candidate[] { k1 }; // skip sorting step if k = 1 (return saved k1)
		else if(k > 1 && k < elems_min_len) {
			Arrays.sort(distances); // sort array
			return Arrays.copyOfRange(distances, 0, k); // return array splice between 0 to k
		}
		
		return null; // k was a number out of reach!
	}
	
	public static void KNearestNeighbor(String[] train, String[] test, int K) {
		int correct = 0, i;
		
		int train_arr_len = (TRAIN_SIZE == 0) ? train.length : TRAIN_SIZE;
		int test_arr_len = (TEST_SIZE == 0) ? test.length : TEST_SIZE;
		
		for(i = 0 ; i < test_arr_len ; i++) {
			if(LOG_EVERYTHING) System.out.print("[" + i + "]:\t");
			
			// Find K-nearest neighbors for test data. Returns null if K or input format is incorrect!
			Candidate[] candidates = findK(train, train_arr_len, test[i], K);
			
			if(candidates == null) 
				System.out.println("ERROR: Could not calculate K-nearest Neighbors! Wrong data type? K?");
			else {
				if(useK(candidates, test[i])) 
				{ 
					if(LOG_EVERYTHING) System.out.println("\t--> Correct! :) <--"); 
					correct++;
				} else if(LOG_EVERYTHING) System.out.println("\t--> Incorrect! :( <--");
			}
		}	
		
		System.out.println("-- > K: " + K + ", Accuracy: " + (double)correct / i + " <--");
	}
	
	// -- SETTINGS -- //
	private static int TEST_SIZE = 0; // chunk of test file desired (0 max)
	private static int TRAIN_SIZE = 1000; // chunk of training file desired (0 max)
	private static int MAX_DIGIT_LABEL = 10; // 0 - 9 are the labels
	private static boolean LOG_EVERYTHING = false; // 1 - true, 0 - false
	// -------------- //
	
	public static void main(String[] args) {	
		// Load an image (could be NULL if DNE)
		String[] train = loadFile("zip.train");
		String[] test = loadFile("zip.test");
		
		if(train == null || test == null)
			System.out.println("ERROR: Could not load data!");
		else {
			for(int i = 1 ; i < 26 ; i++)
				KNearestNeighbor(train, test, i);
		}
	}	 
}
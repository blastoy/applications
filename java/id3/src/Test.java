import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Test {
	private static BufferedReader br;
	
	public static ArrayList<ArrayList<String>> loadFile(String file_path) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		try {
			br = new BufferedReader(new FileReader(file_path));
			String line;
			int i = 0;
			
			while((line = br.readLine()) != null) {
				result.add(new ArrayList<String>());
				
				String[] split = line.split(",");
				
				for(String s : split) {
					result.get(i).add(s);
				}
				i++;
			}
			
			return result;
		} catch(IOException e) {
			System.out.println("ERROR: " + e);
		}
		
		return null;
	}
	
	public static double getEntropy(int positive, int negative) {
		if(positive == 0 || negative == 0) return 0.0;
		
		double total = positive + negative;
		double entropy_pos = (positive / total) * ((Math.log(positive / total)) / Math.log(2));
		double entropy_neg = (negative / total) * ((Math.log(negative / total)) / Math.log(2));
		
		return -entropy_pos - entropy_neg;
	}
	
	public static double getInfoGain(double main_ent, int left_pos, int left_neg, int right_pos, int right_neg,  int train_len) {
		double left_ent = getEntropy(left_pos, left_neg) * (((double)left_pos + left_neg) / train_len);
		double right_ent = getEntropy(right_pos, right_neg) * (((double)right_pos + right_neg) / train_len);
		
		return main_ent - (left_ent + right_ent);
	}
	
	public static Node ID3(ArrayList<ArrayList<String>> train, ArrayList<String> targets, ArrayList<Integer> attributes) {
		Node root = new Node();
		NUM_NODES++;
		
		int train_len = train.size();
		
		root.setSize(train_len);
		
		if(train_len < STOPPING_POINT) {
			int pos = 0, neg = 0;
			
			for(String t : targets) {
				if(t.equals("1")) pos++; else neg++; 
			}
			
			int max = (pos > neg) ? Integer.MAX_VALUE : -Integer.MAX_VALUE;
			
			return new Node(max);
		}
		
		int main_pos = 0; // to calc entropy of root node
		int main_neg = 0; // to calc entropy of root node
		
		for(String t : targets) {
			if(t.equals("1")) main_pos++; else main_neg++;
		}
		
		if(main_neg == 0) return new Node(Integer.MAX_VALUE);
		if(main_pos == 0) return new Node(-Integer.MAX_VALUE);
		
		if(attributes.isEmpty()) {
			int max = (main_pos > main_neg) ? Integer.MAX_VALUE : -Integer.MAX_VALUE;
			return new Node(max);
		}
		
		double main_ent = getEntropy(main_pos, main_neg);
	
		int best_attr = -1;
		double highest_info_gain = -1.0;
		double curr_gain;
		
		for(int i : attributes) {
			int left_pos = 0, left_neg = 0, right_pos = 0, right_neg = 0;

			for(int j = 0 ; j < train_len ; j++) {
				if(train.get(j).get(i).equals("0")) {
					if(targets.get(j).equals("1")) left_pos++; else left_neg++;
				} else {
					if(targets.get(j).equals("1")) right_pos++; else right_neg++;
				}
			}
					
			if((curr_gain = getInfoGain(main_ent, left_pos, left_neg, right_pos, right_neg, train_len)) > highest_info_gain) {
				highest_info_gain = curr_gain;
				best_attr = i;
			}
		}
		
		root.setValue(best_attr);
		
		ArrayList<ArrayList<String>> train_left = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> train_right = new ArrayList<ArrayList<String>>();
		
		ArrayList<String> targets_left = new ArrayList<String>();
		ArrayList<String> targets_right = new ArrayList<String>();
	
		int num_pos = 0, num_neg = 0;
		
		for(int i = 0 ; i < train_len ; i++) {
			ArrayList<String> al = train.get(i);
			
			if(al.get(best_attr).equals("0")) {
				train_left.add(al);
				targets_left.add(targets.get(i));
				if(targets.get(i).equals("1")) num_pos++; else num_neg++; 
			} else {
				train_right.add(al);
				targets_right.add(targets.get(i));
				if(targets.get(i).equals("1")) num_pos++; else num_neg++;
			}
		}
		
		if(train_len < MIN_DATA) {
			MIN_DATA = train_len;
		}
		
		int max = (num_pos > num_neg)? Integer.MAX_VALUE : -Integer.MAX_VALUE;
		
		attributes.remove(attributes.indexOf(best_attr));
		
		ArrayList<Integer> attr_left = new ArrayList<>();
		ArrayList<Integer> attr_right = new ArrayList<>();
		
		for(int i : attributes) {
			attr_left.add(i);
			attr_right.add(i);
		}
		
		if(train_left.isEmpty()) {
			root.setLeftChild(new Node(max));
		} else {
			root.setLeftChild(ID3(train_left, targets_left, attr_left));
		}
		
		if(train_right.isEmpty()) {
			root.setRightChild(new Node(max));
		} else {
			root.setRightChild(ID3(train_right, targets_right, attr_right));
		}	
		
		return root;
	}
	
	public static int STOPPING_POINT;
	public static int NUM_NODES;
	public static int MIN_DATA;
	
	public static void main(String[] args) {	
		for(int k = 5 ; k < 3210 ; k = k + 2) {
			STOPPING_POINT = k;
			NUM_NODES = 0;
			MIN_DATA = Integer.MAX_VALUE;
			
			ArrayList<ArrayList<String>> train = loadFile("adult_b.dat");
			ArrayList<String> targets = new ArrayList<String>();
		
			for(ArrayList<String> t : train) {
				targets.add(t.get(0));
				t.remove(0);
			}
			
			ArrayList<Integer> attributes = new ArrayList<Integer>();
	
			int len = train.get(0).size();	
			
			for(int i = 0 ; i < len ; i++) {
				attributes.add(i);
			}
			
			Node tree = ID3(train, targets, attributes);
			
			ArrayList<ArrayList<String>> test = loadFile("adult_a.dat");
			
			int counter = 0, total = 0;
			
			String calc_answer, real_answer;
			
			
			System.out.println("lol");
			
			/*
			for(ArrayList<String> t : test) {
				real_answer = t.get(0);
				t.remove(0);
				
				Node result = tree;
				
				while(true) {
					int curr = result.getValue();
					
					if(curr == Integer.MAX_VALUE) { 
						calc_answer = "1";
						break;
					} else if (curr == -Integer.MAX_VALUE) {
						calc_answer = "-1";
						break;
					} else {
						String selection = t.get(Integer.valueOf(result.getValue()));
						if(selection.equals("0")) {
							result = result.getLeftChild();
						} else {
							result = result.getRightChild();
						}
					}
				}
				
				if(!calc_answer.equals(real_answer)) counter++; 
				total++;
			}
			*/
			//System.out.println(NUM_NODES + "\t" + MIN_DATA + "\t" + (double)counter/total);
		}	
	}
}
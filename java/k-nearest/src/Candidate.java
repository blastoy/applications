public class Candidate implements Comparable<Candidate> {
	
	private double label;
	private double distance;
	
	public Candidate() {
		label = 0;
		distance = 0;
	}
	
	public Candidate(double num) {
		label = num;
		distance = 0;
	}
	
	public Candidate(double num, double dist) {
		label = num;
		distance = dist;
	}
	
	public void addDistance(double dist) { distance += dist; }

	public double getLabel() { return label; }
	public void setLabel(double num) { label = num; }

	public double getDistance() { return distance; }
	public void setDistance(double dist) { distance = dist; }
	
	@Override
	public String toString() {
		return "Distance Calculated: [ " + distance + " ]. Label: [ " + label + " ]";
	}
	
	@Override
	public int compareTo(Candidate other) {
		if(this.distance > other.distance) return 1;
		else if(this.distance < other.distance) return -1;
		return 0;
	}
}

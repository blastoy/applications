public class Node {
    private Node left_child = null;
    private Node right_child = null;
    private int value = 0;
    private int size = 0;
    
    public Node() { }
    
    public Node(int value) {
        this.setValue(value);
    }
    
    public void setLeftChild(Node child) {
        left_child = child;
    }
    
    public void setRightChild(Node child) {
        right_child = child;
    }
    
    public Node getLeftChild() {
    	return left_child;
    }
    
    public Node getRightChild() {
    	return right_child;
    }
    
	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String toString() {
		return "My value is: " + value + ", and my size is: " + size;
	}
}
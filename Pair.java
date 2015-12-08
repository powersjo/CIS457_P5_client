public class Pair {
	private Integer key;
 	private byte[] val;

	public Pair(Integer key, byte[] val) {
    	this.key = key;
    	this.val = val;
  	}

  	public Integer getKey() { return key; }
 	public byte[] getValue() { return val; }
}

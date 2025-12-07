package chess.botterBot;

public class LookUpTable {
	private Bucket[] content;
	private class Bucket{
		public long value;
		public Bucket next;
		public Bucket(long value) {
			this.value = value;
		}
	}
	private int elements;
	public static final double threshold = 0.7;
	private int mask;
	public LookUpTable(int initialcapacity) {
		int i = 1;
		while(initialcapacity != 0) {
			i++;
			initialcapacity = initialcapacity >>> 1;
		}
		mask = (2 << i)-1;
		content = new Bucket[i];
	}
	public boolean put(LookUpKey key, long value) {
		return false;
	}
}

class LookUpKey{
	public final byte square;
	public final long blockers;
	public LookUpKey(byte square, long blockers) {
		this.square = square;
		this.blockers = blockers;
	}
	@Override
	public int hashCode() {
		return Long.hashCode(blockers)^Byte.hashCode(square);
	}
}
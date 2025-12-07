package chess.botterBot;

public class PRNG {
	private long number;
	static final long add = (1l << 61) - 339l; //also a prime
	static final long mult = 0l;
	static final long xor = (1l << 62) - 153l; //2^62 -153 is a prime
	public PRNG(long seed) {
		this.number = seed;
	}
	public long getNumber() {
		return number;
	}
	public long next() {
		number = (number * add) ^ xor;
		return number;
	}
	public long nextPositive() {
		long next;
		while((next = next()) < 0);
		return next;
	}
}

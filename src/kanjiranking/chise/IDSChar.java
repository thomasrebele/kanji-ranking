package kanjiranking.chise;

public class IDSChar {
	char c = 0;
	int argCount = 0;
	public IDSChar(char c, int argCount) {
		this.c = c;
		this.argCount = argCount;
	}
	public String toString() {
		return c + ":" + argCount;
	}
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof IDSChar)) return false;
		return this.c == ((IDSChar) o).c;
	}
	public int hashCode() {
		return c;
	}
}
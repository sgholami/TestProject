package misc;

public class StringPair extends Pair<String,String> {

	public StringPair (String key, String value) {
		super(key, value);
	}
	
	public String key () {
		return this.key;
	}
	
	public String value () {
		return this.value;
	}

}

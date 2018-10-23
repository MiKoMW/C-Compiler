package sem;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable = new HashMap<>();
	
	public Scope(Scope outer) { 
		this.outer = outer; 
	}
	
	public Scope() { this(null); }
	
	public Symbol lookup(String name) {
		Symbol symbol;

		symbol = lookupCurrent(name);
		if(symbol != null){
			return symbol;
		}

		// may need change.
		if(outer == null){
			return null;
		}

		return outer.lookup(name);

		// To be completed...
	}
	
	public Symbol lookupCurrent(String name) {
		// To be completed...

		Symbol symbol = this.symbolTable.getOrDefault(name,null);

		return symbol;
	}
	
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}

	public void put(String string,Symbol symbol){
		symbolTable.put(string, symbol);

	}
}

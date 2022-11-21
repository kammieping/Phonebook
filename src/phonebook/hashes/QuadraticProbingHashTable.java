package phonebook.hashes;

import java.util.ArrayList;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link QuadraticProbingHashTable} is an Openly Addressed {@link HashTable} which uses <b>Quadratic
 * Probing</b> as its collision resolution strategy. Quadratic Probing differs from <b>Linear</b> Probing
 * in that collisions are resolved by taking &quot; jumps &quot; on the hash table, the length of which
 * determined by an increasing polynomial factor. For example, during a key insertion which generates
 * several collisions, the first collision will be resolved by moving 1^2 + 1 = 2 positions over from
 * the originally hashed address (like Linear Probing), the second one will be resolved by moving
 * 2^2 + 2= 6 positions over from our hashed address, the third one by moving 3^2 + 3 = 12 positions over, etc.
 * </p>
 *
 * <p>By using this collision resolution technique, {@link QuadraticProbingHashTable} aims to get rid of the
 * &quot;key clustering &quot; problem that {@link LinearProbingHashTable} suffers from. Leaving more
 * space in between memory probes allows other keys to be inserted without many collisions. The tradeoff
 * is that, in doing so, {@link QuadraticProbingHashTable} sacrifices <em>cache locality</em>.</p>
 *
 * @author Kammie Ping
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see LinearProbingHashTable
 * @see CollisionResolver
 */
public class QuadraticProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/
	private int countWTomb;
	
    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */

    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *               we want soft deletion, {@code false} otherwise.
     */
    public QuadraticProbingHashTable(boolean soft) {
        softFlag = soft;
        count = 0;
        countWTomb = 0;
        primeGenerator = new PrimeGenerator();
        table = new KVPair[primeGenerator.getCurrPrime()];
        
        for(int i = 0; i < table.length; i++) {
        	table[i] = null;
        }
    }

    @Override
    public Probes put(String key, String value) {
        if(key == null || value == null) {
        	throw new IllegalArgumentException("Key/value cannot be null!");
        }
        int probes = 0;
    	if(countWTomb > (capacity()/2)) {
        	probes += resize();
        }
        int hash = hash(key);
        int conHash = hash;
        int i = 1;
        if(table[hash] != null) {
	        while(table[hash] != null) {
	        	probes++;
	        	hash = (conHash + (i-1) + ((i-1)*(i-1)))%capacity();
	        	i++;
	        }
	        probes--;
        }
        probes++;
        table[hash] = new KVPair(key, value);
        count++;
        countWTomb++;
        
        return new Probes(value, probes);
    }
    
    private int resize() {
    	ArrayList<KVPair> newKeysVals = new ArrayList<KVPair>();
    	int probes = 0;
    	for(int i = 0; i < capacity(); i++) {
    		if(table[i] != null && !(table[i].equals(TOMBSTONE))) {
    			newKeysVals.add(table[i]);
    		}
    		probes++;
    	}
    	
    	count = 0;
    	countWTomb = 0;
    	table = new KVPair[primeGenerator.getNextPrime()];
    	for(int i = 0; i < table.length; i++) {
        	table[i] = null;
        }
    	for(int i = 0; i < newKeysVals.size(); i++) {
    		probes += (put(newKeysVals.get(i).getKey(), newKeysVals.get(i).getValue())).getProbes();
    	}
    	return probes;
    }


    @Override
    public Probes get(String key) {
    	if(key == null) {
    		return new Probes(null, 0);
    	}
        int probes = 1;
        int hash = hash(key);

        if(table[hash] == null) {
        	return new Probes(null, probes);
        }
        
        int conHash = hash;
        int i = 1;
        hash = (conHash + (i-1) + (i-1)*(i-1))%capacity();
    	i++;
    	
        while(!(table[hash].getKey().equals(key))) {
        	hash = (conHash + (i-1) + (i-1)*(i-1))%capacity();
        	i++;
        	probes++;
        	if(table[hash] == null) {
        		return new Probes(null, probes);
        	}
        }
        return new Probes(table[hash].getValue(), probes);
    }

    @Override
    public Probes remove(String key) {
    	if(key == null) {
        	return new Probes(null, 0);
        }
        int probes = 1;
        int hash = hash(key);
        int conHash = hash;
        int i = 1;
        hash = (conHash + (i-1) + (i-1)*(i-1))%capacity();
    	i++;
        if(!containsKey(key)) {
	        while(table[hash] != null) {
	        	hash = (conHash + (i-1) + (i-1)*(i-1))%capacity();
	        	i++;
	        	probes++;
	        }
	        return new Probes(null, probes);
        }
        
        //soft deletion	
        if(softFlag == true) {
        	while(!(table[hash].getKey().equals(key))) {
        		probes++;
        		hash = (conHash + (i-1) + (i-1)*(i-1))%capacity();
            	i++;
        	}
        	String value = table[hash].getValue();
        	count--;
        	table[hash] = TOMBSTONE;
        	return new Probes(value, probes);
        }
        //hard deletion
        String value = table[hash].getValue();
        ArrayList<KVPair> moving = new ArrayList<KVPair>();
        i = 1;
        while(table[hash] != null && !(table[hash].getKey().equals(key))){
        	probes++;
        	hash = (conHash + (i-1) + (i-1)*(i-1))%capacity();
        	i++;
        }
        
        table[hash] = null;
        //probes++;
        countWTomb--;
        count--;
        
        for(int j = 0; j < table.length; j++) {
        	if(table[j] != null) {
        		moving.add(table[j]);
        		count--;
            	countWTomb--;
            	table[j] = null;
            	probes+= put(moving.get(moving.size()-1).getKey(), moving.get(moving.size()-1).getValue()).getProbes();
        	}
        	probes++;
        }
        return new Probes(value, probes);
    }


    @Override
    public boolean containsKey(String key) {
    	int hash = hash(key);
        if(table[hash] == null) {
        	return false;
        }
        int i = 1;
        int conHash = hash;
        while(!(table[hash].getKey().equals(key))) {
        	hash = (conHash + (i-1) + ((i-1)*(i-1)))%capacity();
        	i++;
        	if(table[hash] == null) {
        		return false;
        	}
        }
        return true;
    }

    @Override
    public boolean containsValue(String value) {
    	for(int i = 0; i < table.length; i++) {
        	if(table[i].getValue().equals(value)) {
        		return true;
        	}
        }
        return false;
    }
    @Override
    public int size(){
        return count;
    }

    @Override
    public int capacity() {
        return table.length;
    }

}
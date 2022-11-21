package phonebook.hashes;

import java.util.ArrayList;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPair;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**
 * <p>{@link LinearProbingHashTable} is an Openly Addressed {@link HashTable} implemented with <b>Linear Probing</b> as its
 * collision resolution strategy: every key collision is resolved by moving one address over. It is
 * the most famous collision resolution strategy, praised for its simplicity, theoretical properties
 * and cache locality. It <b>does</b>, however, suffer from the &quot; clustering &quot; problem:
 * collision resolutions tend to cluster collision chains locally, making it hard for new keys to be
 * inserted without collisions. {@link QuadraticProbingHashTable} is a {@link HashTable} that
 * tries to avoid this problem, albeit sacrificing cache locality.</p>
 *
 * @author Kammie Ping
 *
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see OrderedLinearProbingHashTable
 * @see QuadraticProbingHashTable
 * @see CollisionResolver
 */
public class LinearProbingHashTable extends OpenAddressingHashTable {

    /* ********************************************************************/
    /* ** INSERT ANY PRIVATE METHODS OR FIELDS YOU WANT TO USE HERE: ******/
    /* ********************************************************************/
	//private Boolean del;
	//private PrimeGenerator primeGenerator;
	//private KVPair[] table;
	//private int count;
	private int countWTomb;
	private ArrayList<KVPair> keysVals;
	
    /* ******************************************/
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS: */
    /* **************************************** */

    /**
     * Constructor with soft deletion option. Initializes the internal storage with a size equal to the starting value of  {@link PrimeGenerator}.
     *
     * @param soft A boolean indicator of whether we want to use soft deletion or not. {@code true} if and only if
     *             we want soft deletion, {@code false} otherwise.
     */
    public LinearProbingHashTable(boolean soft) {
        softFlag = soft;
        count = 0;
        countWTomb = 0;
        keysVals = new ArrayList<KVPair>();
        primeGenerator = new PrimeGenerator();
        table = new KVPair[primeGenerator.getCurrPrime()];
        
        for(int i = 0; i < table.length; i++) {
        	table[i] = null;
        }
    }

    /**
     * Inserts the pair &lt;key, value&gt; into this. The container should <b>not</b> allow for {@code null}
     * keys and values, and we <b>will</b> test if you are throwing a {@link IllegalArgumentException} from your code
     * if this method is given {@code null} arguments! It is important that we establish that no {@code null} entries
     * can exist in our database because the semantics of {@link #get(String)} and {@link #remove(String)} are that they
     * return {@code null} if, and only if, their key parameter is {@code null}. This method is expected to run in <em>amortized
     * constant time</em>.
     * <p>
     * Instances of {@link LinearProbingHashTable} will follow the writeup's guidelines about how to internally resize
     * the hash table when the capacity exceeds 50&#37;
     *
     * @param key   The record's key.
     * @param value The record's value.
     * @return The {@link phonebook.utils.Probes} with the value added and the number of probes it makes.
     * @throws IllegalArgumentException if either argument is {@code null}.
     */
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
        if(table[hash] != null) {
	        while(table[hash] != null) {
	        	probes++;
	        	hash++;
	        	hash = hash%capacity();
	        }
        }
        probes++;
        table[hash] = new KVPair(key, value);
        count++;
        countWTomb++;
        keysVals.add(new KVPair(key, value));
        
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
    	keysVals = new ArrayList<KVPair>();
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
        while(!(table[hash].getKey().equals(key))) {
        	hash++;
        	probes++;
        	if(hash >= capacity()) {
        		hash = 0;
        	}
        	if(hash >= capacity() || table[hash] == null) {
        		return new Probes(null, probes);
        	}
        }
        return new Probes(table[hash].getValue(), probes);
    }


    /**
     * <b>Return</b> the value associated with key in the {@link HashTable}, and <b>remove</b> the {@link phonebook.utils.KVPair} from the table.
     * If key does not exist in the database
     * or if key = {@code null}, this method returns {@code null}. This method is expected to run in <em>amortized constant time</em>.
     *
     * @param key The key to search for.
     * @return The {@link phonebook.utils.Probes} with associated value and the number of probe used. If the key is {@code null}, return value {@code null}
     * and 0 as number of probes; if the key doesn't exist in the database, return {@code null} and the number of probes used.
     */
    @Override
    public Probes remove(String key) {
        if(key == null) {
        	return new Probes(null, 0);
        }
        int probes = 1;
        int hash = hash(key);
        if(!containsKey(key)) {
	        while(table[hash] != null) {
	        	hash++;
	        	probes++;
	        	hash = hash%capacity();
	        }
	        return new Probes(null, probes);
        }
        
        //soft deletion	
        if(softFlag == true) {
        	while(!(table[hash].getKey().equals(key))) {
        		probes++;
        		hash++;
        		hash = hash%capacity();
        	}
        	String value = table[hash].getValue();
        	keysVals.remove(new KVPair(key, value));
        	count--;
        	table[hash] = TOMBSTONE;
        	return new Probes(value, probes);
        }
        //hard deletion
        String value = table[hash].getValue();
        
        ArrayList<KVPair> moving = new ArrayList<KVPair>();
        while(table[hash] != null && !(table[hash].getKey().equals(key))){
        	probes++;
        	hash++;
        	hash = hash%capacity();
        }
        
        table[hash] = null;
        hash++;
        probes++;
        hash = hash%capacity();        
        
        while(table[hash] != null) {
        	moving.add(table[hash]);
        	table[hash] = null;
        	hash++;
        	hash = hash%capacity();
        	probes++;
        	count--;
        	countWTomb--;
        }
        for(int i = 0; i < moving.size(); i++) {
        	probes += (put(moving.get(i).getKey(), moving.get(i).getValue())).getProbes();
        }
        
        keysVals.remove(new KVPair(key, value));
        countWTomb--;
        count--;
        return new Probes(value, probes);
    }

    @Override
    public boolean containsKey(String key) {
    	int hash = hash(key);
        if(table[hash] == null) {
        	return false;
        }
        while(!(table[hash].getKey().equals(key))) {
        	hash++;
        	hash = hash%capacity();
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
    public int size() {
        return count;
    }

    @Override
    public int capacity() {
        return table.length;
    }
}

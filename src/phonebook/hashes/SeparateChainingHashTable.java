package phonebook.hashes;

import java.util.ArrayList;

import phonebook.exceptions.UnimplementedMethodException;
import phonebook.utils.KVPairList;
import phonebook.utils.PrimeGenerator;
import phonebook.utils.Probes;

/**<p>{@link SeparateChainingHashTable} is a {@link HashTable} that implements <b>Separate Chaining</b>
 * as its collision resolution strategy, i.e the collision chains are implemented as actual
 * Linked Lists. These Linked Lists are <b>not assumed ordered</b>. It is the easiest and most &quot; natural &quot; way to
 * implement a hash table and is useful for estimating hash function quality. In practice, it would
 * <b>not</b> be the best way to implement a hash table, because of the wasted space for the heads of the lists.
 * Open Addressing methods, like those implemented in {@link LinearProbingHashTable} and {@link QuadraticProbingHashTable}
 * are more desirable in practice, since they use the original space of the table for the collision chains themselves.</p>
 *
 * @author Kammie Ping
 * @see HashTable
 * @see SeparateChainingHashTable
 * @see LinearProbingHashTable
 * @see OrderedLinearProbingHashTable
 * @see CollisionResolver
 */
public class SeparateChainingHashTable implements HashTable{

    /* ****************************************************************** */
    /* ***** PRIVATE FIELDS / METHODS PROVIDED TO YOU: DO NOT EDIT! ***** */
    /* ****************************************************************** */

    private KVPairList[] table;
    private int count;
    private PrimeGenerator primeGenerator;
    private ArrayList<String> keys;
    private ArrayList<String> values;

    // We mask the top bit of the default hashCode() to filter away negative values.
    // Have to copy over the implementation from OpenAddressingHashTable; no biggie.
    private int hash(String key){
        return (key.hashCode() & 0x7fffffff) % table.length;
    }

    /* **************************************** */
    /*  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  */
    /* **************************************** */
    /**
     *  Default constructor. Initializes the internal storage with a size equal to the default of {@link PrimeGenerator}.
     */
    public SeparateChainingHashTable(){
    	primeGenerator = new PrimeGenerator();
        primeGenerator.reset();
        keys = new ArrayList<String>();
        values = new ArrayList<String>();
        table = new KVPairList[primeGenerator.getCurrPrime()];
        count = 0;
        
        for(int i = 0; i < table.length; i++) {
        	table[i] = new KVPairList();
        }
    }

    @Override
    public Probes put(String key, String value) {
        if(key == null || value == null) {
        	throw new IllegalArgumentException("Key/value cannot be null!");
        }
        int hash = hash(key);
        table[hash].addBack(key, value);
        count++;
        keys.add(key);
        values.add(value);
        return new Probes(value, 1);
    }

    @Override
    public Probes get(String key) {
        if(key == null) {
        	return new Probes(null, 0);
        }
        return table[hash(key)].getValue(key);
    }

    @Override
    public Probes remove(String key) {
        if(key == null) {
        	return new Probes(null, 0);
        }
        if(keys.contains(key)) {
        	values.remove(keys.indexOf(key));
            keys.remove(key);
            count--;
        }
        return table[hash(key)].removeByKey(key);
    }

    @Override
    public boolean containsKey(String key) {
        return table[hash(key)].containsKey(key);
    }

    @Override
    public boolean containsValue(String value) {
        for(int i = 0; i < table.length; i++) {
        	if(table[i].containsValue(value) == true) {
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
        return table.length; // Or the value of the current prime.
    }

    /**
     * Enlarges this hash table. At the very minimum, this method should increase the <b>capacity</b> of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the enlargement heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     * @see PrimeGenerator#getNextPrime()
     */
    public void enlarge() {
    	ArrayList<String> newKeys = new ArrayList<String>();
    	ArrayList<String> newVals = new ArrayList<String>();
    	
    	for(int i = 0; i < keys.size(); i++) {
    		newKeys.add(keys.get(i));
    		newVals.add(values.get(i));
    	}
    	
    	count = 0;
    	keys = new ArrayList<String>();
    	values = new ArrayList<String>();
    	table = new KVPairList[primeGenerator.getNextPrime()];
    	
    	for(int i = 0; i < table.length; i++) {
        	table[i] = new KVPairList();
        }
    	for(int i = 0; i < newKeys.size(); i++) {
    		this.put(newKeys.get(i), newVals.get(i));
    	}
    }

    /**
     * Shrinks this hash table. At the very minimum, this method should decrease the size of the hash table and ensure
     * that the new size is prime. The class {@link PrimeGenerator} implements the shrinking heuristic that
     * we have talked about in class and can be used as a black box if you wish.
     *
     * @see PrimeGenerator#getPreviousPrime()
     */
    public void shrink(){
        ArrayList<String> newKeys = new ArrayList<String>();
    	ArrayList<String> newVals = new ArrayList<String>();
    	for(int i = 0; i < keys.size(); i++) {
    		newKeys.add(keys.get(i));
    		newVals.add(values.get(i));
    	}
    	
    	table = new KVPairList[primeGenerator.getPreviousPrime()];
    	count = 0;
    	keys = new ArrayList<String>();
    	values = new ArrayList<String>();
    	
    	for(int i = 0; i < table.length; i++) {
        	table[i] = new KVPairList();
        }
        for(int i = 0; i < newKeys.size(); i++) {
    		this.put(newKeys.get(i), newVals.get(i));
    	}
    }
}
package chess;

import java.util.*;
/**
 * completely thread unsafe in this incarnation
 */
public class Pool {
	public static final int UNUSED = 0;
	public static final int FREE   = 1;
	public static final int IN_USE = 2;
	
	String name;
	int size;
	PoolFactoryMethod createMethod;

	int numUsed;

	PoolEntry pool[];

	int rollingIndex = 0;
	
	class PoolEntry
	{
		int state;
		Poolable object;
	}

	public Pool(String name, int size, PoolFactoryMethod createMethod)
	{
		this.name = name;
		this.size = size;
		this.createMethod = createMethod;
		this.numUsed = 0;

		pool = new PoolEntry[size];
		for (int i=0; i<size; i++)
		{
			pool[i] = new PoolEntry();
			pool[i].state = UNUSED;
		}
	}
	/**
	 */
	public int getNumUsed() {
		return numUsed;
	}
	/**
	**   Check that no entries in the pool are in use
	*/
	public boolean isPoolEmpty()
	{
		boolean empty = true;
		
		for (int i=0; i<pool.length; i++)
			if (pool[i].state == IN_USE)
			{
				empty = false;
				Debug.debugMsg(this, Debug.ERROR, "Pool not empty!");
			}
		
		return empty;	
	}
	public Poolable obtain()
		throws PoolFullException
	{
		int count = 0;
		while (count++ < size)
		{
			rollingIndex = (++rollingIndex == size)?0:rollingIndex;
			if (pool[rollingIndex].state == UNUSED && createMethod != null)
			{
				pool[rollingIndex].object = createMethod.createPoolObject();
				pool[rollingIndex].object.setPoolID(rollingIndex);
				pool[rollingIndex].state = FREE;
			}
			
			if (pool[rollingIndex].state == FREE)
			{
				pool[rollingIndex].state = IN_USE;
				numUsed++;
				return pool[rollingIndex].object;
			}
		}

		throw new PoolFullException("Pool [" + name + "], size " + size + ", is full.");
	}
	public void release(Poolable obj)
	{
		int index = obj.getPoolID();
		pool[index].state = FREE;
		numUsed--;
	}
	public void setPoolObject(Poolable obj, int index) {
		pool[index].object = obj;
		obj.setPoolID(index);
		pool[index].state = FREE;
	}
}

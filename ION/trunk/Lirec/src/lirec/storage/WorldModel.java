package lirec.storage;

import java.util.ArrayList;
import java.util.HashMap;

import lirec.architecture.Architecture;

/** a specific subclass of lirec storage container with convenience methods added 
 * for handling "agent" and "object" sub containers. */
public class WorldModel extends LirecStorageContainer {

	public static String AGENT_TYPE_NAME = "agent";
	public static String OBJECT_TYPE_NAME = "object";
	
	
	public WorldModel(String name, Architecture architecture) {
		// this container is of the type "WorldModel"
		super(name, "WorldModel", null, architecture);
	}

	
	/** request adding an agent to the world model */
	public void requestAddAgent(String name)
	{
		this.requestAddSubContainer(name, AGENT_TYPE_NAME);
	}
	
	/** request adding an agent to the world model */
	public void requestAddAgent(String name, HashMap<String,Object> initialProperties)
	{
		this.requestAddSubContainer(name, AGENT_TYPE_NAME, initialProperties);
	}
	
	/** request removing an agent from the world model */
	public void requestRemoveAgent(String name)
	{
		if (this.hasSubContainer(name, AGENT_TYPE_NAME))
			this.requestRemoveSubContainer(name);
	}
	
	/** request adding an object to the world model */
	public void requestAddObject(String name)
	{
		this.requestAddSubContainer(name, OBJECT_TYPE_NAME);
	}
	
	/** request adding an object to the world model */
	public void requestAddObject(String name, HashMap<String,Object> initialProperties)
	{
		this.requestAddSubContainer(name, OBJECT_TYPE_NAME, initialProperties);
	}
	
	/** request removing an object from the world model */
	public void requestRemoveObject(String name)
	{
		if (this.hasSubContainer(name, OBJECT_TYPE_NAME))
			this.requestRemoveSubContainer(name);
	}
	
	/** returns whether the world model has an agent of the specified name*/
	public synchronized boolean hasAgent(String name)
	{
		return this.hasSubContainer(name,AGENT_TYPE_NAME);
	}
	
	/** returns whether the world model has an object of the specified name*/
	public synchronized boolean hasObject(String name)
	{
		return this.hasSubContainer(name,OBJECT_TYPE_NAME);
	}
	
	/** returns a list of the names of all agents */
	public synchronized ArrayList<String> getAgentNames()
	{
		return this.getSubContainerNames(AGENT_TYPE_NAME);
	}
	
	/** returns a list of the names of all objects */
	public synchronized ArrayList<String> getObjectNames()
	{
		return this.getSubContainerNames(OBJECT_TYPE_NAME);
	}
	
	/** returns the agent storage container with the specified name or null if 
	 * it does not exist in this world model */
	public synchronized LirecStorageContainer getAgent(String name)
	{
		if (this.hasAgent(name)) return this.getSubContainer(name);
		else return null;
	}
	
	/** returns the object storage container with the specified name or null if 
	 * it does not exist in this world model */
	public synchronized LirecStorageContainer getObject(String name)
	{
		if (this.hasObject(name)) return this.getSubContainer(name);
		else return null;
	}
	
}

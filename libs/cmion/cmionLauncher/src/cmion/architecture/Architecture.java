/*	
    CMION
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  Renamed to CMION
  ---  
*/

package cmion.architecture;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cmion.architecture.EventCmionReady;
import cmion.architecture.IArchitecture;
import cmion.level2.CompetencyExecution;
import cmion.level2.CompetencyLibrary;
import cmion.level3.CompetencyManager;
import cmion.storage.BlackBoard;
import cmion.storage.WorldModel;

import ion.Meta.Element;
import ion.Meta.Simulation;


/** this class is used at the moment as the executable / main entry point for CMION
 *  It is also used for accessing references to all main CMION components */
public class Architecture extends Element implements IArchitecture {
		
	/** the world model in which we store high level symbolic information */
	private WorldModel worldModel;

	/** the black board that stores lower level information for competencies to share
	 *  between each other */
	private BlackBoard blackBoard;
	
	/** the competency execution system */
	private CompetencyExecution competencyExecution;

	/** the competency manager */	
	private CompetencyManager competencyManager;

	/** the competency library */	
	private CompetencyLibrary competencyLibrary;
	
	/** the name of the rule file for the competency manager */
	private String competencyManagerRuleFile;
	
	/** the name of the file with the competency library configuration */
	private String competencyLibraryFile;
	
	/** the name of the file with the initial world model content */
	private String worldModelInitFile;
	
	/** This list stores all custom components that are loaded dynamically, depending on the 
	 *  architecture configuration, i.e. the scenario*/
	private ArrayList<CmionComponent> customComponents; 
	
	/** create a new architecture */
	public Architecture(String architectureConfigurationFile) throws Exception
	{	
		// initialise lists
		customComponents = new ArrayList<CmionComponent>();
		
		// load the configuration file (in here custom components will be built)
		loadConfigFile(architectureConfigurationFile);
		
		// add the architecture itself to the ION Simulation
		Simulation.instance.getElements().add(this);
		Simulation.instance.update();
		
		// now build default components
		
		// 1: create data storage components: Worldmodel and Blackboard
		worldModel = new WorldModel(this,"WorldModel");
		Simulation.instance.getElements().add(worldModel);
		Simulation.instance.update();
		
		blackBoard = new BlackBoard(this,"BlackBoard");
		Simulation.instance.getElements().add(blackBoard);
		Simulation.instance.update();
		
		// 2: create Competency Library, which will in turn create all competencies
		competencyLibrary = new CompetencyLibrary(this, competencyLibraryFile);
		Simulation.instance.getElements().add(competencyLibrary);
		Simulation.instance.update();
		
		// 3: create Competency Execution System
		competencyExecution = new CompetencyExecution(this);
		Simulation.instance.getElements().add(competencyExecution);
		Simulation.instance.update();
		
		// 4: create a competency manager
		competencyManager = new CompetencyManager(this, competencyManagerRuleFile);
		Simulation.instance.getElements().add(competencyManager);
		Simulation.instance.update();
				
		// now register handlers for all CMION Components
		for (Element element : Simulation.instance.getElements())
			if (element instanceof CmionComponent)
				((CmionComponent)element).registerHandlers();

		Simulation.instance.update();
		
		// load world model initial state, if an init file was specified 
		if (worldModelInitFile!=null)
		{	
			worldModel.loadInitFile(worldModelInitFile);
			Simulation.instance.update();
		}
		
		System.out.println("CMION initialised");
	}
	
	/** parses the architecture configuration file that defines what components
	 *  should be loaded */
	private void loadConfigFile(String architectureConfigurationFile) throws Exception
	{
		File configFile = new File(architectureConfigurationFile);
		if (!configFile.exists()) 
			throw new Exception("Could not locate architecture configuration file " + architectureConfigurationFile);
		
		// parse the file to a dom document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (configFile);

		// normalize text representation
		doc.getDocumentElement().normalize();
			
		// read <ArchitectureConfiguration> tags
		NodeList configTags = doc.getElementsByTagName("ArchitectureConfiguration");		
		if (configTags.getLength() != 1) throw new Exception("Error in Architecture Configuration File: xml file needs exactly 1 tag ArchitectureConfiguration");
		NamedNodeMap attribs = configTags.item(0).getAttributes();
		
		// read attribute CompetencyManagerRulesFile
		Node atrCompManFile = attribs.getNamedItem("CompetencyManagerRulesFile");
		if (atrCompManFile!=null) 
			competencyManagerRuleFile = atrCompManFile.getNodeValue();
		else
			throw new Exception("Architecture Configuration file does not specify competency manager rules file. Attribute CompetencyManagerRulesFile not found.");

		// read attribute CompetencyLibraryConfigurationFile
		Node atrCompLibFile = attribs.getNamedItem("CompetencyLibraryConfigurationFile");
		if (atrCompLibFile!=null) 
			competencyLibraryFile = atrCompLibFile.getNodeValue();
		else
			throw new Exception("Architecture Configuration file does not specify competency library configuration file. Attribute CompetencyLibraryConfigurationFile not found.");

		// read optional attribute WorldModelInitFile
		Node atrWorldModelFile = attribs.getNamedItem("WorldModelInitFile");
		if (atrWorldModelFile!=null) 
			worldModelInitFile = atrWorldModelFile.getNodeValue();
		else
			worldModelInitFile=null;

		
		// load custom components and create them
		
		// search for all ArchitectureComponent tags
		NodeList allChildren = configTags.item(0).getChildNodes();
		for (int i=0; i<allChildren.getLength(); i++)
		{
			Node node = allChildren.item(i);
			if (node.getNodeName().equals("ArchitectureComponent"))
			{
				attribs = node.getAttributes();
				
				// read attribute ClassName
				String className;
				Node atrClassName = attribs.getNamedItem("ClassName");
				if (atrClassName!=null) 
					className = atrClassName.getNodeValue();
				else
					throw new Exception("No class name specified for an architecture component in architecture configuration file.");

				// read attribute ConstructorParameters
				String constructorParametersStr = "";
				Node atrConstrPars = attribs.getNamedItem("ConstructorParameters");
				if (atrConstrPars!=null)
					constructorParametersStr= atrConstrPars.getNodeValue();
				
				// create array to store values of the parameters of the constructor
				ArrayList<Object> constructorParameters = new ArrayList<Object>();
		
				// create array that specifies the classes of the parameters of the constructor
				ArrayList<Class<?>> constructorClasses = new ArrayList<Class<?>>();
				
				// the first parameter to the constructor is always a reference to the architecture for all CMION components
				constructorParameters.add(this);
				// its class is IArchitecture
				constructorClasses.add(IArchitecture.class);
				
				// di-sect constructor parameters (in the string all parameters are seperated by a comma)
				StringTokenizer st = new StringTokenizer(constructorParametersStr,",");
				while (st.hasMoreTokens())
				{
					String token = st.nextToken().trim();
					if (!token.equals(""))
					{
						constructorParameters.add(token);
						// class is String
						constructorClasses.add(String.class);
					}
				}
				
				// dynamically construct custom component
				
				// obtain class 
				Class<?> cls = Class.forName(className);
								
				// obtain the constructor 
				Constructor<?> constructor = cls.getConstructor(constructorClasses.toArray(new Class[constructorClasses.size()]));
				
				// construct an instance from the constructor
				Object instance = constructor.newInstance(constructorParameters.toArray());
				
				// check if instance is of the right type
				if (!(instance instanceof CmionComponent)) throw new Exception("CMION component could not be built. "+ className+ " is not a subclass of CmionComponent");
				
				// add custom component to the simulation
				customComponents.add((CmionComponent) instance);
				Simulation.instance.getElements().add((CmionComponent) instance);		
				Simulation.instance.update();
				
			}
		}
	
	}
	
	/** function that updates the ion simulation regularly*/
	private void runSimulation()
	{
		System.out.println("CMION running");
		
		// first start the competencies that are constantly running in the background
		competencyLibrary.startBackgroundCompetencies();
		
		int counter = 0;
				
		// run the ion simulation in an endless loop
		while (true)
		{	
			Simulation.instance.update();
			
			// sleep a little while to give other processes some time as well
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}

			counter++;
			
			// for now, raise the event that architecture is ready here after waiting for a while, later needs to be more 
			// sophisticated (maybe receiving signals from competencies that they are ready)
			if (counter == 20) 
				this.raise(new EventCmionReady());
		}		
	}
	
	/** returns the world model component */
	@Override
	public WorldModel getWorldModel()
	{
		return worldModel;
	}
	
	/** returns the black board component */
	@Override
	public BlackBoard getBlackBoard()
	{
		return blackBoard;
	}
	
	/** returns the competency execution component */
	@Override
	public CompetencyExecution getCompetencyExecution()
	{
		return competencyExecution;
	}
	
	/** returns the competency manager component */
	@Override
	public CompetencyManager getCompetencyManager()
	{
		return competencyManager;
	}
	
	/** returns the competency library component */
	@Override
	public CompetencyLibrary getCompetencyLibrary()
	{
		return competencyLibrary;
	}
	
	/** main method */
	public static void main(String[] args)
	{
		try
		{
			Architecture architecture;
			
			// initialisation
			if(args.length > 0){
				architecture = new Architecture(args[0]);
			} else {
				architecture = new Architecture("ArchitectureConfiguration.xml");
			}

			// run
			architecture.runSimulation();	

		}
		
		// catch exception thrown during constructing the Architecture
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
			
	}
	
	@Override
	public void onDestroy() {}
	
	@Override
	public Object getSystemContext() {
		return null;
	}	

}

/*	
        Lirec Architecture
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
  ---  
*/

package lirec.level2;

import ion.Meta.Simulation;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lirec.architecture.IArchitecture;
import lirec.architecture.LirecComponent;

/** the competency library is the component that registers all components */
public class CompetencyLibrary extends LirecComponent {

	/** the list of competencies that are available for execution by the competency
	 *  execution system */
	private ArrayList<Competency> competencies;
	
	/** the list of competencies that should run in the background constantly, started
	 *  up when the architecture is started */
	private ArrayList<Competency> backgroundCompetencies;

	/** Create a new competency library */
	public CompetencyLibrary(IArchitecture architecture, String competencyLibraryFile) throws Exception 
	{
		super(architecture);
		
		// create competencies array list
		competencies = new ArrayList<Competency>();
		
		// create background competencies array list
		backgroundCompetencies = new ArrayList<Competency>();
		
		// load competency library file (in this function competencies that are 
		// specified in the file, will be built
		loadConfigurationFile(competencyLibraryFile);			
		
		// add all competencies that were loaded to ION and initialise
		for (Competency competency : competencies)
		{	
			Simulation.instance.getElements().add(competency);
			competency.initialize();
		}

		// same for background competencies
		for (Competency competency : backgroundCompetencies)
		{	
			Simulation.instance.getElements().add(competency);
			competency.initialize();
		}	
	}

	/** load the competency library configuration file that specifies which competencies to load*/
	private void loadConfigurationFile(String competencyLibraryFile) throws Exception
	{
		File configFile = new File(competencyLibraryFile);
		if (!configFile.exists()) 
			throw new Exception("Could not locate competency library configuration file " + competencyLibraryFile);
		
		// parse the file to a dom document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (configFile);

		// normalize text representation
		doc.getDocumentElement().normalize();
			
		// read <CompetencyLibrary> tags
		NodeList libTags = doc.getElementsByTagName("CompetencyLibrary");		
		if (libTags.getLength() != 1) throw new Exception("Error in Competency Library Configuration File: xml file needs exactly 1 tag CompetencyLibrary");
		
		// load competencies and create them
		
		// search for all Competency tags
		NodeList allChildren = libTags.item(0).getChildNodes();
		for (int i=0; i<allChildren.getLength(); i++)
		{
			Node node = allChildren.item(i);
			if (node.getNodeName().equals("Competency"))
			{
				NamedNodeMap attribs = node.getAttributes();
				
				// read attribute ClassName
				String className;
				Node atrClassName = attribs.getNamedItem("ClassName");
				if (atrClassName!=null) 
					className = atrClassName.getNodeValue();
				else
					throw new Exception("No class name specified for an architecture component in architecture configuration file.");

				// read attribute RunInBackground default: false
				boolean runInBackground = false;
				Node atrRunInBg = attribs.getNamedItem("RunInBackground");
				if (atrRunInBg!=null)
					if (atrRunInBg.getNodeValue().trim().equalsIgnoreCase("True"))
						runInBackground = true;
				
				// read attribute ConstructorParameters: default none
				String constructorParametersStr = "";
				Node atrConstrPars = attribs.getNamedItem("ConstructorParameters");
				if (atrConstrPars!=null)
					constructorParametersStr= atrConstrPars.getNodeValue();
				
				// create array to store values of contrucotr parameters
				ArrayList<Object> constructorParameters = new ArrayList<Object>();
				// create array that specifies the classes of the parameters of the constructor
				ArrayList<Class<?>> constructorClasses = new ArrayList<Class<?>>();
				
				// the first parameter to the constructor is always a reference to the architecture for all lirec components				
				constructorParameters.add(architecture);
				// it's class is IArchitecture
				constructorClasses.add(IArchitecture.class);
				
				// di-sect constructor parameters (in the string all parameters are seperated by a comma)
				StringTokenizer st = new StringTokenizer(constructorParametersStr,",");
				while (st.hasMoreTokens())
				{
					String token = st.nextToken().trim();
					if (!token.equals(""))
					{
						constructorParameters.add(token);
						constructorClasses.add(String.class);
					}
				}
				
				// dynamically construct custom competency
				
				// obtain class 
				Class<?> cls = Class.forName(className);
				
				// obtain the constructor 
				Constructor<?> constructor = cls.getConstructor(constructorClasses.toArray(new Class[constructorClasses.size()]));
				
				// construct an instance from the constructor
				Object instance = constructor.newInstance(constructorParameters.toArray());
				
				// check if instance is of the right type
				if (!(instance instanceof Competency)) throw new Exception("Competency could not be loaded because "+ className+ " is not a subclass of Competency");
		
				// add to our list of competencies
				if (runInBackground)
					backgroundCompetencies.add((Competency)instance);
				else	
					competencies.add((Competency)instance);			
			}
		}
		
		
	}

	@Override
	public final void registerHandlers() 
	{
		// the competency library needs no handlers at the moment
	}
	
	/** in this function all competencies that are continuously running in the background are started */
	public void startBackgroundCompetencies()
	{
		// if we have any competencies to start do the starting in a separate thread
		if (backgroundCompetencies.size()>0) new BackgroundCompetencyStarter().start();	
	}
	
	
	/** return a List of competencies with the given type, the list will be empty
	 *  if no such competency is in the library */
	public ArrayList<Competency> getCompetencies(String type)
	{
		ArrayList<Competency> returnList = new ArrayList<Competency>();
		for (Competency competency : competencies)
			if (competency.getCompetencyType().equals(type))
				returnList.add(competency);

		return returnList;
	}
	
	/** thread for starting the background competencies. this might look at first
	 *  like an over complication, but the reason for handling the starting like here
	 *  is that we cannot be sure when exactly the background competencies will be available,
	 *  e.g. if an external client program needs to connect first, that's why we 
	 *  have to keep trying */
	private class BackgroundCompetencyStarter extends Thread
	{
		@Override
		public void run()
		{
			// create list for storing the competencies that we have already 
			// started, initially empty
			ArrayList<Competency> alreadyStartedCompetencies = new ArrayList<Competency>();
			
			// initially of course not all competencies are started
			boolean allStarted = false;
	
			while (!allStarted)
			{
				// assume all are started
				allStarted = true;

				// iterate over background competencies, attempt to start them
				for (Competency competency : backgroundCompetencies)
					if (!alreadyStartedCompetencies.contains(competency))
					{
						// this competency is not started yet, see if we can start it now & here						
						if (competency.isAvailable()) 
						{
							// it is available, so we can start it and add it to the started competencies list
							// note: competency start parameters are empty, if something needs to be passed to
							// the competency, the constructor can be used
							competency.requestStartCompetency(new HashMap<String,String>());
							alreadyStartedCompetencies.add(competency);
						}
						else allStarted = false;
					}
			}
			
			// all competencies are started: print out a message
			System.out.println("All background competencies are started");
		}
	}
	
	
	
	

}
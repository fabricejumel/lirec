package FAtiMA.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Samuel
 * 
 */
public class AgentLogger {
	
	private final int LOGGING_INTERVAL = 500; 
	private int _counter = 0;
	private boolean _initialized;
	private BufferedWriter outFile;
	
	/**
	 * Singleton pattern 
	 */
	private static AgentLogger _agentLoggerInstance;
	
	public static AgentLogger GetInstance()
	{
		if(_agentLoggerInstance == null)
		{
			_agentLoggerInstance = new AgentLogger();
		}
		
		return _agentLoggerInstance;
	} 
	
	private AgentLogger(){
		this._initialized = false;
	}
	
	public void initialize(String logName) throws IOException{	
		
		outFile = new BufferedWriter(new FileWriter(logName+"-Log.txt"));
		System.out.println("Log Initialized");
		
		this._initialized = true;
	}

	public void log(String msg) {
		
		if(this._initialized){
			try{
				outFile.write("\n"+msg+"\n");
				outFile.flush();
			}catch(IOException e){
				System.out.println("WARNING: IOexception when writing to the log");	
			}   
		}else{
			System.out.println("WARNING: Attempt to write in the log without initializing it first! Nothing will be written");	
		}   
	}
	
	
	public void logAndPrint(String msg) {
		System.out.println(msg);
		this.log(msg);
	}	

	
	public void close() {
		try{
			outFile.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void intermittentLog(String msg){
		_counter++;
		if(_counter == 1){
			this.log(msg);	
		}
		if(_counter == LOGGING_INTERVAL){
			_counter = 0;
		}
	}
}

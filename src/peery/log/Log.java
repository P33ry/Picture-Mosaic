package peery.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {

	public static Log log;
	public static final boolean silenceDebug = false,
	appendEvents = true, appendErrors = false;
	
	public final File eventFile, errorFile;
	private BufferedWriter eventWriter, errorWriter;
	
	
	public Log(String location){
		this.eventFile = new File(location+"/eventLog.log");
		this.errorFile = new File(location+"/ERROR.log");
		
		try {
			if(!this.eventFile.exists()){
				this.eventFile.createNewFile();
			}
			if(!this.errorFile.exists()){
				this.errorFile.createNewFile();
			}
			this.eventWriter = new BufferedWriter(new FileWriter(eventFile, appendEvents));
			this.errorWriter = new BufferedWriter(new FileWriter(errorFile, appendErrors));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void initLog(String location){
		if(Log.log != null){
			return;
		}
		
		Log.log = new Log(location);
	}
	
	public static void log(int logLvl, String message){
		Log.log.logs(logLvl, message);
	}
	
	public static void log(LogLevel lv, String message){
		Log.log.logs(lv.ordinal(), message);
	}
	
	@SuppressWarnings("unused")
	public void logs(int logLvl, String message){
		String prefix = LogLevel.values()[logLvl].toString();
		prefix = "["+prefix+"]";
		BufferedWriter logWriter;
		if(silenceDebug && logLvl == LogLevel.Debug.ordinal()){
			return;
		}
		
		if(logLvl == LogLevel.Error.ordinal()){
			logWriter = this.errorWriter;
		}
		else{
			logWriter = this.eventWriter;
		}
		String timeStamp = new java.util.Date().toString();
		String msg = "["+timeStamp+"]"+prefix+" "+message;
		System.out.println(msg);
		try {
			logWriter.write(msg+"\n");
			logWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Log.initLog("/home/peery/Software_Projects/EclipseWorkspace/Picture Mosaic/resources/");
		Log.log(LogLevel.Debug, "Test!");
		Log.log(LogLevel.Error, "TEST ERROR");
	}
}

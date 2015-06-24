package org.sidiff.common.services.debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.Bundle;
import org.sidiff.common.io.IOUtil;
import org.sidiff.common.services.ContextSensitiveService;
import org.sidiff.common.services.Service;
import org.sidiff.common.services.ServiceContext;
import org.sidiff.common.services.ServiceContext.MissingServiceException;
import org.sidiff.common.services.events.SCEventListener;
import org.sidiff.common.util.Debugger;
import org.sidiff.common.util.ReflectionUtil;
import org.sidiff.common.util.StringUtil;

public class SiDiffDebugger implements CommandProvider, ContextSensitiveService {

	/**
	 * Name of the file which stores recently used debugger commands,
	 */
	private static final String HISTORY_FILENAME = System.getProperty("user.home")+System.getProperty("file.separator")+".sidiffdebugger";

	private static SiDiffDebugger INSTANCE = null;
	
	private SiDiffDebugger() {
	}
	
	////////////////////////////////////////////////
	
	/**
	 * The service context, if the debugger has been initialized.
	 */
	private ServiceContext serviceContext = null;
	
	/**
	 * The list of arbitrary debugger objects.
	 */
	private static HashMap<String, Debugger> debuggers = new HashMap<String, Debugger>();
	
	/**
	 * Shortcuts for debugger names.
	 */
	private static HashMap<String, String> shortCutDebuggers = new HashMap<String, String>();

	/**
	 * Resources used during initialization.
	 */
	private ArrayList<Resource> resources;
	
	private ArrayList<String> commandHistory;
	private File commandHistoryFile;
	
	/**
	 * Initialization.
	 */
	@Override
	public void initialize(ServiceContext serviceContext, Object... params) {
		this.resources = new ArrayList<Resource>();
		this.commandHistory = new ArrayList<String>();
		this.serviceContext = serviceContext;
		
		for (Object o: params) {
			if (o instanceof Resource) {
				this.resources.add((Resource)o);
			}
		}
		
		for(Debugger dbg : debuggers.values()){
			if(dbg instanceof ContextSensitiveService){
				((ContextSensitiveService)dbg).initialize(serviceContext, params);
			}
		}
		
		shortCutDebuggers.put("cs", "org.sidiff.core.correspondences.CorrespondencesService");
		shortCutDebuggers.put("cd", "org.sidiff.core.correspondences.internal.CorrespondencesDebugger");
		shortCutDebuggers.put("sd", "org.sidiff.core.similarities.internal.SimilaritiesDebugger");
		shortCutDebuggers.put("ss", "org.sidiff.core.similarities.SimilaritiesService");
		shortCutDebuggers.put("ad", "org.sidiff.common.emf.annotation.internal.AnnotationDebugger");
		
		commandHistoryFile = new File(HISTORY_FILENAME);
		
		if (commandHistoryFile.canRead()) {
			try {
				FileInputStream fstream = new FileInputStream(commandHistoryFile);
				for (String s : IOUtil.readFromStream(fstream).split("\n"))
					commandHistory.add(s);
			} catch (FileNotFoundException e) {
			}
		}
		
	}
	
	/**
	 * Adds an arbitrary object which can be used for debugging.
	 * @param debugger
	 */
	public static boolean addDebugger(Debugger debugger) {
		return debuggers.put(debugger.getClass().getName(), debugger) == null;
	}

	/**
	 * Adds an arbitrary object which can be used for debugging
	 * and also registers a shortcut for this object.
	 * @param debugger
	 */
	public static boolean addDebugger(String shortcut, Debugger debugger) {
		boolean result = true;
		result &= debuggers.put(debugger.getClass().getName(), debugger) == null;
		String key = shortcut;
		if (shortCutDebuggers.containsKey(key)) {
			int i = 2;
			do {
				key = key + "" + i;
				i++;
			} while (shortCutDebuggers.containsKey(key));
		}
		result &= shortCutDebuggers.put(key, debugger.getClass().getName()) == null;
		return result;
	}

	/**
	 * Parses a string descriptor of an element.
	 * @param element
	 * @return
	 */
	private EObject getElement(String element) {
		String[] eid = element.split(":");
		Resource res = getResource(eid[0]);
		
		return res.getEObject(eid[1]);
	}
	
	/**
	 * Parses a string descriptor of a resource.
	 * @param resource
	 * @return
	 */
	private Resource getResource(String resource) {
		return resources.get(Integer.parseInt(resource));
	}
	
	/**
	 * Checks whether this debugger has been initialized. 
	 * @param commandInterpreter
	 * @return
	 */
	private boolean initialized(CommandInterpreter commandInterpreter) {
		if (serviceContext==null) {
			commandInterpreter.println("SidiffDebugger has not been initialized!\n"+
					"You should insert it into a service context, before that context is initialized.");
			return false;
		}
		return true;
	}
	
	/**
	 * Prints the available commands to the console.
	 */
	@Override
	public String getHelp() {
		return 	"--- Sidiff Debugger commands ---\n\t" +
				"resources -> lists known resources \n\t" +
				"debuggers -> lists known debuggers (except static classes) \n\t" + 
				"debug [debugger] [method] [params...] -> executes the given method of the given debugger with the given parameters \n\t" +
				"debug2file [filename] [debugger] [method] [params...] -> equal to debug [debugger] [method] [params...], but the result is written to the given file \n\t" +
				"shortcuts -> lists known shortcuts \n\t" +
				"setsc [shortcut] [string] -> sets a shortcut (used for names of debuggers, services, and functions) \n\t" +
				"debuggerinfo [debugger] -> lists the methods of a debugger \n\t" + 
				"dbhistory -> lists recently used commands ('dbhistory clear' clears the list) \n\t" + 
				"inspect [element] -> lists all properties of an element \n" + 
				"";
	}
	
	/**
	 * Lists the available resources.
	 * @param commandInterpreter
	 * @throws Exception
	 */
	public void _resources(CommandInterpreter commandInterpreter) throws Exception {
		if (!initialized(commandInterpreter))
			return;
		commandInterpreter.println("Known resources:");
		int i = 0;
		for (Resource r: resources) {
			commandInterpreter.println(i++ +" "+r.getURI());
		}
	}
	
	/**
	 * Lists the available debuggers except classes which only contain static methods.
	 * @param commandInterpreter
	 * @throws Exception
	 */
	public void _debuggers(CommandInterpreter commandInterpreter) throws Exception {
		if (!initialized(commandInterpreter))
			return;
		commandInterpreter.println("available debuggers (except classes which only contain static methods):");
		for (String d: debuggers.keySet()) {
			commandInterpreter.println("  "+d);
		}
		commandInterpreter.println("available services which can be queried reflectively:");
		for (Class<?> s : serviceContext.getServiceIDs()) {
			commandInterpreter.println("  "+s.getName());
		}
	}
	
	/**
	 * Lists the currently defined shortcuts.
	 * @param commandInterpreter
	 * @throws Exception
	 */
	public void _shortcuts(CommandInterpreter commandInterpreter) throws Exception {
		if (!initialized(commandInterpreter))
			return;
		commandInterpreter.println("available shortcuts:");
		for (String s: shortCutDebuggers.keySet()) {
			commandInterpreter.println("  "+s+" -> "+shortCutDebuggers.get(s));
		}
	}
	
	/**
	 * Defines a shortcut.
	 */
	public static boolean setShortcut(String shortcut, String longstring) {
		return shortCutDebuggers.put(shortcut, longstring) == null;
	}
	
	/**
	 * Defines a shortcut.
	 * @param commandInterpreter
	 * @throws Exception
	 */
	public void _setsc(CommandInterpreter commandInterpreter) throws Exception {
		if (!initialized(commandInterpreter))
			return;
		String shortcut = commandInterpreter.nextArgument();
		String longstring = commandInterpreter.nextArgument();
		setShortcut(shortcut, longstring);
	}
	
	/**
	 * Resolves a debugger by its name.
	 * @param debuggerName
	 * @return
	 */
	private Object getDebugger(String debuggerName) {
		if (shortCutDebuggers.containsKey(debuggerName))
			debuggerName = shortCutDebuggers.get(debuggerName);
		Object debugger = debuggers.get(debuggerName);
		if (debugger==null) {
			try {
				debugger = serviceContext.getService(ReflectionUtil.loadClass(debuggerName));
			} catch (ClassNotFoundException e) {
			} catch (MissingServiceException e) {
			}
		}
		if (debugger==null) {
			try {
			debugger = ReflectionUtil.loadClass(debuggerName);
			} catch (ClassNotFoundException e) {
				System.err.println("Unable to find debugger: "+debuggerName);
			}
		}
		return debugger;
	}
	
	/**
	 * Methods which are ignored when listing the operations of a debugger (i.e. the operations which are defined by java.lang.Object.
	 */
	private static List<String> IGNORED_METHODS = Arrays.asList(new String[]{"wait", "hashCode", "getClass", "equals", "toString", "notify", "notifyAll"});
	
	/**
	 * Lists the operations available for a given debugger.
	 * @param commandInterpreter
	 * @throws Exception
	 */
	public void _debuggerinfo(CommandInterpreter commandInterpreter) throws Exception {
		if (!initialized(commandInterpreter))
			return;
		String debuggerName = commandInterpreter.nextArgument();
		Object debugger = getDebugger(debuggerName);
		Class<?> cls;
		if (debugger instanceof Class<?>) {
			cls = (Class<?>)debugger;
		} else {
			cls = debugger.getClass();
		}
		commandInterpreter.println("Methods of "+debuggerName+":");
		for (Method m : cls.getMethods()) {
			if (IGNORED_METHODS.contains(m.getName()))
				continue;
			commandInterpreter.print("  "+m.getReturnType()+" "+m.getName()+"(");
			for (Class<?> c : m.getParameterTypes()) {
				commandInterpreter.print(c.getName()+", ");
			}
			if (m.isVarArgs())
				commandInterpreter.print("... ");
			commandInterpreter.println(")");
		}
		
	}	
	
	/**
	 * Delegates the call to _debug, however, the result is not written to the console but into the given file.
	 * @param commandInterpreter
	 * @throws Exception
	 */
	public void _debug2file(final CommandInterpreter commandInterpreter) throws Exception {
		String filename = commandInterpreter.nextArgument();
		addToHistory(filename);
		FileWriter outFile = new FileWriter(filename);
		final PrintWriter out = new PrintWriter(outFile);

		_debug(new CommandInterpreter() {
			@Override
			public void println(Object o) {
				out.println(o);
			}
			@Override
			public void println() {
				out.println();
			}
			@Override
			public void printStackTrace(Throwable t) {
			}
			@SuppressWarnings("unchecked")
			@Override
			public void printDictionary(Dictionary dic, String title) {
			}
			@Override
			public void printBundleResource(Bundle bundle, String resource) {
			}
			@Override
			public void print(Object o) {
				out.print(o);
			}
			@Override
			public String nextArgument() {
				return commandInterpreter.nextArgument();
			}
			@Override
			public Object execute(String cmd) {
				return null;
			}
		});
		out.close();
		commandInterpreter.println("Done. Results written to "+filename);
	}
	
	/**
	 * Runs the debug command. I.e. the debugger is resolved and the respective method is called reflectively. 
	 * @param commandInterpreter
	 * @throws Exception
	 */
	public void _debug(CommandInterpreter commandInterpreter) throws Exception {
		if (!initialized(commandInterpreter))
			return;
		String debuggerName = commandInterpreter.nextArgument();
		String functionName = commandInterpreter.nextArgument();
		if ((debuggerName.equals("my")||debuggerName.equals("all"))
				&& (functionName.equalsIgnoreCase("error")||functionName.equalsIgnoreCase("errors"))) {
			commandInterpreter.println("What do you think who I am? I cannot do everything you want me to do! But in this case I can: The answer is 42.");
			return;
		}
		Object debugger = getDebugger(debuggerName);
		if (shortCutDebuggers.containsKey(functionName))
			functionName = shortCutDebuggers.get(functionName);
		ArrayList<String> params = new ArrayList<String>();
		String p = commandInterpreter.nextArgument();
		while (p != null) {
			params.add(p);
			p = commandInterpreter.nextArgument();
		}
		
		if (debugger==null) {
			commandInterpreter.println("error: no such debugger or service!");
			return;
		}
		ArrayList<Method> methods = new ArrayList<Method>();
		Class<?> cls;
		if (debugger instanceof Class<?>) {
			cls = (Class<?>)debugger;
		} else {
			cls = debugger.getClass();
		}
		for (Method m : cls.getMethods()) {
			if (m.getName().equals(functionName)) {
				methods.add(m);
			}
		}
		if (methods.isEmpty()) {
			commandInterpreter.println("error: no such method!");
			return;
		}
		for (int m=0; m<methods.size(); m++) {
			ArrayList<Object> paramValues = new ArrayList<Object>();
			Method method = methods.get(m);
			if (method.getParameterTypes().length == 0) {
				if (params.size() != 0) {
					if (m<methods.size()-1) { 
						continue;
					} else {
						commandInterpreter.println("error: no such method");
						return;
					}
				}
				try {
					commandInterpreter.println(StringUtil.resolve(method.invoke(debugger)));
					addToHistory(debuggerName,functionName,params);
					return;
				} catch (Exception e) {
					if (m<methods.size()-1) { 
						continue;
					} else {
						commandInterpreter.println("error: no such method");
						return;
					}
				}
			}
			Class<?> type = null;
			for (int i=0; i<params.size(); i++) {
				if (i<method.getParameterTypes().length)
					type = method.getParameterTypes()[i];
				if (type == Resource.class) {
					try {
						paramValues.add(getResource(params.get(i)));
					} catch (Exception e) {
						if (m<methods.size()-1) { 
							break;
						} else {
							commandInterpreter.println("error: not a resource "+params.get(i));
							return;
						}
					}
				} else if (type == EObject.class) {
					try {
						paramValues.add(getElement(params.get(i)));
					} catch (Exception e) {
						if (m<methods.size()-1) { 
							break;
						} else {
							commandInterpreter.println("error: not an element "+params.get(i));
							return;
						}
					}
				} else if (type == String.class) {
					paramValues.add(params.get(i));
				} else if (Service.class.isAssignableFrom(type)) {
					String sname = params.get(i);
					if (shortCutDebuggers.containsKey(sname))
						sname = shortCutDebuggers.get(sname);
					try {
						Object service = serviceContext.getService(ReflectionUtil.loadClass(sname));
						paramValues.add(service);
					} catch (ClassNotFoundException e) {
						if (m<methods.size()-1) { 
							break;
						} else {
							commandInterpreter.println("error: not a service "+sname);
							return;
						}
					}
				} else {
					if (m<methods.size()-1) { 
						break;
					} else {
						commandInterpreter.println("error: unsupported type "+type.getName());
						return;
					}
				}
			}
			try {
				commandInterpreter.println(StringUtil.resolve(method.invoke(debugger, paramValues.toArray())));

				addToHistory(debuggerName,functionName,params);
				
				return;
			} catch (Exception e) {
				if (m < methods.size() - 1) {
					continue;
				} else {
					commandInterpreter.println("error: no such method");
					return;
				}
			}
		}
	}

	public void _inspect(CommandInterpreter commandInterpreter) throws Exception {
		EObject element = getElement(commandInterpreter.nextArgument());
		if (element != null) {
			for (EStructuralFeature sf : element.eClass().getEAllStructuralFeatures()) {
				commandInterpreter.println(sf.getName()+"\t:   "+StringUtil.resolve(element.eGet(sf)));
			}
		}
	}
	/**
	 * shows recent commands or deletes them
	 */
	public void _dbhistory(CommandInterpreter commandInterpreter) throws Exception {
		if (!initialized(commandInterpreter))
			return;
		String clean = commandInterpreter.nextArgument();
		if ("clean".equals(clean)) {
			commandInterpreter.println("deleted history of commands...");
			commandHistory.clear();
			if (commandHistoryFile.canWrite()) {
				commandHistoryFile.delete();
			}
		} else {
			commandInterpreter.println("recent commands:");
			for (String s: commandHistory)
				commandInterpreter.println("  "+s);
			}
	}
	
	private void addToHistory(String fileName) {
		String line = "D2F "+fileName+" ";
		if (!commandHistory.contains(line)) {
			commandHistory.add(line);
			//appendToHistoryFile(line);
		}
	}
	
	private void addToHistory(String debuggerName, String functionName, ArrayList<String> params) {
		String line = null;
		if (commandHistory.size()>0) {
			line = commandHistory.get(commandHistory.size()-1);
			if (line.startsWith("D2F")) {
				commandHistory.remove(commandHistory.size()-1);
				line = line.replaceFirst("^D2F", "debug2file");
			} else {
				line = "debug ";
			}
		}
		if (line == null) {
			line = "debug ";
		}
		line += debuggerName + " " + functionName;
		for (String p : params)
			line += " "+p;
		if (!commandHistory.contains(line)) {
			commandHistory.add(line);
			appendToHistoryFile(line);
		}
	}
	
	private void appendToHistoryFile(String line) {
		if (!commandHistoryFile.exists()) {
			try {
				commandHistoryFile.createNewFile();
			} catch (IOException e) {
			}
		}
		if (commandHistoryFile.canWrite()) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(commandHistoryFile, true));
				out.write(line+"\n");
				out.close();
			} catch (IOException e) {
			}
		}
	}

	public static SiDiffDebugger getInstance() {
		
		if(SiDiffDebugger.INSTANCE==null){
			SiDiffDebugger.INSTANCE= new SiDiffDebugger();			
		}
		return SiDiffDebugger.INSTANCE;
	}
}

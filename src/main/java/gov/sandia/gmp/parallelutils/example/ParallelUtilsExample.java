package gov.sandia.gmp.parallelutils.example;

import gov.sandia.gmp.parallelutils.ParallelBroker;
import gov.sandia.gmp.parallelutils.ParallelBroker.ParallelMode;
import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.parallelutils.ParallelTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;

/**
 * @author bjlawry
 * <p>This example represents a simple HelloWorld-type program.  It
 * demonstrates how to create a list of tasks and submit them to ParallelUtils
 * for distributed execution, and shows how to process the results.  It also
 * shows how to restart driver and nodes with different memory requirements
 * programmatically, without having to load new configuration files.
 */
public class ParallelUtilsExample {


	public static void main(String[] args){
		ParallelBroker p = ParallelBroker.create(ParallelMode.CONCURRENT_FABRIC);
		
		long sleep = 1000;
		long numTasks = 100;
		//p.setClientThreadPoolSize(1000);
		//p.setTaskTimeout(60);
		
		if(args.length == 1){
			try {
				sleep = Math.max(250L,Long.parseLong(args[0]));
			} catch (NumberFormatException e){}
		}

		//Create task list:
		LinkedList<ExampleTask> tasks1 = new LinkedList<ExampleTask>();
		//LinkedList<ExampleTask> tasks2 = new LinkedList<ExampleTask>();
		for(int i = 0; i < numTasks; i++){
			//p.submit(new ExampleTask(sleep));
			tasks1.addLast(new ExampleTask(sleep));
			//tasks2.addLast(new ExampleTask(sleep));
		}
		
		//The call to submit(tasks) will block until all tasks are executed
		//and returned.
		p.submit(tasks1);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//((ParallelBrokerDistributed) p).nodeNotification();
		
		//System.out.println("After run");

		/*List<ParallelResult> prs = null;
		while ((prs = p.getResultsWait()) != null) {
			for (ParallelResult pr : prs) {
				System.out.println(pr);
			}
		} */
	    ParallelResult pr = null;
		while ((pr = (ParallelResult)p.getResultWait()) != null) {
			System.out.println(pr);
		} 

	/*	int taskResultCount = 0;
		while (taskResultCount < numTasks) {
			ParallelResult pr = p.getResult();
			if (pr != null) {
				System.out.println(pr);
				taskResultCount++;
			}
		}
	
		int taskResultCount = 0;
		while (taskResultCount < numTasks) {
			List<ParallelResult> prs = p.getResults();
			if (prs != null) {
				for (ParallelResult pr : prs) {
					System.out.println(pr);
					taskResultCount++;
				}
			}
		} */


		
		//Releases all NRM resources (driver, nodes):
		System.out.println("CLOSE");
		p.close();
		
		//Demonstrates how to do a second run with different heap requirements:
	/*	p.setDriverMaxMemory(384);
		p.setNodeMaxMemory(384);
		
		//Reconnect (restarts driver, nodes):
		p.submit(tasks2);
		for(ParallelResult r : p.getResults()) System.out.println(r);
		
		p.close(); */
		
		//This is necessary because NRM has threads running in background that
		//will prevent the system from exiting after main() returns:
		System.exit(0);
	}

    
	
	/**
	 * @author bjlawry
	 * <p>Simple task implementation.  Each task must call "setResult()" at the
	 * end of the run() method's execution.
	 */
	public static class ExampleTask extends ParallelTask{
		private static final long serialVersionUID = 1L;
		private ExampleResult tr;
		private long sleepTime;

		public ExampleTask(long sleep){ 
			sleepTime = Math.max(250L,sleep);
		}

		@Override
		public void run() {
			tr = new ExampleResult();
			try {
				double r = new Random().nextDouble();
				System.out.println("Sleeping for " + (r*sleepTime)/1000 + " seconds");
				Thread.sleep((long)(sleepTime*r));
				
			} catch (InterruptedException e) {}
			
			setResult(tr);
			//System.exit(-1);
		}
	}
	
	/**
	 * @author bjlawry
	 * <p>Simple result implementation.  Each task instance must have a
	 * corresponding result instance that gets set at the end of execution.
	 */
	public static class ExampleResult extends ParallelResult{
		private static final long serialVersionUID = 1L;
		private String result;

		public ExampleResult(){
			String hostname = null;
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				hostname = "UNKNOWN";
			}
			result = "\"Hello, World\" from  "+hostname;
		}
		
		@Override
		public String toString(){ return result; }
	}
}

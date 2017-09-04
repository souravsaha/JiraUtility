/**
 *
 */
package application.logging;

import java.util.Map;

/**
 *
 *
 * This class is only for test
 * @author raparash
 *
 */
public class LogProducer implements Runnable{

	private Map<Integer,String> mapOfLogs;
	private int sizeIndicator;

	public LogProducer(Map<Integer,String> map,int sizeIndicator){
		this.mapOfLogs=map;
		this.sizeIndicator=sizeIndicator;
	}

	public void writeLogs(Integer key,String value) throws InterruptedException{
		synchronized (mapOfLogs) {

			while(mapOfLogs.size()==sizeIndicator){
				mapOfLogs.wait();
			}

			System.out.println("produced: <"+key+","+value+">");
			mapOfLogs.put(key, value);
			mapOfLogs.notifyAll();
		}
	}
	@Override
	public void run() {

		for(int i=1;i<=20;i++){
			try {
				writeLogs(Integer.valueOf(i),"TestLog"+i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}

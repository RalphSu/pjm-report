/**
 * 
 */
package org.pjm2.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author liasu
 *
 */
public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
		ReportGenerator generator = new ReportGenerator();
		generator.startLoop();
		while(true) {
			try {
				Thread.sleep(5000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error("Report thread waiting interrupted, ignore this waiting and wait again.", e);
			}
		}
	}
}

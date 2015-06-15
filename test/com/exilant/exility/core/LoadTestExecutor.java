/* *******************************************************************************************************
Copyright (c) 2015 EXILANT Technologies Private Limited

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ******************************************************************************************************** */
package com.exilant.exility.core;

/*
 * TestExecutor.java
 *
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class uses Executor Service for running test case processor is different
 * threads
 */
public class LoadTestExecutor {
	static ExecutorService executorService;
	private static final int HANDLERS = 5;

	/**
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {

		test();
	}

	/**
 * 
 */
	public static void test() {
		// exe = Executors.newCachedThreadPool() ;

		executorService = Executors.newFixedThreadPool(HANDLERS);
		for (int i = 0; i < 1000; i++) {
			LoadHandler test = new LoadHandler();
			executorService.execute(test);
			// exe.submit(test1);
		}
		shutdownAndAwaitTermination(executorService);
	}

	static void shutdownAndAwaitTermination(ExecutorService exService) {
		exService.shutdown();
		try {
			if (!exService.awaitTermination(60, TimeUnit.SECONDS)) {
				exService.shutdownNow();
				if (!exService.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			exService.shutdownNow();
			Thread.currentThread().interrupt();
		}

		while (!exService.isTerminated()) {
			// TODO: ?????????????????????????????? why would any one get away
			// with such a code
		}
		System.out.println("Threads completed");

	}

}
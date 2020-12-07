package org.rjo.chess.uci;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.rjo.chess.eval.SearchStrategy;

public class UciReporter implements Runnable {

	private final long SLEEP_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);
	private boolean stop;
	private SearchStrategy strategy;
	private PrintStream outputStream;

	public UciReporter(SearchStrategy strategy, PrintStream outputStream) {
		this.strategy = strategy;
		this.outputStream = outputStream;
	}

	@Override
	public void run() {
		stop = false;
		reportProgress();
	}

	private void reportProgress() {
		long start = System.currentTimeMillis();
		while (!stop) {
			try {
				Thread.sleep(SLEEP_INTERVAL_MS);
				int nbrNodes = strategy.getCurrentNbrNodesSearched();
				double timeRunningInSeconds = (System.currentTimeMillis() - start) / SLEEP_INTERVAL_MS;
				long nps = Math.round(nbrNodes / timeRunningInSeconds);
				outputStream.println("info nodes " + nbrNodes + " nps " + nps);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

}

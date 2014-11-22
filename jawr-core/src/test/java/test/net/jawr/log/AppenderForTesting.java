package test.net.jawr.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class AppenderForTesting extends AppenderSkeleton {

	private static List<String> LOGS = new ArrayList<String>();

	protected void append(LoggingEvent event) {
		LOGS.add(event.getRenderedMessage());
	}

	public void close() {
	}

	public boolean requiresLayout() {
		return false;
	}

	public static String[] getMessages() {
		return (String[]) LOGS.toArray(new String[LOGS.size()]);
	}

	public static void clear() {
		LOGS.clear();
	}
}

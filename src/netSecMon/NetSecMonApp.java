package netSecMon;


import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetSecMonApp {

	private final NetSecMonGUI gui;
	private ConnectionsManager manager;
	private final ExecutorService managerThread = Executors.newSingleThreadExecutor();
	private NetSecMonApp() {
		gui = new NetSecMonGUI();
		gui.setAppInstance(this);
		gui.setVisible(true);
	}
	public void loadThreadManager() {
		manager = new ConnectionsManager();
		gui.setConnectionsManager(manager);
		managerThread.submit(manager);
	}

	public void shutDown() {
		manager.stopPolling();
		managerThread.shutdownNow();
		gui.setVisible(false);
		gui.dispose();
		System.exit(0);
	}
	public static void main(String[] args)
	{
		new NetSecMonApp();
		
	}
}

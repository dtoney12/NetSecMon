package netSecMon;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
public class NetSecMonApp {
	private final NetSecMonGUI gui;
	private ConnectionsManager manager;
	private final ExecutorService managerThread = Executors.newSingleThreadExecutor();
	public NetSecMonApp() {
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
		if (manager != null) manager.stopPolling();
		managerThread.shutdownNow();
		gui.setVisible(false);
		gui.dispose();
		System.exit(0);
	}
	public static void main(String[] args)
	{
		SpringApplicationBuilder builder = new SpringApplicationBuilder(NetSecMonApp.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		
	}
}

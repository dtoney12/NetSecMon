package netSecMon;


import netSecMon.attempt.AttemptService;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final AttemptService dbService;
	@Autowired
	public NetSecMonApp(AttemptService attemptService) {
		gui = new NetSecMonGUI(attemptService);
		gui.setAppInstance(this);
		gui.setVisible(true);
		this.dbService = attemptService;
	}
	public void loadThreadManager() {
		manager = new ConnectionsManager(dbService);
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

package netSecMon;


public class NetSecMonApp {

	private final NetSecMonGUI gui;
	
	private NetSecMonApp() {	
		gui = new NetSecMonGUI();
		gui.setAppInstance(this);
		gui.setVisible(true);
	}
	
	public void setManager() {
		netSecMon.ConnectionsManager manager;

		manager = new netSecMon.ConnectionsManager();
		gui.setConnectionsManager(manager);
		gui.getJobsBox().removeAll();
		gui.resetButtons();
		manager.setJobsBox(gui.getJobsBox());
		manager.setLogField(gui.getLogField());
	}

	
	public static void main(String[] args)
	{
		new NetSecMonApp();
		
	}
}

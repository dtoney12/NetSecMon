
/*

The NetSecMonGUI class launches the GUI for the NetSecMon application


*/

package netSecMon;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;



public class NetSecMonGUI extends JFrame {

   	final String[] searchTypes = {"URL", "IP"};
   	final Integer[] pollIntervals = {10,30,60,120,300,1200,3600,10800,86400};
   	final String[] displayOrderTypes = {"Most Recent", "Failed", "Pending", "Alphabetical", "IP First"};
   	File file;
   	JTextField filePathTextField = new JTextField(20);
	JTextArea log;							
	Box jobsBox;
	JComboBox<Integer> pollIntervalBox;
	JButton startButton;
	JButton stopButton;
	ConnectionsManager manager;
	NetSecMonApp app;
	ArrayList<URL> urls = new ArrayList<>();
	

	public NetSecMonGUI() {
		super("NetSecMon");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (getWidth() / 2 ), 
		                              middle.y - (getHeight() / 2 ));
		setLocation(newLocation);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
//		             __________________________________________________________________
//                   |                        Menu Bar                                |
//                   |  Program   Report  Help                                        |
//                   |________________________________________________________________|
//		  TopPanel   |                        SearchBox                               |
//		             |________________________________________________________________|
//		            /|                                                                |
//		           / |                        CenterPanel                             |
//		          /  |                                         |                      |
//		MainPanel{   |  JobsBoxPanel                           |      ControlPanel    |
//		          \  |-----------------------------------------------------------------
//		           \ |                                                                |
//		            \|                             logPanel                           |
//		             |                                                                |
//                   |                                                                |
//		             |________________________________________________________________|

		

// Menu bar
		JMenuBar menu = new JMenuBar();
		JMenu programMenu = new JMenu("Program");
		JMenu reportMenu = new JMenu("Report");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem openFile = new JMenuItem("Open file");
		JMenuItem quitProgram = new JMenuItem("Quit");
		openFile.addActionListener(e -> chooseFile());
		quitProgram.addActionListener(e -> app.shutDown());
		programMenu.add(openFile);
		programMenu.add(quitProgram);
		reportMenu.add(new JMenuItem("Generate Report"));
		helpMenu.add(new JMenuItem("About"));
		menu.add(programMenu);
		menu.add(reportMenu);
		menu.add(helpMenu);
		this.setJMenuBar(menu);

//  TopPanel
		JPanel topPanel = new JPanel();
		JComboBox<String> searchBox = new JComboBox<>(searchTypes);
		JTextField searchText = new JTextField(40);
		JButton searchButton = new JButton("Search");
		topPanel.add(searchBox);
		topPanel.add(searchText);
		topPanel.add(searchButton);

		
// mainPanel, includes center (resource, control, and jobs panels) and log panel (on bottom)
		
    // CenterPanel

		// JobsBoxPanel
		jobsBox = Box.createVerticalBox();
		JScrollPane jobsBoxScrollPane = new JScrollPane(jobsBox);
		jobsBoxScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jobsBoxScrollPane.getVerticalScrollBar().setUnitIncrement(30);
		JPanel jobsBoxPanel = new JPanel(new BorderLayout());
		jobsBoxPanel.setBorder(new TitledBorder("Jobs in Progress"));
		jobsBoxPanel.add(jobsBoxScrollPane);
		jobsBoxPanel.setPreferredSize(new Dimension(1000,500));


		// ControlPanel
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		controlPanel.setBorder(new TitledBorder("Control"));

		JPanel controlTopPanel = new JPanel();
		controlTopPanel.setLayout(new GridLayout());
		startButton = new JButton("Start");
		startButton.setOpaque(true);
		controlTopPanel.add(startButton);
		stopButton = new JButton("Stop");
		stopButton.setOpaque(true);
		controlTopPanel.add(stopButton);

		JPanel displayOrderPanel = new JPanel();
		displayOrderPanel.setBorder(new TitledBorder("Order"));
		JComboBox<String> displayOrderBox = new JComboBox<>(displayOrderTypes);
		displayOrderPanel.add(displayOrderBox);

		JPanel pollIntervalPanel = new JPanel();
		pollIntervalPanel .setBorder(new TitledBorder("Poll (seconds)"));
		pollIntervalBox = new JComboBox<>(pollIntervals);
		pollIntervalBox.setSelectedIndex(3);

		pollIntervalPanel .add(pollIntervalBox);

		JPanel controlBottomPanel = new JPanel();
		controlBottomPanel.add(displayOrderPanel);
		controlBottomPanel.add(pollIntervalPanel);

		controlPanel.add(controlTopPanel, BorderLayout.NORTH);
		controlPanel.add(controlBottomPanel, BorderLayout.SOUTH);

		// centerPanel add controlPanel and JobsBoxPanel
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(jobsBoxPanel, BorderLayout.WEST);
		centerPanel.add(controlPanel, BorderLayout.EAST);



	// LogPanel
		JPanel logPanel = new JPanel();
		logPanel.setBorder(new TitledBorder("LOG"));
		log = new JTextArea(20,100); 
		JScrollPane logScrollPane = new JScrollPane(log);
		logPanel.add(logScrollPane, BorderLayout.CENTER);
		DefaultCaret caret = (DefaultCaret) log.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		// mainPanel add centerPanel and logPanel
		JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, logPanel);
		mainPanel.setOneTouchExpandable(true);
		mainPanel.setDividerLocation(250);
		
		
// frame add panels
		add(topPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		pack();
		
//		control action listeners
		startButton.addActionListener( e -> {
			if (manager != null) {
				manager.startPolling();
			}
		} );

		stopButton.addActionListener( e -> {
			if (manager != null) {
				manager.stopPolling();
			}
		} );

		pollIntervalBox.addActionListener( e-> {
			if (manager != null) {
				manager.setPollingInterval((Integer) pollIntervalBox.getSelectedItem());
			}
		});
		
		// listener to perform search
//		searchButton.addActionListener(e -> search());
	}
	public void setConnectionsManager(ConnectionsManager connectionsManager) {
		manager = connectionsManager;
		manager.setJobsBox(jobsBox);
		manager.setLogField(log);
		manager.setUrls(urls);
		manager.setPollingInterval((Integer) pollIntervalBox.getSelectedItem());
	}
	public void setAppInstance(NetSecMonApp thisApp) {
		app = thisApp;
	}
	// Use file dialog to select file.
	private void chooseFile() {
		try {
			JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
			int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
            	file = chooser.getSelectedFile();
    			filePathTextField.setText(file.getAbsolutePath());
            	readFile();
            }
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}
	
	private void readFile() throws FileNotFoundException {
		urls.clear();
		Scanner sc = new Scanner(new BufferedReader(new FileReader(file)));
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.length() == 0 || line.charAt(0) == '/') {  // skip comments
				continue;
			}
			Scanner scLine = new Scanner(line);
			String keyWord = scLine.next();
			parseHTTPS(keyWord);
			scLine.close();
		}
		sc.close();

		app.loadThreadManager();
	}
	
	
	private void parseHTTPS(String urlString) {
		URL url;
		String preparsedString = "";
		  try {
			  preparsedString = preparse(urlString);
			  url = new URL(preparsedString);
			  urls.add(url);
			  System.out.println("\nURL ADDED: " + preparsedString +'\n');
			  log.append("\nURL ADDED: " + preparsedString +'\n');
		     
		  } catch (MalformedURLException e) {
			  System.out.println('\n' + "COULD NOT FORM URL: " + urlString + " || Preparsed output: " + preparsedString + "  -- -> java.net.MalformedURLException() ");
		  }
	}
	
	private String preparse(String targetUrlString) {
		System.out.println('\n');
		if (targetUrlString.charAt(0) == '*') {
			log.append("\nWILL PREPARSE URL: " + targetUrlString);
			System.out.println("\nWILL PREPARSE URL: " + targetUrlString);
			// replace without "*." at the beginning of the url
			int targetUrlStringLength = targetUrlString.length();
			targetUrlString = targetUrlString.substring(2, targetUrlStringLength);
			
		}
		targetUrlString = "https://" + targetUrlString;
		return targetUrlString;
	}
}

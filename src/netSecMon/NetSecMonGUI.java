
/*
 * 
* The NetSecMonGUI class launches the GUI for the NetSecMon application
* 
* @input

* 
* @exception 
*
* @author  Dale Toney
* @version 1.0
* @since   2019/3/28

*/

package netSecMon;

import java.awt.*;
import java.awt.event.*;  
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;


@SuppressWarnings("serial")
public class NetSecMonGUI extends JFrame {
// CONSTANTS for JFrame
//		final int minWIDTH = 1300, minHEIGHT = 500;
//   	final int prefWIDTH = 1500, prefHEIGHT = 800;
//   	final Dimension minimumSizeFrame = new Dimension(minWIDTH, minHEIGHT);
//   	final Dimension preferredSizeFrame = new Dimension(prefWIDTH, prefHEIGHT);
//   	final Dimension preferredSizeTopPanel = new Dimension(1000, 500);
//   	final Dimension minimumSizeCenterPanel = new Dimension(1000, 100);
   	final String[] searchTypes = {"URL", "IP"};
   	final Integer[] pollIntervals = {1,5,30,60,120,300,1200,3600};
   	final String[] displayOrderTypes = {"Most Recent", "Failed", "Pending", "Alphabetical", "IP First"};
   	File file;
   	JTextField filePathTextField = new JTextField(20);
	JTextArea log;							
	Box jobsBox;
	JButton pauseButton;
	JButton stopButton;
	Boolean paused = false;
	Boolean stopped = false;
	ConnectionsManager manager;
	Thread managerThread;
	NetSecMonApp app;
	

	public NetSecMonGUI() {
		super("NetSecMon");

//		setMinimumSize(minimumSizeFrame);   
//		setPreferredSize(preferredSizeFrame);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (getWidth() / 2 ), 
		                              middle.y - (getHeight() / 2 ));
		setLocation(newLocation);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
//		             __________________________________________________________________
//                   |                        Menu Bar                                |
//                   |  File   Report  Help                                           |
//                   |________________________________________________________________|
//		            /|                                                                |
//		           / |                                                                |
//		TopPanel  {  |                                                                |
//		           \ |        SearchPanel            |       DisplayOrderPanel        |
//		            \|________________________________________________________________|
//		            /|                                                                | 
//		           / |                             CenterPanel                        |
//		          /  |                                                                |
//		MainPanel{   |  ResourcesOutterPanel  |    ControlPanel    |  JobsBoxPanel    |
//		          \  |-----------------------------------------------------------------
//		           \ |                                                                |
//		            \|                             logPanel                           |
//		             |                                                                |
//                   |                                                                |
//		             |________________________________________________________________|

		

// Menu bar
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu reportMenu = new JMenu("Report");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem openFile = new JMenuItem("Open file");
		openFile.addActionListener(e -> chooseFile());
		fileMenu.add(openFile);
		reportMenu.add(new JMenuItem("Generate Report"));
		helpMenu.add(new JMenuItem("About"));
		menu.add(fileMenu);
		menu.add(reportMenu);
		menu.add(helpMenu);
		this.setJMenuBar(menu);

//  TopPanel
		
		// SearchPanel, include comboBox, text field, and search button
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BorderLayout());
		JPanel searchTopPanel = new JPanel();
		searchPanel.add(searchTopPanel, BorderLayout.NORTH);
		searchTopPanel.setBorder(new TitledBorder("Search Target"));
		JComboBox<String> searchBox = new JComboBox<String>(searchTypes);
		searchTopPanel.add(searchBox);
		JTextField searchText = new JTextField(20);
		searchTopPanel.add(searchText);
		JButton searchButton = new JButton("Search");
		searchTopPanel.add(searchButton);
						
		// DisplayOrderPanel
		JPanel displayOrderPanel = new JPanel();
		displayOrderPanel.setBorder(new TitledBorder("Display Order"));
		JComboBox<String> displayOrderBox = new JComboBox<String>(displayOrderTypes);
		displayOrderPanel.add(displayOrderBox);
		
		// topPanel 
		JPanel topPanel = new JPanel();
//		topPanel.setMinimumSize(preferredSizeTopPanel);
		topPanel.setLayout(new GridLayout(1,2));
		topPanel.add(searchPanel);
		topPanel.add(displayOrderPanel);
		
		
// mainPanel, includes center (resource, control, and jobs panels) and log panel (on bottom)
		
    // CenterPanel
		
		// ResourcesOutterPanel
		JPanel resourcesPanel = new JPanel();
		resourcesPanel.setLayout(new BorderLayout());
//		resourcesPanel.setMinimumSize(new Dimension(240,500));
		resourcesPanel.setBackground(Color.WHITE);
		JScrollPane resourcesPanelScrollPane = new JScrollPane(resourcesPanel); 
		resourcesPanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel resourcesOutterPanel = new JPanel(new BorderLayout());
		resourcesOutterPanel.setBorder(new TitledBorder("Resources View"));
		resourcesOutterPanel.add(resourcesPanelScrollPane);
		resourcesOutterPanel.setPreferredSize(new Dimension(240,500));
		
		// ControlPanel
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		controlPanel.setBorder(new TitledBorder("Control"));
		JPanel controlTopPanel = new JPanel();
		controlTopPanel.setLayout(new BorderLayout());
		pauseButton = new JButton("Pause");
		pauseButton.setOpaque(true);
		pauseButton.setBackground(Color.GREEN);
		controlTopPanel.add(pauseButton, BorderLayout.NORTH);
		stopButton = new JButton("Stop");
		stopButton.setOpaque(true);
		stopButton.setBackground(null);
		controlTopPanel.add(stopButton, BorderLayout.SOUTH);
		
		JPanel controlBottomPanel = new JPanel();
		controlBottomPanel.setBorder(new TitledBorder("Polling Interval"));
		JComboBox<Integer> pollIntervalBox = new JComboBox<Integer>(pollIntervals);
		controlBottomPanel.add(pollIntervalBox);
		
		controlPanel.add(controlTopPanel, BorderLayout.NORTH);
		controlPanel.add(controlBottomPanel, BorderLayout.SOUTH);
		controlPanel.setPreferredSize(new Dimension(130,200));
		
		// JobsBoxPanel
		jobsBox = Box.createVerticalBox();
		JScrollPane jobsBoxScrollPane = new JScrollPane(jobsBox);
		jobsBoxScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jobsBoxScrollPane.getVerticalScrollBar().setUnitIncrement(30);
		JPanel jobsBoxPanel = new JPanel(new BorderLayout());
		jobsBoxPanel.setBorder(new TitledBorder("Jobs in Progress"));
		jobsBoxPanel.add(jobsBoxScrollPane);
		jobsBoxPanel.setPreferredSize(new Dimension(1000,500));
		

		
		// centerPanel add ResourcesOutterPanel and JobsBoxPanel
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
//		centerPanel.setMinimumSize(minimumSizeCenterPanel);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(resourcesOutterPanel, BorderLayout.WEST);
		centerPanel.add(controlPanel, BorderLayout.CENTER);
		centerPanel.add(jobsBoxPanel, BorderLayout.EAST);


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
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){ 
				if (paused) {
					pauseButton.setBackground(Color.GREEN);
					pauseButton.setText("Pause");
				} else {
					pauseButton.setBackground(Color.ORANGE);
					pauseButton.setText("Paused");
				}
				paused = !(paused);
				manager.pause();
			}
		});
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){ 
				if (stopped) {
//					stopButton.setBackground(Color.GREEN);
//					stopButton.setText("Stop");
//					Thread managerThread = new Thread(manager);
//					managerThread.start();
				} else {
					stopButton.setBackground(Color.RED);
					stopButton.setText("Stopped");
					manager.stop();
				}
				stopped = !(stopped);
			}
		});
		
		// listener to perform search
//		searchButton.addActionListener(e -> search());
		
	}
	
	public void setConnectionsManager(ConnectionsManager connectionsManager) {
		manager = connectionsManager;
	}
	public void resetButtons() {
		pauseButton.setBackground(Color.GREEN);
		pauseButton.setText("Pause");
		paused = false;
		stopButton.setBackground(Color.RED);
		stopButton.setText("Stop");
		stopped = false;
	}
	public void setAppInstance(NetSecMonApp thisApp) {
		app = thisApp;
	}
	public JTextArea getLogField() {
		return log;
	}
	public Box getJobsBox() {
		return jobsBox;
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
		app.setManager();
		
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
		
		managerThread = new Thread(manager);
		managerThread.start();
	}
	
	
	private void parseHTTPS(String urlString) {
		URL url;
		String preparsedString = "";
		  try {
			  preparsedString = preparse(urlString);
			  url = new URL(preparsedString);
			  manager.urls.add(url);
			  System.out.println("\nURL ADDED: " + preparsedString +'\n');
			  log.append("\nURL ADDED: " + preparsedString +'\n');
		     
		  } catch (MalformedURLException e) {
			  System.out.println('\n'  + "COULD NOT FORM URL: " + urlString + " || Preparsed output: " + preparsedString + "  -- -> java.net.MalformedURLException() ");
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

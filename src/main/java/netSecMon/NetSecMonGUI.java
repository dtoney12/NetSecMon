
/*

The NetSecMonGUI class launches the GUI for the NetSecMon application


*/

package netSecMon;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.JTextComponent;


public class NetSecMonGUI extends JFrame {

	final Integer[] pollIntervals = {5, 10, 30, 60, 120, 300, 1200, 3600, 10800, 86400};
	final String[] displayOrderTypes = {"Most Recent", "Failed", "Pending", "Alphabetical", "IP Address"};
	File file;
	JTextField filePathTextField = new JTextField(20);
	JTextArea log;
	JPanel jobsBox;

	JScrollBar scrollBar;
	JComboBox<Integer> pollIntervalBox;
	JButton startButton;
	JButton stopButton;
	JButton searchButton;
	JComboBox<String> searchBox;
	ArrayList<Result> results;

	Vector<String> searchBoxItems = new Vector<>();
	DefaultComboBoxModel<String> searchBoxModel = new DefaultComboBoxModel<>(searchBoxItems);
	ConnectionsManager manager;
	NetSecMonApp app;
	ArrayList<URL> urls = new ArrayList<>();


	public NetSecMonGUI() {
		super("NetSecMon");
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
		quitProgram.addActionListener(e -> app.shutDown());
		programMenu.add(openFile);
		programMenu.add(quitProgram);
		JMenuItem reportGenerate = new JMenuItem("Generate Report");
		reportMenu.add(reportGenerate);
		helpMenu.add(new JMenuItem("About"));
		menu.add(programMenu);
		menu.add(reportMenu);
		menu.add(helpMenu);
		this.setJMenuBar(menu);

//  TopPanel
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		searchBox = new JComboBox<>(searchBoxModel);
		searchBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		searchBox.setEditable(true);
		searchButton = new JButton("Search");
		topPanel.add(searchBox);
		topPanel.add(searchButton);


// mainPanel, includes center (resource, control, and jobs panels) and log panel (on bottom)

		// CenterPanel

		// JobsBoxPanel
		jobsBox = new JPanel();
		jobsBox.setLayout(new BoxLayout(jobsBox, BoxLayout.Y_AXIS));
		JScrollPane jobsBoxScrollPane = new JScrollPane(jobsBox);
		scrollBar = jobsBoxScrollPane.getVerticalScrollBar();
		jobsBoxScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jobsBoxScrollPane.getVerticalScrollBar().setUnitIncrement(30);
		jobsBoxScrollPane.setPreferredSize(new Dimension(960, 500));
		JPanel jobsBoxPanel = new JPanel();
		jobsBoxPanel.setBorder(new TitledBorder("Jobs in Progress"));
		jobsBoxPanel.add(jobsBoxScrollPane);
		jobsBoxPanel.setPreferredSize(new Dimension(980, 0));

		// ControlPanel
		JPanel controlPanel = new JPanel(new BorderLayout());
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
		pollIntervalPanel.setBorder(new TitledBorder("Poll (seconds)"));
		pollIntervalBox = new JComboBox<>(pollIntervals);
		pollIntervalBox.setSelectedIndex(2);

		pollIntervalPanel.add(pollIntervalBox);

		JPanel controlBottomPanel = new JPanel();
		controlBottomPanel.add(displayOrderPanel);
		controlBottomPanel.add(pollIntervalPanel);

		controlPanel.add(controlTopPanel, BorderLayout.NORTH);
		controlPanel.add(controlBottomPanel, BorderLayout.SOUTH);

		// centerPanel add controlPanel and JobsBoxPanel
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(jobsBoxPanel, BorderLayout.WEST);
		centerPanel.add(controlPanel, BorderLayout.EAST);


		// LogPanel
		JPanel logPanel = new JPanel();
		logPanel.setBorder(new TitledBorder("LOG"));
		log = new JTextArea(20, 100);
		JScrollPane logScrollPane = new JScrollPane(log);
		logPanel.setLayout(new BorderLayout());
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
		openFile.addActionListener(e -> chooseFile());
		reportGenerate.addActionListener(e -> TestDB());

		searchButton.addActionListener(e -> {
			int connectionsIndex = (results.get(searchBox.getSelectedIndex()).index);
			scrollBar.setValue(connectionsIndex * 31);
			System.out.println("ConnectionsIndex of : " + results.get(searchBox.getSelectedIndex()).hostURL + " = " + connectionsIndex);
			jobsBox.validate();
			jobsBox.repaint();
		});

		// updating searchBox dropdown matches dynamically requires access to underlying document of the combo box
		ComboBoxEditor searchBoxEditor = searchBox.getEditor();
		((JTextComponent) searchBoxEditor.getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				results = getMatchResults((String) searchBoxEditor.getItem());
				String[] resultsArray = new String[results.size()];
				for (int i=0; i < results.size(); i++) {
					resultsArray[i] = results.get(i).hostURL;
				}
				SwingUtilities.invokeLater(() -> {
					searchBoxItems.clear();
					Collections.addAll(searchBoxItems, resultsArray);
					searchBox.setPopupVisible(false);
					searchBox.setPopupVisible(true);
				});
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				results = getMatchResults((String) searchBoxEditor.getItem());
				String[] resultsArray = new String[results.size()];
				for (int i=0; i < results.size(); i++) {
					resultsArray[i] = results.get(i).hostURL;
				}
				SwingUtilities.invokeLater(() -> {
					searchBoxItems.clear();
					Collections.addAll(searchBoxItems, resultsArray);
					searchBox.setPopupVisible(false);
					searchBox.setPopupVisible(true);
				});
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});

		startButton.addActionListener(e -> {
			if (manager != null) {
				manager.startPolling();
			}
		});

		stopButton.addActionListener(e -> {
			if (manager != null) {
				manager.stopPolling();
			}
		});

		pollIntervalBox.addActionListener(e -> {
			if (manager != null) {
				manager.setPollingInterval((Integer) pollIntervalBox.getSelectedItem());
			}
		});

		displayOrderBox.addActionListener(e -> {
			if (manager != null) {
				manager.sortBy((String) displayOrderBox.getSelectedItem());
			}
		});
		// listener to perform search
//		searchButton.addActionListener(e -> search());
	}

	public ArrayList<Result> getMatchResults(String input) {
		int minMatchRatio = 0;
		ArrayList<Result> results = new ArrayList<>();

		for (int index=0; index < manager.connections.size(); index++) {
			HttpsConnection connection = manager.connections.get(index);
			int Fuzzymatchvalue = FuzzySearch.ratio(input, connection.url.getHost());
			if (Fuzzymatchvalue > Integer.max(50, minMatchRatio)) {
				results.add(new Result(Fuzzymatchvalue, connection.url.getHost(), index));
				minMatchRatio = Fuzzymatchvalue;
			}
		}
		results.sort(Result::compareTo);
		return results;
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
			if (returnVal == JFileChooser.APPROVE_OPTION) {
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
		} catch (MalformedURLException e) {
			System.out.println('\n' + "COULD NOT FORM URL: " + urlString + " || Preparsed output: " + preparsedString + "  -- -> java.net.MalformedURLException() ");
		}
	}

	private String preparse(String targetUrlString) {
		System.out.println('\n');
		if (targetUrlString.charAt(0) == '*') {
//			log.append("\nWILL PREPARSE URL: " + targetUrlString);
//			System.out.println("\nWILL PREPARSE URL: " + targetUrlString);

			// replace without "*." at the beginning of the url
			int targetUrlStringLength = targetUrlString.length();
			targetUrlString = targetUrlString.substring(2, targetUrlStringLength);

		}
		targetUrlString = "https://" + targetUrlString;
		return targetUrlString;
	}

	private void TestDB() {
		try {
			Connection Conn = DriverManager.getConnection
					("jdbc:mysql://127.0.0.1:3306/?user=root&password=Welcome2");
			Statement s = Conn.createStatement();
			s.executeUpdate("DROP DATABASE IF EXISTS TestDB");
			int result = s.executeUpdate("CREATE DATABASE TestDB");
			if (result != 0) {
				System.out.println("\n SUCCESS in CREATING TESTDB");
			} else System.out.println("\n FAILURE in CREATING TESTDB");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private class Result implements Comparable<Result> {
		private int matchRatio;
		public String hostURL;
		public int index;

		private Result(int ratio, String url, int i) {
			this.matchRatio = ratio;
			this.hostURL = url;
			this.index = i;
		}
		@Override
		public int compareTo(Result other) {
			return Integer.compare(this.matchRatio, other.matchRatio);
		}
	}
}

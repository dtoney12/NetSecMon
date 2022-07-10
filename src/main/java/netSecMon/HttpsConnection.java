
/*

* The HttpsConnection class implements a Runnable and instantiates a Https Connection

*/


package netSecMon;


import netSecMon.attempt.Attempt;
import netSecMon.attempt.AttemptService;

import java.awt.*;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.Principal;
import java.io.*;

import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import java.security.cert.X509Certificate;
//import javax.net.ssl.SSLContext;
//import java.security.NoSuchAlgorithmException;
//import java.security.KeyManagementException;
//
//import javax.net.ssl.SSLSession;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;


public class HttpsConnection extends JPanel implements Runnable {
		
	URL url;
	String ipAddress = "";
	HttpsURLConnection con;
	ConnectionsManager manager;
	AttemptService dbservice;
	ScheduledFuture futureTask;
	JButton buttonDelete;
	JLabel urlLabel;
	JProgressBar barProgress;
    JButton buttonPause;
	public JButton buttonTakeOffline;
	public Boolean isOffline = false;
	JButton buttonRestart;
	JButton buttonCertShow;
	Status status = Status.AWAITING_RESOURCE;
	JTextArea log;
	String msg;
	DateTimeFormatter timeStampFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SS");
	public HttpsConnection(URL urlObject) {
		url = urlObject;
		this.setPreferredSize(new Dimension(900,40));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		buttonDelete = new JButton("Delete");
		buttonDelete.setPreferredSize(new Dimension(100,20));
		buttonCertShow = new JButton("Show Cert");
		buttonCertShow.setPreferredSize(new Dimension(100,20));
		barProgress = new JProgressBar();
		barProgress.setStringPainted (true);
		buttonPause = new JButton("Pause");
		buttonPause.setPreferredSize(new Dimension(170,20));
		buttonPause.setOpaque(true);
		buttonPause.setBorderPainted(false);
		buttonTakeOffline = new JButton("Take Offline");
		buttonTakeOffline.setPreferredSize(new Dimension(130,20));
		buttonRestart = new JButton("Restart");
		buttonRestart.setPreferredSize(new Dimension(100,20));
		urlLabel = new JLabel("<html>" + url.getHost() + "</html>",SwingConstants.LEFT);
		this.add(buttonDelete);
		this.add(buttonCertShow);
		this.add(urlLabel);
		this.add(buttonPause);
		this.add(buttonTakeOffline);
		this.add(barProgress);
		this.add(buttonRestart);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		this.setBackground(new Color(209, 223, 250));
		status = Status.RUNNING;
		showStatus();
		buttonTakeOffline.addActionListener( e -> takeOffline());
		buttonRestart.addActionListener( e -> manager.restartTask(this));
		buttonDelete.addActionListener( e -> manager.deleteConnection(this));
		buttonCertShow.addActionListener(e -> showCert());
	}
    void setLogField(JTextArea logTextArea) {
		log = logTextArea;
	}
	public void setManager(ConnectionsManager connectionmanager) {
		manager = connectionmanager;
	}
	public void setAttemptService(AttemptService attemptService) {
		dbservice = attemptService;
	}
	public void resolveHostIP() {
		try {
			ipAddress = InetAddress.getByName(url.getHost()).getHostAddress();
		} catch (UnknownHostException e) {
			log.append("\n" + e.getMessage());
			System.out.println("\n"+ e.getMessage());
		}
		urlLabel.setText("<html>" + url.getHost() + "<br/>" + ipAddress + "</html>");
	}

	public void setFutureTask(ScheduledFuture task) {
		cancelFutureTask();
		futureTask = task;
	}
	public void cancelFutureTask() {
		if (futureTask != null) {
			futureTask.cancel(true);
		}
	}
	public void takeOffline() {
		cancelFutureTask();
		buttonTakeOffline.setEnabled(false);
		this.setBackground(Color.gray);
		this.isOffline = true;
	}
	private String getTime() {
		return LocalDateTime.now().format(timeStampFormat);
	}
	public void run() {
		try {
			status = Status.RUNNING;
			showStatus();
			barProgress.setValue(0);
			msg = "\n" + getTime() + " " + url.toString() + " || OPENING CONNECTION ";
			log.append(msg);
			System.out.println(msg);
			con = (HttpsURLConnection) url.openConnection();

			msg = "\n" + getTime() + " " + url.toString() + " || ATTEMPTING CONNECTION ";
			log.append(msg);
			System.out.println(msg);
			con.connect();
			barProgress.setValue(100);
			status = Status.CONNECTED;
			showStatus();
		}
//		catch (sun.security.validator.ValidatorException e) {
//	    	String msg = "\n" + url.toString() + " || COULD NOT CONNECT || ValidatorException :  ----> (HttpsConnection.con.connect() -> sun.security.validator.ValidatorException() ";
//			log.append(msg);
//			System.out.println(msg);
//	    	showStatus(Status.VALIDATOREXCEPTION);
//	    }
		catch (java.net.ConnectException e) {
			String msg = "\n" + getTime() + " " + url.toString() + " || COULD NOT CONNECT || ConnectException :  ----> (HttpsConnection.con.connect() -> java.net.ConnectException() ";
			log.append(msg);
			System.out.println(msg);
			status = Status.CONNECTED;
			showStatus();
		} catch (java.net.SocketException e) {
			String msg = "\n" + getTime() + " " + url.toString() + " || COULD NOT CONNECT || SocketException :  ----> (HttpsConnection.con.connect() -> socketException() ";
			log.append(msg);
			System.out.println(msg);
			status = Status.SOCKETEXCEPTION;
			showStatus();
		} catch (IOException e) {
			String msg = "\n" + getTime() + " " + url.toString() + " || COULD NOT CONNECT || IOException ";
			log.append(msg);
			System.out.println(msg);
			status = Status.IOEXCEPTION;
			showStatus();
		}
		log.append("\n");
		System.out.println("\n");
		dbservice.addAttempt(new Attempt(url.getHost(), LocalDateTime.now().format(timeStampFormat), status));
		//print all the content
		//printPayload(con);
	}

	void showStatus () {
        switch (status) {
        	case LOADING:
			case WAITING:
				break;
	        case RUNNING:
	            buttonPause.setBackground (Color.green);
	            buttonPause.setText ("Running");
	            break;
	        case SUSPENDED:
	            buttonPause.setBackground (Color.yellow);
	            buttonPause.setText ("Suspended");
	            break;
	        case IN_WAITQUEUE:
	            buttonPause.setBackground (Color.orange);
	            buttonPause.setText ("In Wait Queue");
	            break;
	        case AWAITING_RESOURCE:
	            buttonPause.setBackground (Color.orange);
	            buttonPause.setText ("Awaiting Resource");
	            break;
	        case CONNECTED:
	            buttonPause.setBackground (Color.cyan);
	            buttonPause.setText ("Connected");
	            break;
	        case IN_QUEUE:
	            buttonPause.setBackground (Color.gray);
	            buttonPause.setText ("In Queue");
	            break;
	        case RESOURCE_UNAVAILABLE:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("UNMATCHED SKILL REQ");
            	break;
	        case IOEXCEPTION:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("COULD NOT CONNECT");
            	break;
	        case IOEXCEPTION_GET:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("IOEXCEPTION_GET");
            	break;
	        case CONNECTEXCEPTION:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("CONNECTEXCEPTION");
            	break;
	        case SOCKETEXCEPTION:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("SOCKETEXCEPTION");
            	break;
	        case SSLPEERUNVERIFIEDEXCEPTION:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("SSLPEERUNVERIFIEDEXCEPTION");
            	break;
	        case SSLHANDSHAKEEXCEPTION:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("SSLHANDSHAKEEXCEPTION");
            	break;
	        case VALIDATOREXCEPTION:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("VALIDATOREXCEPTION");
            	break;
	        case CERTIFICATEEXCEPTION:
	        	buttonPause.setBackground (Color.red);
            	buttonPause.setText ("CERTIFICATEEXCEPTION");
            	break;
		} // end switch on status
    } // end showStatus
    
	private void showCert() {
		if ( con!=null ) {
			try {
				String msg = '\n' + con.getURL().toString() + " || Response Code : " + con.getResponseCode();

				// popup with reponse digest
				JFrame responseFrame = new JFrame();
				JPanel responsePanel = new JPanel();
				responsePanel.setBorder(new TitledBorder("TLS RESPONSE DIGEST"));
				JTextArea responseTextArea = new JTextArea(100,100);
				JScrollPane responseScrollPane = new JScrollPane(responseTextArea);
				responsePanel.setLayout(new BorderLayout());
				responsePanel.add(responseScrollPane);
				responsePanel.setMinimumSize(new Dimension(800,450));
				responseFrame.add(responsePanel);
				responseFrame.setVisible(true);
				responseFrame.setMinimumSize(new Dimension(1000,500));

				responseTextArea.append('\n' + "Hostname : " + con.getURL().toString());
				responseTextArea.append('\n' + "Response Code : " + con.getResponseCode());
				responseTextArea.append('\n' + "Response Message:" + con.getResponseMessage());
				responseTextArea.append('\n' + "Cipher Suite : " + con.getCipherSuite());
				responseTextArea.append('\n' + "InstanceFollowRedirects:" + con.getInstanceFollowRedirects());
				responseTextArea.append('\n' + "Header : " + con.getHeaderField(1));
				responseTextArea.append('\n' + "Using proxy:" + con.usingProxy());

				Certificate[] certs = con.getServerCertificates();
				for(Certificate cert : certs) {
					if (cert instanceof X509Certificate x509cert) {

						// Get subject
				        Principal principal = x509cert.getSubjectX500Principal();
				        String subjectDn = principal.getName();
						responseTextArea.append('\n' + "Cert subject : " + subjectDn);
				        // Get issuer
				        principal = x509cert.getIssuerX500Principal();
				        String issuerDn = principal.getName();
						responseTextArea.append('\n' + "Cert issuer : " + issuerDn);
					}

					responseTextArea.append('\n' + "Cert Type : " + cert.getType());
					responseTextArea.append('\n' + "Cert Hash Code : " + cert.hashCode());
					responseTextArea.append('\n' + "Cert Public Key Algorithm : "
			                                    + cert.getPublicKey().getAlgorithm());
					System.out.println('\n' + "Cert Public Key Format : "
			                                    + cert.getPublicKey().getFormat());
				}
							
			} catch (javax.net.ssl.SSLHandshakeException e) {
				msg = "\n" + getTime() + con.getURL().toString() + " || COULD NOT CONNECT || SSLHandshakeException :  ----> (HttpsConnection.con.connect() -> java.net.ssl.SSLHandshakeException ";
				log.append(msg);
				System.out.println(msg);
				status = Status.SSLHANDSHAKEEXCEPTION;
				showStatus();

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
				status = Status.SSLPEERUNVERIFIEDEXCEPTION;
				showStatus();
			} catch (IOException e){
				String msg = "\n" + getTime() + " COULD NOT GETRESPONSE() || IOException : " + url.toString() + " ----> (HttpsClient.httpsConnect.con.connect() -> java.net.IOException() ";
				log.append(msg);
				System.out.println(msg);
				status = Status.IOEXCEPTION;
				showStatus();
			}
		}
	}
		
//	private void printPayload(HttpsURLConnection con){
//		if ( con!=null ) {
//			try {
//				
//			   System.out.println("****** Content of the URL ********");			
//			   BufferedReader br = 
//				new BufferedReader(
//					new InputStreamReader(con.getInputStream()));
//						
//			   String input;
//						
//			   while ((input = br.readLine()) != null){
//			      System.out.println(input);
//			   }
//			   br.close();
//			} catch (IOException e) {
//			   e.printStackTrace();
//			}
//	    }
//	}
}


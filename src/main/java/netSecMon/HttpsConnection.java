
/*

* The HttpsConnection class implements a Runnable and instantiates a Https Connection

*/


package netSecMon;


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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;


public class HttpsConnection extends JPanel implements Runnable {
		
	URL url;
	String ipAddress = "";
	HttpsURLConnection con;
	ConnectionsManager manager;
	ScheduledFuture futureTask;
	JButton buttonDelete;
	JLabel urlLabel;
	JProgressBar barProgress;
    JButton buttonPause;
	public JButton buttonTakeOffline;

	public Boolean isOffline = false;
	JButton buttonRestart;
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
		this.add(urlLabel);
		this.add(buttonPause);
		this.add(buttonTakeOffline);
		this.add(barProgress);
		this.add(buttonRestart);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		this.setBackground(new Color(209, 223, 250));
		showStatus(Status.RUNNING);
		buttonTakeOffline.addActionListener( e -> takeOffline());
		buttonRestart.addActionListener( e -> manager.restartTask(this));
		buttonDelete.addActionListener( e -> manager.deleteConnection(this));
	}
    void setLogField(JTextArea logTextArea) {
		log = logTextArea;
	}
	public void setManager(ConnectionsManager connectionmanager) {
		manager = connectionmanager;
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
//			System.out.println("FUTURETASK IS NULL" + (futureTask == null));
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
			showStatus(Status.RUNNING);
			barProgress.setValue(0);
			msg = "\n" + getTime() + " " + url.toString() + " || OPENING CONNECTION ";
			log.append(msg);
			System.out.println(msg);
			con = (HttpsURLConnection) url.openConnection();

			msg = "\n" + getTime() + " " + url.toString() + " || ATTEMPTING CONNECTION ";
			log.append(msg);
			System.out.println(msg);
			con.connect();
			try {
				getResponse(con);  // get and print certs
			} catch (javax.net.ssl.SSLHandshakeException e) {
				msg = "\n" + getTime() + con.getURL().toString() + " || COULD NOT CONNECT || SSLHandshakeException :  ----> (HttpsConnection.con.connect() -> java.net.ssl.SSLHandshakeException ";
				log.append(msg);
				System.out.println(msg);
				showStatus(Status.SSLHANDSHAKEEXCEPTION);

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
				showStatus(Status.SSLPEERUNVERIFIEDEXCEPTION);
			}
			barProgress.setValue(100);
			showStatus(Status.CONNECTED);
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
			showStatus(Status.CONNECTEXCEPTION);
		} catch (java.net.SocketException e) {
			String msg = "\n" + getTime() + " " + url.toString() + " || COULD NOT CONNECT || SocketException :  ----> (HttpsConnection.con.connect() -> socketException() ";
			log.append(msg);
			System.out.println(msg);
			showStatus(Status.SOCKETEXCEPTION);
		} catch (IOException e) {
			String msg = "\n" + getTime() + " " + url.toString() + " || COULD NOT CONNECT || IOException ";
			log.append(msg);
			System.out.println(msg);
			showStatus(Status.IOEXCEPTION);
		}
		log.append("\n");
		System.out.println("\n");
		//print all the content
		//printPayload(con);
	}

	void showStatus (Status st) {
        status = st;
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
    
	private void getResponse(HttpsURLConnection con) throws javax.net.ssl.SSLHandshakeException, SSLPeerUnverifiedException  {
	     
		if ( con!=null ) {
			try {
//				System.out.println("\n" + con.getURL().toString() + " || ATTEMPTING GETRESPONSE");
//				String msg = '\n' + con.getURL().toString() + " || Response Code : " + con.getResponseCode();
//				log.append(msg);
//				System.out.println(msg);
//				System.out.println("Response Message:"
//			                + con.getResponseMessage());
//				System.out.println("Cipher Suite : " + con.getCipherSuite());
//				System.out.println("InstanceFollowRedirects:"
//			                + con.getInstanceFollowRedirects());
//				System.out.println("Header : " + con.getHeaderField(1));
//				System.out.println("Using proxy:" + con.usingProxy());
//
				Certificate[] certs = con.getServerCertificates();
//				for(Certificate cert : certs) {
//					if (cert instanceof X509Certificate x509cert) {
//
//						// Get subject
//				        Principal principal = x509cert.getSubjectX500Principal();
//				        String subjectDn = principal.getName();
//				        System.out.println("Cert subject : " + subjectDn);
//				        // Get issuer
//				        principal = x509cert.getIssuerX500Principal();
//				        String issuerDn = principal.getName();
//				        System.out.println("Cert issuer : " + issuerDn);
//					}
//
//					System.out.println("Cert Type : " + cert.getType());
//					System.out.println("Cert Hash Code : " + cert.hashCode());
//					System.out.println("Cert Public Key Algorithm : "
//			                                    + cert.getPublicKey().getAlgorithm());
//					System.out.println("Cert Public Key Format : "
//			                                    + cert.getPublicKey().getFormat());
//				}
							
			} catch (IOException e){
				String msg = "\n" + getTime() + " COULD NOT GETRESPONSE() || IOException : " + url.toString() + " ----> (HttpsClient.httpsConnect.con.connect() -> java.net.IOException() ";
				log.append(msg);
				System.out.println(msg);
				showStatus(Status.IOEXCEPTION_GET);
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


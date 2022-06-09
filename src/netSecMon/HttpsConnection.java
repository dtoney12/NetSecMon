
/*

* The HttpsConnection class implements a Runnable and instantiates a Https Connection

*/


package netSecMon;


import java.net.URL;
import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.Principal;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;



public class HttpsConnection implements Runnable {
		
	URL url;
	HttpsURLConnection con;
	JProgressBar barProgress;
	Boolean pauseFlag = false;
	JLabel shipLabel;
    JButton buttonPause;
	JButton buttonStop;
	JPanel barPanel = new JPanel();
	Status status = Status.AWAITING_RESOURCE;
	JTextArea log;
	Box box;
	Component spacer;
	
	public HttpsConnection(URL urlObject) {
		url = urlObject;
		barProgress = new JProgressBar();
		barProgress.setStringPainted (true);
		shipLabel = new JLabel();
		shipLabel.setPreferredSize(new Dimension(140,20));
		buttonPause = new JButton("Pause");
		buttonPause.setPreferredSize(new Dimension(170,20));
		buttonPause.setOpaque(true);
		buttonPause.setBorderPainted(false);
		buttonStop = new JButton("Stop");
		buttonStop.setPreferredSize(new Dimension(130,20));
		barPanel.add(new JLabel (url.toString() , SwingConstants.LEFT));
		barPanel.add(shipLabel);
		barPanel.add(buttonPause);
		barPanel.add(buttonStop);
		barPanel.add(barProgress);
		barPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		barPanel.setBackground(new Color(209, 223, 250));
		spacer = Box.createRigidArea(new Dimension(0,5));
		spacer.setBackground(Color.BLACK);
		
	}
	
	public void run() {
		if (!pauseFlag) {
			try {
				box.add(barPanel);
				box.add(spacer);
				showStatus(Status.RUNNING);
				String msg = "\n" + url.toString() + " || OPENING CONNECTION ";
				log.append(msg);
				System.out.println(msg);
				con = (HttpsURLConnection)url.openConnection();
				
				msg = "\n" + url.toString() + " || ATTEMPTING CONNECTION ";
				log.append(msg);
				System.out.println(msg);
				con.connect();
				try { 
					getResponse(con);  // get and print certs
				} catch (javax.net.ssl.SSLHandshakeException e) {
					msg = "\n" + con.getURL().toString() + " || COULD NOT CONNECT || SSLHandshakeException :  ----> (HttpsConnection.con.connect() -> java.net.ssl.SSLHandshakeException ";
					log.append(msg);
					System.out.println(msg);
					showStatus(Status.SSLHANDSHAKEEXCEPTION);
					
				} catch (SSLPeerUnverifiedException e) {
					e.printStackTrace();
					showStatus(Status.SSLPEERUNVERIFIEDEXCEPTION);
				}
				barProgress.setValue(100);
				showStatus (Status.CONNECTED);
		    } 
	//		catch (sun.security.validator.ValidatorException e) {
	//	    	String msg = "\n" + url.toString() + " || COULD NOT CONNECT || ValidatorException :  ----> (HttpsConnection.con.connect() -> sun.security.validator.ValidatorException() ";
	//			log.append(msg);
	//			System.out.println(msg);
	//	    	showStatus(Status.VALIDATOREXCEPTION);
	//	    } 
			catch (java.net.ConnectException e) {
		    	String msg = "\n" + url.toString() + " || COULD NOT CONNECT || ConnectException :  ----> (HttpsConnection.con.connect() -> java.net.ConnectException() ";
		    	log.append(msg);
				System.out.println(msg);
		    	showStatus(Status.CONNECTEXCEPTION);
		    } catch (java.net.SocketException e) {
		    	String msg = "\n" + url.toString() + " || COULD NOT CONNECT || SocketException :  ----> (HttpsConnection.con.connect() -> socketException() ";
		    	log.append(msg);
				System.out.println(msg);
		    	showStatus(Status.SOCKETEXCEPTION);
		    } catch (IOException e) {
		    	String msg = "\n" + url.toString() + " || COULD NOT CONNECT || HttpsConnection.con.connect() -> java.net.IOException() ";
		    	log.append(msg);
		    	log.append('\n' + e.getMessage());
				System.out.println(msg);
				System.out.println('\n' + e.getMessage());
		    	showStatus(Status.IOEXCEPTION);
		    }
			log.append("\n");
			System.out.println("\n");
			//print all the content
			//printPayload(con);
		}
	}
    
    public void togglePause () {
    	pauseFlag = !pauseFlag;
    }
    void setLogField(JTextArea logTextArea) {
		log = logTextArea;
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
    
	void setJobsBox(Box jobsBox) {
		box = jobsBox;
	}
	   
	private void getResponse(HttpsURLConnection con) throws javax.net.ssl.SSLHandshakeException, SSLPeerUnverifiedException  {
	     
		if ( con!=null ) {
			try {
				System.out.println("\n" + con.getURL().toString() + " || ATTEMPTING GETRESPONSE");
				String msg = '\n' + con.getURL().toString() + " || Response Code : " + con.getResponseCode();
				log.append(msg);
				System.out.println(msg);
				System.out.println("Response Message:" 
			                + con.getResponseMessage());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("InstanceFollowRedirects:" 
			                + con.getInstanceFollowRedirects());
				System.out.println("Header : " + con.getHeaderField(1));
				System.out.println("Using proxy:" + con.usingProxy());
								
				Certificate[] certs = con.getServerCertificates();
				for(Certificate cert : certs) {
					if (cert instanceof X509Certificate x509cert) {

						// Get subject
				        Principal principal = x509cert.getSubjectX500Principal();
				        String subjectDn = principal.getName();
				        System.out.println("Cert subject : " + subjectDn);
				        // Get issuer
				        principal = x509cert.getIssuerX500Principal();
				        String issuerDn = principal.getName();
				        System.out.println("Cert issuer : " + issuerDn);
					}
					
					System.out.println("Cert Type : " + cert.getType());
					System.out.println("Cert Hash Code : " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm : " 
			                                    + cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format : " 
			                                    + cert.getPublicKey().getFormat());
				}
							
			} catch (IOException e){
				String msg = "\nCOULD NOT GETRESPONSE() || IOException : " + url.toString() + " ----> (HttpsClient.httpsConnect.con.connect() -> java.net.IOException() ";
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



/**
 * 
* The Thread_Manager class manages the thread pool and urls list for the NetSecMon application
* 
* @input

* 
* @exception 
*
* @author  Dale Toney
* @version 1.0
* @since   2019/3/28
**/


package netSecMon;

import java.util.ArrayList;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;

public class ConnectionsManager implements Runnable {
	ScheduledThreadPoolExecutor pool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);
	ArrayList<URL> urls = new ArrayList<URL>();
	ArrayList<HttpsConnection> connections = new ArrayList<HttpsConnection>();
	JTextArea log;	
	Box box;
	
	
	public void run() {
//		setTrustAllCerts();
		for (URL url: urls) {
			HttpsConnection connection = new HttpsConnection(url);
			connections.add(connection);
			connection.setLogField(log);
			connection.setJobsBox(box);
			pool.scheduleAtFixedRate(connection, 0, 30, TimeUnit.SECONDS);
		}
	}
	
	public void pause() {
		for (HttpsConnection connection: connections) {
			connection.togglePause();
		}
//		
	}
	
	public void stop() {
		pool.shutdown();
	}
	
	void setLogField(JTextArea logTextArea) {
		log = logTextArea;
	}
	void setJobsBox(Box jobsBox) {
		box = jobsBox;
	}
	private void setTrustAllCerts() {
		// Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
        SSLContext sc;
        String msg;
        try {
        	sc = SSLContext.getInstance("SSL");
        	try {
            	sc.init(null, trustAllCerts, new java.security.SecureRandom());
            	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            	// Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            }
            catch (KeyManagementException E) {
            	msg = "\nKeyManagementException :  ----> (URLIST_Manager)";
				log.append(msg);
				System.out.println(msg);
            }
        }
        catch (NoSuchAlgorithmException E) {
        	msg = "\nNoSuchAlgorithmException :  ----> (URLIST_Manager)";
			log.append(msg);
			System.out.println(msg);
        }
	}
}

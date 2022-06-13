
/*

The Thread_Manager class manages the thread pool and urls list for the NetSecMon application

*/


package netSecMon;

import java.awt.*;
import java.util.ArrayList;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.*;

public class ConnectionsManager implements Runnable {
	ScheduledThreadPoolExecutor pool;
	ArrayList<URL> urls;
	ArrayList<HttpsConnection> connections = new ArrayList<>();
//	ArrayList<ScheduledFuture> poolFutureTasks = new ArrayList<>();

	JTextArea log;	
	Box box;
	private int pollingInterval;
	
	public ConnectionsManager() {
		pool = new ScheduledThreadPoolExecutor(4);
		pool.setRemoveOnCancelPolicy(true);
	}
	public void run() {
//		setTrustAllCerts();
		connections.clear();
		box.removeAll();
		for (URL url: urls) {
			HttpsConnection connection = new HttpsConnection(url);
			connections.add(connection);
			box.add(connection);
			connection.setLogField(log);
			connection.setManager(this);
		}
	}
	public void startPolling() {
		stopPolling();
		for (HttpsConnection connection: connections) {
			if (!connection.isOffline) {
				ScheduledFuture task = pool.scheduleAtFixedRate(connection, 0, pollingInterval, TimeUnit.SECONDS);
				connection.setFutureTask(task);
			}
		}
	}
	public void stopPolling() {
		for (HttpsConnection connection: connections) {
			if (connection.futureTask != null) {
				connection.futureTask.cancel(true);
			}
		}
	}

	public void setPollingInterval(Integer i) {
		pollingInterval = i;
	}
	public void restartTask(HttpsConnection connection) {
		connection.setFutureTask(pool.scheduleAtFixedRate(connection, 0, pollingInterval, TimeUnit.SECONDS));
		connection.setBackground(new Color(209, 223, 250));
		connection.buttonTakeOffline.setEnabled(true);
		connection.isOffline = false;
	}
	public void deleteConnection(HttpsConnection connection) {
		connection.cancelTask();
		connections.remove(connection);
		box.removeAll();
		for (HttpsConnection conns: connections) {
			box.add(conns);
		}
		box.repaint();
	}
	void setLogField(JTextArea logTextArea) {
		log = logTextArea;
	}
	void setJobsBox(Box jobsBox) {
		box = jobsBox;
	}
	void setUrls(ArrayList<URL> urlList) {
		urls = urlList;
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
                HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> true;
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

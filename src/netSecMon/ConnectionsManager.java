
/*

The Thread_Manager class manages the thread pool and urls list for the NetSecMon application

*/


package netSecMon;

import java.util.ArrayList;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
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
	ScheduledThreadPoolExecutor pool;
	ArrayList<URL> urls;
	ArrayList<HttpsConnection> connections = new ArrayList<>();
	ArrayList<ScheduledFuture> poolTasks = new ArrayList<>();

	JTextArea log;	
	Box box;
	private int pollingInterval;
	
	
	public void run() {
//		setTrustAllCerts();
		connections.clear();
		box.removeAll();
		for (URL url: urls) {
			HttpsConnection connection = new HttpsConnection(url);
			connections.add(connection);
			box.add(connection);
			connection.setLogField(log);
		}
	}
	public void startPolling() {
		pool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);
		poolTasks.clear();
		for (HttpsConnection connection: connections) {
			poolTasks.add(pool.scheduleAtFixedRate(connection, 0, pollingInterval, TimeUnit.SECONDS));
		}
	}
	public void stopPolling() {
		if (pool != null) pool.shutdownNow();
		for (ScheduledFuture task: poolTasks) {
			task.cancel(true);
		}
		poolTasks.clear();
	}

	public void setPollingInterval(Integer i) {
		pollingInterval = i;
		if (!poolTasks.isEmpty()) {
			poolTasks.clear();
			for (HttpsConnection connection : connections) {
				poolTasks.add(pool.scheduleAtFixedRate(connection, 0, pollingInterval, TimeUnit.SECONDS));
			}
		}
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

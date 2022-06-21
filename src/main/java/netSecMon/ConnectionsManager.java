
/*

The Thread_Manager class manages the thread pool and urls list for the NetSecMon application

*/


package netSecMon;

import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.awt.*;
import java.net.UnknownHostException;
import java.util.*;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
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
	JPanel box;
	private int pollingInterval;
	
	public ConnectionsManager() {
		pool = new ScheduledThreadPoolExecutor(4);
		pool.setRemoveOnCancelPolicy(true);
	}
	public void run() {
		try {
			//		setTrustAllCerts();
			connections.clear();
			box.removeAll();
			for (URL url : urls) {
				HttpsConnection connection = new HttpsConnection(url);
				connection.setLogField(log);
				connection.setManager(this);
				connections.add(connection);
				box.add(connection);
//				connection.resolveHostIP();
				box.validate();
				box.repaint();
//				System.out.println("]\n Panel height = " + connection.getHeight());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\n ERROR: " + e.getMessage());
		}
	}
	public int searchFuzzy() {
		return FuzzySearch.ratio("mysmilarstring","myawfullysimilarstirng");
	}
	public void startPolling() {
		stopPolling();
		for (HttpsConnection connection: connections) {
			if (!connection.isOffline) {
				connection.setFutureTask(pool.scheduleAtFixedRate(connection, 0, pollingInterval, TimeUnit.SECONDS));
			}
		}
	}
	public void stopPolling() {
		for (HttpsConnection connection: connections) {
			if (connection.futureTask != null) {
				connection.cancelFutureTask();
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
		connection.cancelFutureTask();
		connections.remove(connection);
		box.remove(connection);
		box.validate();
		box.repaint();
	}
	public void sortBy(String sortByType) {
		try {
			switch (sortByType) {
				case "Alphabetical" ->
						connections.sort((conn1, conn2) -> conn1.url.getHost().compareToIgnoreCase(conn2.url.getHost()));
				case "IP Address" -> connections.sort((conn1, conn2) -> {
					if (conn1.ipAddress.length() == 0) {
						if (conn2.ipAddress.length() == 0) {
							return 0;
						}
						return 1;
					} else if (conn2.ipAddress.length() == 0) {
						return -1;
					}
					Integer[] ip1 = Arrays.stream(conn1.ipAddress.split("\\.")).mapToInt(Integer::valueOf).boxed().toArray(Integer[]::new);
					Integer[] ip2 = Arrays.stream(conn2.ipAddress.split("\\.")).mapToInt(Integer::valueOf).boxed().toArray(Integer[]::new);
					if (Objects.equals(ip1[0], ip2[0])) {
						if (Objects.equals(ip1[1], ip2[1])) {
							if (Objects.equals(ip1[2], ip2[2])) {
								if (Objects.equals(ip1[3], ip2[3])) {
									return 0;
								}
								return ip1[3].compareTo(ip2[3]);
							}
							return ip1[2].compareTo(ip2[2]);
						}
						return ip1[1].compareTo(ip2[1]);
					}
					return ip1[0].compareTo(ip2[0]);
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\n ERROR: " + e.getMessage());
		}
		box.removeAll();
		for (HttpsConnection connection: connections) {
			box.add(connection);
		}
		box.validate();
		box.repaint();
	}
	void setLogField(JTextArea logTextArea) {
		log = logTextArea;
	}
	void setJobsBox(JPanel jobsBox) {
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

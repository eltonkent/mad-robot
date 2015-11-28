/*******************************************************************************
 * Copyright (c) 2011 MadRobot.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.madrobot.security.HexUtils;
import com.madrobot.text.CharUtils;

public class NetUtils {

	/**
	 * Create and initialise sn SSLContext.
	 * 
	 * @param protocol
	 *            the protocol used to instatiate the context
	 * @param keyManagers
	 *            the array of key managers, may be {@code null} but array
	 *            entries must not be {@code null}
	 * @param trustManagers
	 *            the array of trust managers, may be {@code null} but array
	 *            entries must not be {@code null}
	 * @return the initialised context.
	 * @throws IOException
	 *             this is used to wrap any {@link GeneralSecurityException}
	 *             that occurs
	 */
	public static SSLContext createSSLContext(String protocol, KeyManager[] keyManagers,
			TrustManager[] trustManagers) throws IOException {
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance(protocol);
			ctx.init(keyManagers, trustManagers, /* SecureRandom */null);
		} catch (GeneralSecurityException e) {
			IOException ioe = new IOException("Could not initialize SSL context");
			ioe.initCause(e);
			throw ioe;
		}
		return ctx;
	}

	/**
	 * Enable Http Response cache. Works only on Ice-cream sandwich.
	 * 
	 * @param cacheDir
	 *            Directory to be used as a response cache
	 * @param cacheSize
	 *            Size of the cache
	 */
	public static void enableHttpResponseCache(String cacheDir, long cacheSize) {
		try {
			File httpCacheDir = new File(cacheDir, "http");
			Class.forName("android.net.http.HttpResponseCache")
					.getMethod("install", File.class, long.class)
					.invoke(null, httpCacheDir, cacheSize);
		} catch (Exception e) {
		}
	}

	/**
	 * Encodes the string in the string buffer
	 * <p>
	 * Encodes a string so that it is suitable for transmission over HTTP
	 * </p>
	 * 
	 * @param string
	 * @return the encoded string
	 */
	public final static String encodeString(StringBuilder string) {
		StringBuilder encodedUrl = new StringBuilder(); // Encoded URL

		int len = string.length();
		// Encode each URL character
		final String UNRESERVED = "-_.!~*'()\"";
		for (int i = 0; i < len; i++) {
			char c = string.charAt(i); // Get next character
			if (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z'))
					|| ((c >= 'A') && (c <= 'Z'))) {
				// Alphanumeric characters require no encoding, append as is
				encodedUrl.append(c);
			} else {
				int imark = UNRESERVED.indexOf(c);
				if (imark >= 0) {
					// Unreserved punctuation marks and symbols require
					// no encoding, append as is
					encodedUrl.append(c);
				} else {
					// Encode all other characters to Hex, using the format
					// "%XX",
					// where XX are the hex digits
					encodedUrl.append('%'); // Add % character
					// Encode the character's high-order nibble to Hex
					encodedUrl.append(CharUtils.toHexChar((c & 0xF0) >> 4));
					// Encode the character's low-order nibble to Hex
					encodedUrl.append(CharUtils.toHexChar(c & 0x0F));
				}
			}
		}
		return encodedUrl.toString(); // Return encoded URL
	}

	/**
	 * Get the data network's IP address
	 * 
	 * @return
	 */
	public static String getCarrierAllocatedIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
					.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				System.out.println("Interface Name" + intf.getDisplayName());
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("CDI", ex.toString());
		}

		return null;
	}

	/**
	 * Finds this device's global IP address
	 * 
	 * @param url
	 *            the url to find the global IP
	 * @return The global IP address, or null if a problem occurred
	 */
	public static Inet4Address getGlobalAddress(String url) {
		try {
			URLConnection uc = new URL(url).openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			return (Inet4Address) InetAddress.getByName(br.readLine());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Finds a local, non-loopback, IPv4 address
	 * 
	 * @return The first non-loopback IPv4 address found, or <code>null</code>
	 *         if no such addresses found
	 * @throws SocketException
	 *             If there was a problem querying the network interfaces
	 */
	public static InetAddress getLocalAddress() throws SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> addresses = iface.getInetAddresses();

			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				if ((addr instanceof Inet4Address) && !addr.isLoopbackAddress()) {
					return addr;
				}
			}
		}
		return null;
	}

	private static boolean isMaskValue(String component, int size) {
		try {
			int value = Integer.parseInt(component);

			return value >= 0 && value <= size;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isValidEmailAddress(String address) {
		final String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(emailPattern);
		Matcher matcher = pattern.matcher(address);
		return matcher.matches();
	}

	/**
	 * Validate the given IPv4 address.
	 * 
	 * @param address
	 *            the IP address as a String.
	 * 
	 * @return true if a valid IPv4 address, false otherwise
	 */
	public static boolean isValidIPv4(String address) {
		String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(address);
		return matcher.matches();
	}

	public static boolean isValidIPv4WithNetmask(String address) {
		int index = address.indexOf("/");
		String mask = address.substring(index + 1);

		return (index > 0) && isValidIPv4(address.substring(0, index))
				&& (isValidIPv4(mask) || isMaskValue(mask, 32));
	}

	/**
	 * Validate the given IPv6 address.
	 * 
	 * @param address
	 *            the IP address as a String.
	 * 
	 * @return true if a valid IPv4 address, false otherwise
	 */
	public static boolean isValidIPv6(String address) {
		if (address.length() == 0) {
			return false;
		}

		int octet;
		int octets = 0;

		String temp = address + ":";
		boolean doubleColonFound = false;
		int pos;
		int start = 0;
		while (start < temp.length() && (pos = temp.indexOf(':', start)) >= start) {
			if (octets == 8) {
				return false;
			}

			if (start != pos) {
				String value = temp.substring(start, pos);

				if (pos == (temp.length() - 1) && value.indexOf('.') > 0) {
					if (!isValidIPv4(value)) {
						return false;
					}

					octets++; // add an extra one as address covers 2 words.
				} else {
					try {
						octet = Integer.parseInt(temp.substring(start, pos), 16);
					} catch (NumberFormatException ex) {
						return false;
					}
					if (octet < 0 || octet > 0xffff) {
						return false;
					}
				}
			} else {
				if (pos != 1 && pos != temp.length() - 1 && doubleColonFound) {
					return false;
				}
				doubleColonFound = true;
			}
			start = pos + 1;
			octets++;
		}

		return octets == 8 || doubleColonFound;
	}

	public static boolean isValidIPv6WithNetmask(String address) {
		int index = address.indexOf("/");
		String mask = address.substring(index + 1);

		return (index > 0)
				&& (isValidIPv6(address.substring(0, index)) && (isValidIPv6(mask) || isMaskValue(
						mask, 128)));
	}

	/**
	 * Uses regular expressions to validate a URL
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isValidURL(String url) {
		String regex = "\\b(https?|ftp|file)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]";
		Pattern patt = Pattern.compile(regex);
		Matcher matcher = patt.matcher(url);
		return matcher.matches();
	}

	/**
	 * Extract Http links from a given text.
	 * <p>
	 * Looks for the following patterns<br/>
	 * <ul>
	 * <li>http[s]://www.google.com</li>
	 * <li>http[s]://www.google.com:80</li>
	 * <li>www.google.com</li>
	 * <li>http[s]://www.google.com/test/test.html</li>
	 * <li>www.google.com/test/test.html</li>
	 * <li>http[s]://google.com</li>
	 * </ul>
	 * It does not work for strings that contains URL's with no "http://" or
	 * "www" preceeding the URL. Eg: URL's with just Google.com will not work.
	 * </p>
	 * 
	 * @param text
	 * @return
	 */
	public static ArrayList<String> extractHttpLinks(String text) {
		ArrayList<String> links = new ArrayList<String>();

		String regex = "\\(?\\b(http://|www[.]|https://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		while (m.find()) {
			String urlStr = m.group();
			char[] stringArray1 = urlStr.toCharArray();
			if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
				char[] stringArray = urlStr.toCharArray();
				char[] newArray = new char[stringArray.length - 2];
				System.arraycopy(stringArray, 1, newArray, 0, stringArray.length - 2);
				urlStr = new String(newArray);
				System.out.println("Finally Url =" + newArray.toString());

			}
			links.add(urlStr);
		}
		return links;
	}

	/**
	 * Makes the platform trust all SSL connections.
	 * <p>
	 * Warning: This should be used for development purposes only
	 * </p>
	 */
	public static void trustAllSecureConnections() {
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}

	/**
	 * @return True if the url is correctly URL encoded
	 */
	public static boolean verifyURLEncoding(String url) {
		int count = url.length();
		if (count == 0) {
			return false;
		}

		int index = url.indexOf('%');
		while (index >= 0 && index < count) {
			if (index < count - 2) {
				try {
					HexUtils.parseHex(url.charAt(++index));
					HexUtils.parseHex(url.charAt(++index));
				} catch (IllegalArgumentException e) {
					return false;
				}
			} else {
				return false;
			}
			index = url.indexOf('%', index + 1);
		}
		return true;
	}

	/**
	 * check if the app has network connectivity
	 * 
	 * @param ctxt
	 * @return
	 */
	public static boolean isConnected(Context ctxt) {
		NetworkInfo nfo = ((ConnectivityManager) ctxt
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (nfo != null) {
			return nfo.isConnected();
		}
		return false;
	}

	/**
	 * Converts the given IP address to Classless Inter-Domain Routing number
	 * 
	 * @throws IllegalArgumentException
	 *             if the ip address is invalid.
	 * @param ip
	 * @return
	 * 
	 */
	public static int IpToCidr(String ip) {
		if (!isValidIPv4(ip)) {
			throw new IllegalArgumentException("Invalid IP address");
		}
		double sum = -2;
		String[] part = ip.split("\\.");
		for (String p : part) {
			sum += 256D - Double.parseDouble(p);
		}
		return 32 - (int) (Math.log(sum) / Math.log(2d));
	}

	/**
	 * Check if the given host is reachable in the specified timeout.
	 * 
	 * @param host
	 */
	public static void doPing(String host, int timeout) {
		try {
			// TODO: Use ProcessBuilder ?
			Runtime.getRuntime().exec(
					String.format("/system/bin/ping -q -n -w 1 -c 1 %s", host));
		} catch (Exception e) {
			try {
				if (InetAddress.getByName(host).isReachable(timeout)) {
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Get the average response time of the given host.
	 * <p>
	 * </p>
	 * 
	 * @param host
	 * @param timeout
	 *            good timeout values are between 1000 and 5000
	 * @return
	 */
	public static int getAverageResponseTime(String host, int timeout) {
		// TODO: Reduce allocation
		BufferedReader reader = null;
		int rate = 800;
		Matcher matcher;
		try {
			final Process proc = Runtime.getRuntime().exec(
					"/system/bin/ping -A -q -n -w 3 -W 2 -c 3 " + host);
			reader = new BufferedReader(new InputStreamReader(proc.getInputStream()), 512);
			String line;
			Pattern mPattern = null;
			while ((line = reader.readLine()) != null) {
				matcher = mPattern.matcher(line);
				if (matcher.matches()) {
					reader.close();
					return (int) Float.parseFloat(matcher.group(1));
				}
			}
			reader.close();
		} catch (Exception e) {
			try {
				final long start = System.nanoTime();
				if (InetAddress.getByName(host).isReachable(timeout)) {
					return (int) ((System.nanoTime() - start) / 1000);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		return rate;
	}

	/**
	 * Send a stop signal to the given port.
	 * <p>
	 * Stops any incoming stream on the given port.
	 * </p>
	 * 
	 * @param port
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static void sendStopSignal(int port) throws UnknownHostException, IOException {
		Socket s = new Socket(InetAddress.getByName("127.0.0.1"), port);
		OutputStream out = s.getOutputStream();
		System.err.println("sending server stop request");
		out.write(("\r\n").getBytes());
		out.flush();
		s.close();
	}

}

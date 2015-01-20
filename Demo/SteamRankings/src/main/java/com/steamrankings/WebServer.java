/**
 * This is the template file for Assignment 1 Problem 2 for ECSE 414 Fall 2014.
 * 
 * This class implements a multi-threaded HTTP 1.0-compliant web server. The
 * root directory from which files are served is the same directory from which
 * this application is executed. When the server encounters an error, it sends a
 * response message with the appropriate HTML code so that the error information
 * is displayed.
 * 
 * @author michaelrabbat
 *
 */
package com.steamrankings;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.koraktor.steamcondenser.exceptions.WebApiException;
import com.github.koraktor.steamcondenser.steam.community.WebApi;

/**
 * This is the main class which runs the loop that listens for incoming requests
 * and spawns new threads to handle each request.
 * 
 * @author michaelrabbat
 * 
 */
public final class WebServer {
	public static void main(String argx[]) throws Exception {
		// Step 1: Set the port number (may not work with 80)
		int port = 6789;
		
		// Create the socket to listen for incoming connections
		ServerSocket serverSocket = new ServerSocket(port);
		
		// Enter an infinite loop and process incoming connections
		// Use Ctrl-C to quit the application
		while (true) {
			// Listen for a new TCP connection request
			Socket clientSocket = serverSocket.accept();
			
			// Construct an HttpRequest object to process the request message
			HttpRequest httpRequest = new HttpRequest(clientSocket);
			
			// Create a new thread to process the request
			// Start the thread
			new Thread(httpRequest).start();
		}
	}
}

/**
 * This is the helper class that processes individual HTTP requests
 * 
 * @author michaelrabbat
 * 
 */
final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;
	
	/**
	 * Constructor takes the socket for this request
	 */
	public HttpRequest(Socket socket) throws Exception
	{
		this.socket = socket;
	}
	
	/**
	 * Implement the run() method of the Runnable interface. 
	 */
	public void run()
	{
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * This is where the action occurs
	 * @throws Exception
	 */
	private void processRequest() throws Exception
	{
		// STEP 2a: Parse the HTTP Request message
		// Get a reference to the socket's input and output streams
		
		// Set up input stream filters
		BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		
		// Get the request line of the HTTP request message
		String requestLine = input.readLine();

		// Display the request line
		System.out.println();
		System.out.println(requestLine);

		// Get and display the header lines
		String headerLine;
		do {
			headerLine = input.readLine();
			System.out.println(headerLine);
		} while(!headerLine.equals(""));
		
		// (The last part of STEP 2 is at the end of this method)
		// (Close the socket)
		
		// STEP 3a: Prepare and Send the HTTP Response message
		// Extract the filename from the request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over the method, which we'll assume is "GET"
		String fileName = tokens.nextToken();
		
		String[] request = fileName.split("\\?");
		String name = "Not found";
		
		if(request[0].equals("/getSteamID")) {
			request = request[1].split("=");
			if(request[0].equals("id")) {
				SteamrollerApi api;
				try {
					// SPECIFY API KEY
					String APIKEY = null;
					if (APIKEY == null) {
						throw new Exception("API Key not found");
					}
					
					api = new SteamrollerApi("");
					Map<String,Object> param = new HashMap<String, Object>();
					System.out.println(request[1]);
					
					param.put("steamids", request[1]);
					String jsonString = api.getJSON("ISteamUser", "GetPlayerSummaries", 2, param);
					System.out.println(jsonString);
					
					
					JSONObject json = new JSONObject(jsonString);
					System.out.println(json);
					
					
					json = (JSONObject) json.get("response");
					System.out.println(json);
					
					
					JSONArray jsonArray = json.getJSONArray("players");
					System.out.println(jsonArray);
					
					
					
					for(int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonElement = jsonArray.getJSONObject(i);
						Iterator itr = jsonElement.keys();
					      while(itr.hasNext()) {
					    	 String element = (String) itr.next();
					         if(element.equals("personaname")) {
					        	 name = jsonElement.getString("personaname");
					        	 break;
					         }
					      }
					}
					
					System.out.println(name);
					
				} catch (WebApiException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		System.out.println(fileName);		
		// Prepend a "." to the file name so that the file request is in the
		// current directory
		fileName = "." + fileName;
		
		// Open the requested file
		InputStream fis = null;
		
		// Construct the response message header
		String statusLine = null;
		String contentTypeLine = null;
		String errorMessage = "<HTML><HEAD><TITLE>404 Not Found</TITLE></HEAD><BODY>404 Not Found</BODY></HTML>";

		statusLine = "HTTP/1.0 200" + CRLF;
		contentTypeLine = "Content-type: " + "text/plain" + CRLF;
		
		// Send a HTTP response header containing the status line and
		// content-type line. Don't forget to include a blank line after the
		// content-type to signal the end of the header.
		output.writeBytes(statusLine);
		output.writeBytes(contentTypeLine);
		output.writeBytes(CRLF);
		
		// Send the body of the message (the web object)
		// You may use the sendBytes helper method provided
		fis = new ByteArrayInputStream(name.getBytes());
		sendBytes(fis, output);
		output.writeBytes(CRLF);
		// STEP 2b: Close the input/output streams and socket before returning
		System.out.println();
		System.out.println("Closing input stream...");
		input.close();
		System.out.println("Closing output stream...");
		output.close();
		System.out.println("Closing socket...");
		socket.close();
	}
	
	/**
	 * Private method that returns the appropriate MIME-type string based on the
	 * suffix of the appended file
	 * @param fileName
	 * @return
	 */
	private static String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		else if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
			return "image/jpeg";
		}
		else if(fileName.endsWith(".gif")) {
			return "image/gif";
		}
		else if(fileName.endsWith(".css")) {
			return "text/css";
		}
		
		// STEP 3b: Add code here to deal with GIFs and JPEGs
		return "application/octet-stream";
	}

	/**
	 * Private helper method to read the file and send it to the socket
	 * @param fis
	 * @param os
	 * @throws Exception
	 */
	private static void sendBytes(InputStream fis, OutputStream os) 
		throws Exception 
	{
		// Allocate a 1k buffer to hold bytes on their way to the socket
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		// Copy requested file into the socket's output stream
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}
}
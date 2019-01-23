package serverSocket2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;


public class ServerSocket2 {
	public static void main(String[] arg)throws Exception{

		int port = 8081;
		try {
			ServerSocket server = new ServerSocket(port);
			
			// run
			while(true) {
				Socket client = server.accept(); 
				new Thread(new ClientThread(client)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ClientThread implements Runnable {

	private Socket client;
	private InputStream input;
	private OutputStream output;
	
	public ClientThread(Socket s) {
		client = s;
		try {
			input = client.getInputStream();
			output = client.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		String filename = read();
		String[] spliters = filename.split("\\.");
		String suffix = spliters[spliters.length - 1];
		String contentType = "Content-Type:text/html \n";
		try {
			StringBuffer head = new StringBuffer();
			
			if(suffix.equals("png") || suffix.equals("jpg")) {
				contentType = "Content-Type:image/png" + "\n";
				
			}
			DataOutputStream bOutput = new DataOutputStream(output);
			BufferedInputStream reader = new BufferedInputStream(new FileInputStream("." + filename));
			// add head
			head.append("HTTP /1.1 200 ok \n");
			head.append(contentType);
			head.append("Content-Length:" + reader.available() + "\n");
			head.append("\n");
			bOutput.write(head.toString().getBytes());
			
			byte[] buffer = new byte[4096];
			int bytesRead;
			while( (bytesRead = reader.read(buffer)) != -1) {
				bOutput.write(buffer, 0, bytesRead);
			}
			bOutput.close();
			reader.close();
			
		} catch (Exception e) {
			ServerLogger.error(e.getMessage());
		}
		

	}
	
	private String read() {
		String returnValue = "/mainpage.html";
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		try {
			// read request head, like: GET /index.html HTTP/1.1
			String readLine = reader.readLine();
			ServerLogger.info(client.getInetAddress().getHostAddress() + ":" + readLine);

			String[] split = readLine.split(" ");
			if(!split[1].equals("/") ) {

				returnValue = split[1];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnValue;
	}
}

class ServerLogger {
	public static void info(String msg) {
		System.out.println("INFO-" + msg);
	}
	public static void error(String msg) {
		System.out.println("ERROR-" + msg);
	}
}


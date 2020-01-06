package tokenring;

/**
 * Nodo
 * @author Vincenzo Cavallo, Malamine Liviano D’Arcangelo Koumare
 */

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Nodo {

	private static final String TAG = "node";

	private final String ip_add_str;
	private InetAddress ip_add;
	private int id;
	private final int port;
	private int neighbor_port;
	// UDP Socket
	private DatagramSocket dgramSocket;
	// private final Socket senderSocket;
	
	/* Input and output buffers will be declared 
	   at the moment of the Sending/receiving a packet
	private BufferedReader socketIn;
	private PrintWriter socketOut;
	*/

	boolean token_bool = false;
	boolean running = false;
	private Token token;

	public Nodo(int id, String ip_add, int port, int neighbor_port) {
		this.id = id;
		this.ip_add_str = ip_add;
		this.port = port;
		this.neighbor_port = neighbor_port;
		dgramSocket = null;
		running = false;

		try {
			this.ip_add = InetAddress.getByName(ip_add);
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host");
			e.printStackTrace();
		}

	}
	public void start(){
		try {
			dgramSocket = new DatagramSocket(this.port);
			running = true;	
			System.out.println("Node " + id + " Listening on port: " + port + ", neighbor is: " + neighbor_port );
		} catch (IOException e) {
			System.out.println(TAG + port + " Impossibile ascoltare sulla porta: " + port);
			e.printStackTrace();
		}
	}

	public void send_msg(int id_dest, String msg) throws SocketTimeoutException{
		DatagramPacket out;
		byte[] byte_buffer;
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String current_time_str = formatter.format(date);
		//Packet content: Timestamp, sender ID, Destination ID, message
		String content = current_time_str + "," 
						 + Integer.toString(this.id) + "," 
						 + Integer.toString(id_dest) + "," 
						 + msg;

		out = new DatagramPacket(content.getBytes(), content.length(), ip_add, neighbor_port);
		try {
			dgramSocket.send(out);
		} catch (IOException e) {
			System.out.println("Not able to send package out");
			e.printStackTrace();
		}
	}

	/**Reads a message and if it is not the receiver, forwards it to the neighbor Node */
	public void read_msg() throws SocketTimeoutException{
		DatagramPacket in, out;
		byte[] byte_buffer = new byte[512];
		long timestamp;
		String content, time_str, sender_id_str, dest_id_str, msg;

		in = new DatagramPacket(byte_buffer, byte_buffer.length);
		
		try {
			dgramSocket.setSoTimeout(1000);
			dgramSocket.receive(in);
			content = new String(in.getData(), 0, in.getLength());
			time_str = content.substring(0, content.indexOf(","));
			sender_id_str = content.substring(content.indexOf(",")+1, content.indexOf(",", content.indexOf(",")+1));
			dest_id_str = content.substring(content.indexOf(",", content.indexOf(",") +1) +1, content.lastIndexOf(","));
			msg = content.substring(content.lastIndexOf(",")+1);
		
			//DEBUG
			System.out.println("time_str: " + time_str);
			System.out.println("sender_id_str: " + sender_id_str);
			System.out.println("dest_id_str: " + dest_id_str);
			System.out.println("msg: " + msg);
			//----------
			
			if (this.id == Integer.parseInt(dest_id_str)){
				System.out.println("\nNode " + this.id + " Received the message: \n");
				System.out.println(msg + "\n");
				System.out.println("from node " + sender_id_str + " at time: " + time_str + "\n");
			}
			else {
				System.out.println("Node " + this.id + " is forwarding the message to its neighbor");
				out = new DatagramPacket(in.getData(), in.getLength(), ip_add, this.neighbor_port);
				dgramSocket.send(out);
			}			
		}catch (SocketTimeoutException ste){
		}
		 catch (IOException e) {
			System.out.println("Not able to send package out");
			e.printStackTrace();
		}
	}
		
	public void stop() {
		running = false;
	}

}
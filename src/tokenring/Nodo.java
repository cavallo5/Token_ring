package tokenring;

/**
 * Nodo
 */

import java.io.*;
import java.net.*;

public class Nodo {

	/**
	 * Tag
	*/
	private static final String TAG = "node";
	/**
	 * Indirizzo nodo in stringa
	*/
	private final String ip_add_str;
	/**
	 * Indirizzo nodo
	*/
	private InetAddress ip_add;
	/**
	 * Id del ndoo
	*/
	private int id;
	/**
	 * Porta del nodo
	*/
	private final int porta;
	/**
	 * Porta del nodo vicino
	*/
	private int porta_vicino;
	/**
	 * Variabili per il calcolo dell'elapsed time
	*/
	long start_time, end_time, time_used=0;

	/**
	 * Socket UDP
	 */
	private DatagramSocket dgramSocket;
	/**
	 * Variabile booleana che è true se il nodo è in esecuzione, altrimenti è falsa.
	 * E' inizializzato a falsa, ma all'avvio del nodo, viene posta a true
	 */
	boolean running = false;

	/*
	 *   Costruttore
	 *   @param id id del nodo
	 *   @param ip_add indirizzo del nodo
	 *   @param port porta del nodo
	 *   @param neighbor_port porta del nodo vicino
	*/
	public Nodo(int id, String ip_add, int port, int neighbor_port) {
		//Inizializzazioni
		this.id = id;
		this.ip_add_str = ip_add;
		this.porta = port;
		this.porta_vicino = neighbor_port;
		dgramSocket = null;
		running = false;
		
		try {
			this.ip_add = InetAddress.getByName(ip_add);
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host");
			e.printStackTrace();
		}
	}
	
	/*
	 * Metodo che simula l'avvio del nodo
	*/
	public void start(){
		try {
			dgramSocket = new DatagramSocket(this.porta); //creazione socket UDP
			running = true;	
			System.out.println("Node " + id + " Listening on port: " + porta + ", neighbor is: " + porta_vicino );
		} catch (IOException e) {
			System.out.println(TAG + porta + " Impossibile ascoltare sulla porta: " + porta);
			e.printStackTrace();
		}
	}

	/*
	 * Metodo per l'invio di un messaggio
	 * @param id_dest id del destinatario del messaggio
	 * @param msg messaggio 
	*/
	public void send_msg(int id_dest, String msg) throws SocketTimeoutException{
		//Genero il pacchetto in uscita 
		DatagramPacket out; //buffer di output
		start_time=System.nanoTime(); //Avvio tempo
		String content = start_time + "," 
						 + Integer.toString(this.id) + "," 
						 + Integer.toString(id_dest) + "," 
						 + msg;
		out = new DatagramPacket(content.getBytes(), content.length(), ip_add, porta_vicino);
		try {
			dgramSocket.send(out);
		} catch (IOException e) {
			System.out.println("Not able to send package out");
			e.printStackTrace();
		}
	}
	
	/*
	 * Metodo che blocca il nodo e ne fa terminare l'esecuzione 
	*/
	public void stop() {
		running = false;
	}

	/*
	 * Funzione che esegue il nodo quando arriva un messaggio. Il nodo lo legge e se:
	 * 	- non è il destinatario, lo inoltra al nodo vicino
 	 *  - è il destinatario, calcola l'elapsed time
	 */
	public void read_msg() throws SocketTimeoutException{
		DatagramPacket in, out; //buffer di input e output vengono dichiarati al momento di ogni invio del pacchetto
		byte[] byte_buffer = new byte[512];
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
		
			/*
			//DEBUG
			System.out.println("time_str: " + time_str);
			System.out.println("sender_id_str: " + sender_id_str);
			System.out.println("dest_id_str: " + dest_id_str);
			System.out.println("msg: " + msg);
			*/
			
			if (this.id == Integer.parseInt(dest_id_str)){ //Se l'ID del nodo che riceve il messaggio è uguale al destinatario del messaggio
				//Calcolo elapsed time
				end_time= System.nanoTime(); //tempo finale in ns 
				time_used= (long) ((end_time - start_time)/1000F); //tempo impiegato in 􏱇µs
				
				System.out.println("\nNodo " + this.id + " ha ricevuto il messaggio: \n");
				System.out.println(msg + "\n");
				System.out.println("dal nodo " + sender_id_str + " impiegando: " + time_used + " us \n");
			}
			else { //Il nodo non è il destinatario del messaggio
				System.out.println("Nodo " + this.id + " invia il messaggio al suo vicino");
				out = new DatagramPacket(in.getData(), in.getLength(), ip_add, this.porta_vicino); //creo buffer di output
				dgramSocket.send(out); //invio messaggio al vicino
			}			
		}catch (SocketTimeoutException ste){
		}
		 catch (IOException e) {
			System.out.println("Not able to send package out");
			e.printStackTrace();
		}
	}
}
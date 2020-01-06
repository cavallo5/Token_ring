package tokenring;

/**
 * TokenRing 
 * @author Vincenzo Cavallo, Malamine Liviano D’Arcangelo Koumare
 */

import java.io.IOException;
import java.util.Scanner;
import java.net.*;


public class TokenRing {
	private static final String IP_ADDRESS = "127.0.0.1";
	/**
	 * Porta di partenza 
	 */
	private int start_port;
	/**
	 * Numero di nodi 
	 */
	private int num_nodi; //numero nodi
	/**
	 * Numero di token 
	 */
	private int num_tokens;
	/**
	 * Numero di token assegnati
	 */
	private int assigned_token;
	private int[] workers;
	private boolean running = false;
	/**
	 * Array di nodi che formano la tokenring
	 */
	private Nodo[] ring_nodes;

	/**
	* Restituisce il numero di nodi
	* @return num_nodi
	*/
	public int getnum_nodes() {
	  	return num_nodi;
	}
	/**
	* Restituisce il numero di token 
	* @return num_token
	*/  
	public int getnum_tokens() {
		  	return num_tokens;
	}

	/*
	 *   Costruttore
	 *   @param start_port porta iniziale da cui partire per la generazione delle altre porte
	 *   @param num_nodes numero dei nodi
	 *   @param num_tokens numero dei token
	*/
	public TokenRing(int start_port, int num_nodes, int num_tokens){
		//creo num_nodes nodi
		ring_nodes = new Nodo[num_nodes];
		//Token token;
		this.start_port = start_port;
		this.num_nodi = num_nodes;
		this.num_tokens = num_tokens;
		this.workers = new int[num_tokens];
		this.assigned_token = 0;
		//final Integer hostname = null;
	}

	  
	public static void main( final String args[]) throws IOException {
		//final Integer hostname = null;
		TokenRing tr = new TokenRing(4040, 10, 2);
		tr.create_nodes();
		//tr.test_msg();
		tr.run();
	}

		public void run(){
			Scanner user_in = new Scanner(System.in);
			// Hop limit rappresenta il limite di passi che può fare ogni singolo token
			int hop_limit = num_nodi;
			int hop_counter = 0;
			running = true;
			//int i = 0;
			while (running){
				hop_counter = 0;
				while (assigned_token < num_tokens){
					int w = (int) (assigned_token * num_nodi / num_tokens);
					// Workers contains the IDs of the nodes possessing the tokens
					workers[assigned_token] = w;
					assigned_token++;
					//i = (i+1) % num_tokens;
				}

				while ( hop_counter <= hop_limit){
					for (int j=0; j<num_tokens; j++){
						int id_dest;
						String msg = "";
						System.out.println("Node " + workers[j] + " can send a message " +
											"insert destination or a number higher than " + 
											num_nodi + " if no message has to be send");
						id_dest = user_in.nextInt();
						try {
							
						if (id_dest <= num_nodi) {
							System.out.println("Write the message: \n");
							msg = user_in.next();
							ring_nodes[workers[j]].send_msg(id_dest, msg);
						}
							// il nodo con il token, prima invia un eventuale messaggio
							// ed in seguito controlla se ne ha in coda.
							ring_nodes[workers[j]].read_msg();								
						} catch (SocketTimeoutException ste) {
							ste.printStackTrace();;
						}					
					}
					for (int k = 0; k<num_tokens; k++){
						workers[k] = (workers[k]+1) % num_nodi;
					}

					hop_counter++;
				}


			}
		}


	public void create_nodes(){
		//Creazione Nodi
		for( int i=0; i < num_nodi;i++)
		{
			//hostname=String.valueOf(i);
			/** The last node is connected to the first one */
			if (i != num_nodi-1){
				ring_nodes[i] = new Nodo(i, IP_ADDRESS, start_port+i, start_port+i+1);
			}
			else{
				ring_nodes[i] = new Nodo(i, IP_ADDRESS, start_port+i, start_port);
			}
			ring_nodes[i].start();
		}

		running = true;

	}

	public void quit(){
		for (int i=1; i<num_nodi; i++){
			ring_nodes[i].stop();
		}
		running = false;
	}
}
	
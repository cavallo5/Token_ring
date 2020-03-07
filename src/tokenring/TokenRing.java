package tokenring;

/**
 * TokenRing 
 */

import java.io.IOException;
import java.util.Scanner;
import java.net.*;


public class TokenRing {
	/**
	 * localhost 
	 */
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
	private int token_assegnati;
	/**
	 * I worker contengono l'ID dei nodi che possiedono il token
	 */
	private int[] workers;
	/**
	 * Variabile booleana che è true se la token è in esecuzione, altrimenti è falsa.
	 * E' inizializzato a falsa, ma all'avvio della token, viene posta a true
	 */
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
		ring_nodes = new Nodo[num_nodes]; //creazione nodi
		this.start_port = start_port; //porta iniziale dalla quale partire per creare le porte degli altri nodi
		this.num_nodi = num_nodes; //numero nodi
		this.num_tokens = num_tokens; //numero token
		this.workers = new int[num_tokens];
		this.token_assegnati = 0; //all'inizio non ho assegnato token
	}
	
	/*
	 * Metodo che simula l'esecuzione della tokenring
	*/
	public void run(){
			Scanner user_in = new Scanner(System.in);
			// limitepassi rappresenta il limite di passi che può fare ogni singolo token
			int limitepassi = num_nodi; //pongo pari al numero di nodi per far in modo che almeno una comunicazione è garantita
			int contatorepassi = 0; //conta i passi del token
			running = true; 
			while (running){
				contatorepassi = 0;
				while (token_assegnati < num_tokens){ 
					int w = (int) (token_assegnati * num_nodi / num_tokens); //numero del token a cui corrisponderà il nodo 
					workers[token_assegnati] = w; //Salvo l'ID dei nodi che avranno il token
					token_assegnati++; //incremento la variabile contatore dei token assegnati
				}
				
				while ( contatorepassi <= limitepassi){
					for (int j=0; j<num_tokens; j++){
						int id_dest;
						String msg = "Hello"; //messaggio del pacchetto
						System.out.println("Nodo " + workers[j] + " ha il token e può inviare un messaggi " +
											"inserisci la destinazione o il numero. Se si sceglie un numero >= " + 
											num_nodi + " allora il nodo non manderà un messaggio");
						id_dest = user_in.nextInt();
						try {
							if (id_dest <= num_nodi) { //se si è inserito un numero inferiore a 500, inviamo il messaggio
								ring_nodes[workers[j]].send_msg(id_dest, msg); //invia messaggi
							}
							// il nodo con il token, prima invia un eventuale messaggio
							// ed in seguito controlla se ne ha in coda
							ring_nodes[workers[j]].read_msg(); //lettura messaggio							
						} catch (SocketTimeoutException ste) {
							ste.printStackTrace();;
						}					
					}
					for (int k = 0; k<num_tokens; k++){
						workers[k] = (workers[k]+1) % num_nodi;
					}
					contatorepassi++;
				}
			}
		}

	/*
	 * Metodo che crea la token ring in base al numero di nodi dichiarati
	*/
	public void create_nodes(){
		//Creazione Nodi
		for( int i=0; i < num_nodi;i++)
		{
			if (i != num_nodi-1){ //Il nodo i-esimo è collegato al nodo i+1-esimo
				ring_nodes[i] = new Nodo(i, IP_ADDRESS, start_port+i, start_port+i+1);
			}
			else{ //l'ultimo nodo è connesso al primo nodo
				ring_nodes[i] = new Nodo(i, IP_ADDRESS, start_port+i, start_port); 
			}
			ring_nodes[i].start(); //Avvio token
		}
		running = true; //aggiorno la variabile booleana che è true quando la tokenring è stata avviata
	}

	/*
	 * Metodo che termina l'esecuzione della token ring
	*/
	public void quit(){
		for (int i=1; i<num_nodi; i++){
			ring_nodes[i].stop();
		}
		running = false;
	}
	

	public static void main( final String args[]) throws IOException {
		TokenRing tr = new TokenRing(4040, 500, 2); //Creo una token con porta iniziale 4040, 500 nodi e 2 token
		tr.create_nodes(); //Creazione nodi e collegamenti
		tr.run(); //Avvio
	}
}
	
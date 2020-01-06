package tokenring;

/**
 * Token
 * @author Vincenzo Cavallo, Malamine Liviano D’Arcangelo Koumare
 */

public class Token {
	/**
	  * variabile contatore
	 */
	int contatore;
	/**
	  * limite del numero di passi all'interno della token ring
	 */
	int limitepassi;
	
	/*
	 *   Costruttore
	*/	
	public Token(){
		this.contatore=0;
		this.limitepassi=600;
	}
	/**
	* Restituisce il contatore
	* @return contatore
	*/
	public int getcontatore() {
		return this.contatore;
	}
	 /**
     * Metodo set della variabile contatore
     * @param a int con la quale setto la variabile contatore
     */
	public void setcontatore(int a) {
		this.contatore=a;
	}
	 /**
     * Funzione che incrementa il contatore all'interno del token
     */
	public void incrementatoken() {
		this.contatore++;
	}
	
	 /**
     * Funzione per il controllo del numero di passi del token
     * @return true se il token ha raggiunto il limite dei passi, altrimenti false
     */
	public boolean controllapassi() {
		if(this.contatore==this.limitepassi) {
			return true;
		}
		else return false;
	}
}
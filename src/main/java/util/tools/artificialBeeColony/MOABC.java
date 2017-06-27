package util.tools.artificialBeeColony;


/**
 * This class implements the MOABC (Multi-Objective Artificial Bee Colony) algorithm based on the article:
 *  - An artificial bee colony algorithm for multi-objective optimisation (2017)
 * 
 * @author Alexandre
 */
public class MOABC {

	private int numberOfIterations;
	private int limite;
	private int arcSize;
	private int popSize;
	
	public MOABC(int numberOfIterations, int limite, int arcSize, int popSize) {
		this.numberOfIterations = numberOfIterations;
		this.limite = limite;
		this.arcSize = arcSize;
		this.popSize = popSize;
	}
	
	public void execute(){
		
		// Initialize the archive and population
		
		for(int i = 0; i < numberOfIterations; i++){
			
			sendingEmployedBees();
			sendingOnlookerBees();
			sendingScoutsBees();
			maintainingArchive();
			
		}
		
	}
	
	public void sendingEmployedBees(){
		
		
	}
	
	public void sendingOnlookerBees(){
		
		
	}
	
	public void sendingScoutsBees(){
		
		
	}
	
	public void maintainingArchive(){
		
		
	}

}

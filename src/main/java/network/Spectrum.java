package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * This class represents the spectrum in the network links
 * 
 * @author Iallen
 */
public class Spectrum implements Serializable {
	
	// To represent the state of a slot
	public static final char FREE = 'l';
	public static final char BUSY = 'u';
	public static final char GB_FOR_ONE = 'b';
	public static final char GB_FOR_TWO = 'B';
	
	private char spectrum[]; // Represents all slots
	
	private TreeSet<int[]> freeSpectrumBands; // Represents the free slots bands
	private int numOfSlots;
	private double slotSpectrumBand;
	private int usedSlots;
	
	private HashMap<Integer, int[]> downGuardBandList;
	private HashMap<Integer, int[]> upperGuardBandList;
	
	/**
	 * Creates a new instance of Spectrum
	 * 
	 * @param numOfSlots int
	 * @param slotSpectrumBand double
	 */
	public Spectrum(int numOfSlots, double slotSpectrumBand){
		
		this.numOfSlots = numOfSlots;
		this.slotSpectrumBand = slotSpectrumBand;
		
		spectrum = new char[numOfSlots];
		for(int i = 0; i < numOfSlots; i++) {
			spectrum[i] = FREE;
		}
		
		int fsb[] = new int[2];
		fsb[0] = 1;
		fsb[1] = numOfSlots;
		freeSpectrumBands = new TreeSet<int[]>(new MyComparator());
		freeSpectrumBands.add(fsb);
		
		downGuardBandList = new HashMap<Integer, int[]>();
		upperGuardBandList = new HashMap<Integer, int[]>();
		
		usedSlots = 0; 
	}

	private class MyComparator implements Comparator<int[]>, Serializable{
		@Override
		public int compare(int[] o1, int[] o2) {
			Integer i1, i2;
			i1 = o1[0];
			i2 = o2[0];
			return i1.compareTo(i2);
		}
	}
	
	/**
	 * Mark as used a certain spectrum band
	 * 
	 * @param spectrumBand int
	 * @return boolean
	 */
	public boolean useSpectrum(int spectrumBand[], int guardBand) throws Exception {
		
		if (spectrumBand[0] > spectrumBand[1]){
			throw new Exception("Invalid spectrum band");
		}
		
		if (checksCollisionWithGuardBand(spectrumBand)) {
			throw new Exception("Trying to use a slot reserved for a guard band. Spectrum band: " + spectrumBand[0] + " - " + spectrumBand[1]);
		}
		
		for (int freeSpecBand[] : this.freeSpectrumBands) {
			if(isInInterval(spectrumBand, freeSpecBand)){
				
				usedSpectrumUpdate(spectrumBand, guardBand);
				addGuardBands(spectrumBand, guardBand);
				
				freeSpectrumBands.remove(freeSpecBand); // Remove free bands
				
				// Create new free bands
				int newSpecBand[];
				if(spectrumBand[0] - freeSpecBand[0] != 0){ // Create band of what's left behind
					newSpecBand = new int[2];
					newSpecBand[0] = freeSpecBand[0];
					newSpecBand[1] = spectrumBand[0] - 1;
					this.freeSpectrumBands.add(newSpecBand);
				}
				
				if(freeSpecBand[1] - spectrumBand[1] != 0){ // Create band of what's left ahead
					newSpecBand = new int[2];
					newSpecBand[0] = spectrumBand[1] + 1;
					newSpecBand[1] = freeSpecBand[1];
					this.freeSpectrumBands.add(newSpecBand);
				}

				usedSlots = usedSlots + (spectrumBand[1] - spectrumBand[0] + 1);
				
				return true;
			}			
		}
		
		return false;
	}
	
	/**
	 * Checks whether the first interval is contained in the second
	 * 
	 * @param inter1 int[]
	 * @param inter2 int[]
	 * @return boolean
	 */
	private boolean isInInterval(int inter1[], int inter2[]){
		
		if(inter1[0] >= inter2[0])
			if(inter1[1] <= inter2[1])
				return true;
		
		return false;
	}
	
	/**
	 * Mark as free a certain spectrum band
	 * 
	 * @param spectrumBand int[]
	 */
	public void freeSpectrum(int spectrumBand[], int guardBand) throws Exception {
		
		if(spectrumBand[0] > spectrumBand[1]){
			throw new Exception("Invalid spectrum band");
		}

		for (int freeSpecBand[] : this.freeSpectrumBands) {
			if(isInInterval(spectrumBand, freeSpecBand)){
				throw new Exception("Spectrum is already free. Spectrum band: " + spectrumBand[0] + " - " + spectrumBand[1]);
			}
		}
		
		freeSpectrumUpdate(spectrumBand, guardBand);
		removeGuardBands(spectrumBand, guardBand);
		
		this.freeSpectrumBands.add(spectrumBand); // Releasing spectrum

		usedSlots = usedSlots - (spectrumBand[1] - spectrumBand[0] + 1);

		// To merge free spectra when necessary
		int merge[];
		
		// First merge with previous free spectrum
		int aux[] = {spectrumBand[0]-1,spectrumBand[1]};
		int flor[] = this.freeSpectrumBands.floor(aux);
		
		if(flor!=null && flor[1] == (spectrumBand[0] - 1)){ // It is necessary to merge
			merge = new int[2];
			merge[0] = flor[0];
			merge[1] = spectrumBand[1];
			this.freeSpectrumBands.remove(flor);
			this.freeSpectrumBands.remove(spectrumBand);
			this.freeSpectrumBands.add(merge);
			spectrumBand = merge;
		}
		
		// Second merge with posterior free spectrum
		int after[] = this.freeSpectrumBands.higher(spectrumBand);
		if(after != null && (after[0] - 1) == spectrumBand[1]){// It is necessary to merge
			merge = new int[2];
			merge[0] = spectrumBand[0];
			merge[1] = after[1];
			this.freeSpectrumBands.remove(after);
			this.freeSpectrumBands.remove(spectrumBand);
			this.freeSpectrumBands.add(merge);
		}
	}
	
	/**
	 * Add upper and down guard bands in the guard bands list
	 * 
	 * @param spectrumBand int[]
	 * @param guardBand int
	 */
	private void addGuardBands(int spectrumBand[], int guardBand) {
		
		int downGB = guardBand;
		if(spectrumBand[0] == 1) { // Check if the band starts on the first slot
			downGB = 0;
		}
		int upperGB = guardBand;
		if(spectrumBand[1] == numOfSlots) { // Check if the band ends on the last slot
			upperGB = 0;
		}
		
		if (downGB > 0) { // Check if you need to create the upper guard band
			int downGuardBand[] = new int[2];
			downGuardBand[0] = spectrumBand[0] - downGB;
			downGuardBand[1] = spectrumBand[0] - 1;
			
			if (downGuardBand[0] < 1) { // To prevent the guard band from leaving the spectrum limit
				downGuardBand[0] = 1;
			}
			
			this.downGuardBandList.put(downGuardBand[1], downGuardBand); // Referenced in this way to stay like upper to the free spectrum band
		}
		
		if (upperGB > 0) { // Check if you need to create the down guard band
			int upperGuardBand[] = new int[2];
			upperGuardBand[0] = spectrumBand[1] + 1;
			upperGuardBand[1] = spectrumBand[1] + upperGB;
			
			if (upperGuardBand[1] > numOfSlots) { // To prevent the guard band from leaving the spectrum limit
				upperGuardBand[1] = numOfSlots;
			}
			
			this.upperGuardBandList.put(upperGuardBand[0], upperGuardBand); // Referenced in this way to stay like down to the free spectrum band
		}
	}
	
	/**
	 * Remove upper and down guard bands in the guard bands list
	 * 
	 * @param spectrumBand int[]
	 * @param guardBand int
	 */
	private void removeGuardBands(int spectrumBand[], int guardBand) {
		
		int downGB = guardBand;
		if(spectrumBand[0] == 1) { // Check if the band starts on the first slot
			downGB = 0;
		}
		int upperGB = guardBand;
		if(spectrumBand[1] == numOfSlots) { // Check if the band ends on the last slot
			upperGB = 0;
		}
		
		if (downGB > 0) { // Check if you need to remove the upper guard band
			int downGuardBand[] = new int[2];
			downGuardBand[0] = spectrumBand[0] - downGB;
			downGuardBand[1] = spectrumBand[0] - 1;
			
			if (downGuardBand[0] < 1) { // To prevent the guard band from leaving the spectrum limit
				downGuardBand[0] = 1;
			}
			
			this.downGuardBandList.remove(downGuardBand[1]); // Removing the reference upper from the free spectrum band
		}
		
		if (upperGB > 0) { // Check if you need to remove the down guard band
			int upperGuardBand[] = new int[2];
			upperGuardBand[0] = spectrumBand[1] + 1;
			upperGuardBand[1] = spectrumBand[1] + upperGB;

			if (upperGuardBand[1] > numOfSlots) { // To prevent the guard band from leaving the spectrum limit
				upperGuardBand[1] = numOfSlots;
			}
			
			this.upperGuardBandList.remove(upperGuardBand[0]); // Removing the reference down from the free spectrum band
		}
	}
	
	/**
	 * Check if the band to be used by the circuit is not colliding with some guard band
	 * 
	 * @param spectrumBand[]
	 * @return boolean
	 */
	public boolean checksCollisionWithGuardBand(int spectrumBand[]) {
		int gb[] = null;
		
		for(int slotNumber : downGuardBandList.keySet()) {
			gb = downGuardBandList.get(slotNumber);
			
			if((gb[0] >= spectrumBand[0] && gb[0] <= spectrumBand[1]) || (gb[1] >= spectrumBand[0] && gb[1] <= spectrumBand[1]) ||
			   (gb[0] <= spectrumBand[0] && gb[1] >= spectrumBand[0]) || (gb[0] <= spectrumBand[1] && gb[1] >= spectrumBand[1])){
				return true;
			}
		}
		
		for(int slotNumber : upperGuardBandList.keySet()) {
			gb = upperGuardBandList.get(slotNumber);
			
			if((gb[0] >= spectrumBand[0] && gb[0] <= spectrumBand[1]) || (gb[1] >= spectrumBand[0] && gb[1] <= spectrumBand[1]) ||
			   (gb[0] <= spectrumBand[0] && gb[1] >= spectrumBand[0]) || (gb[0] <= spectrumBand[1] && gb[1] >= spectrumBand[1])){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Updating the used spectrum
	 * 
	 * @param spectrumBand int[]
	 * @param guardBand int
	 */
	private void usedSpectrumUpdate(int spectrumBand[], int guardBand) throws Exception {
		
		int leftGuardBand = guardBand;
		if(spectrumBand[0] == 1) {
			leftGuardBand = 0;
		}
		int rightGuardBand = guardBand;
		if(spectrumBand[1] == numOfSlots) {
			rightGuardBand = 0;
		}
		
		int slotsNumber = spectrumBand[1] - spectrumBand[0] + 1;
		
		int index = spectrumBand[0] - 1;
		for(int i = 0; i < slotsNumber; i++) {
			
			if(index < 0 || index >= numOfSlots) {
				break;
			}
			
			if(spectrum[index] == FREE) {
				spectrum[index] = BUSY;
				
			}else{
				throw new Exception("Spectrum is not free. Spectrum: " + spectrum[index]);
			}
			index++;
		}
		
		for(int i = 0; i < leftGuardBand; i++) {
			index = (spectrumBand[0] - 2) - i;
			
			if(index < 0 || index >= numOfSlots) {
				break;
			}
			
			if(spectrum[index] == FREE) {
				spectrum[index] = GB_FOR_ONE;
				
			}else if(spectrum[index] == GB_FOR_ONE) {
				spectrum[index] = GB_FOR_TWO;
				
			}else {
				throw new Exception("Spectrum is not free and is not guard band. Spectrum: " + spectrum[index]);
			}
		}
		
		for(int i = 0; i < rightGuardBand; i++) {
			index = spectrumBand[1] + i;
			
			if(index < 0 || index >= numOfSlots) {
				break;
			}
			
			if(spectrum[index] == FREE) {
				spectrum[index] = GB_FOR_ONE;
				
			}else if(spectrum[index] == GB_FOR_ONE) {
				spectrum[index] = GB_FOR_TWO;
				
			}else {
				throw new Exception("Spectrum is not free and is not guard band. Spectrum: " + spectrum[index]);
			}
		}
	}
	
	/**
	 * Updating the free spectrum
	 * 
	 * @param spectrumBand int[]
	 * @param guardBand int
	 */
	private void freeSpectrumUpdate(int spectrumBand[], int guardBand) throws Exception {
		
		int leftGuardBand = guardBand;
		if(spectrumBand[0] == 1) {
			leftGuardBand = 0;
		}
		int rightGuardBand = guardBand;
		if(spectrumBand[1] == numOfSlots) {
			rightGuardBand = 0;
		}
		
		int slotsNumber = spectrumBand[1] - spectrumBand[0] + 1;
		
		int index = spectrumBand[0] - 1;
		for(int i = 0; i < slotsNumber; i++) {
			
			if(index < 0 || index >= numOfSlots) {
				break;
			}
			
			if(spectrum[index] == BUSY) {
				spectrum[index] = FREE;
				
			}else{
				throw new Exception("Spectrum is not busy. Spectrum: " + spectrum[index]);
			}
			index++;
		}
		
		for(int i = 0; i < leftGuardBand; i++) {
			index = (spectrumBand[0] - 2) - i;
			
			if(index < 0 || index >= numOfSlots) {
				break;
			}
			
			if(spectrum[index] == GB_FOR_ONE) {
				spectrum[index] = FREE;
				
			}else if(spectrum[index] == GB_FOR_TWO) {
				spectrum[index] = GB_FOR_ONE;
				
			}else {
				throw new Exception("Spectrum is not guard band. Spectrum: " + spectrum[index]);
			}
		}
		
		for(int i = 0; i < rightGuardBand; i++) {
			index = spectrumBand[1] + i;
			
			if(index < 0 || index >= numOfSlots) {
				break;
			}
			
			if(spectrum[index] == GB_FOR_ONE) {
				spectrum[index] = FREE;
				
			}else if(spectrum[index] == GB_FOR_TWO) {
				spectrum[index] = GB_FOR_ONE;
				
			}else {
				throw new Exception("Spectrum is not guard band. Spectrum: " + spectrum[index]);
			}
		}
	}
	
	/**
	 * Returns the free spectrum bands at the moment
	 * 
	 * @return List<int[]>
	 */
	public List<int[]> getFreeSpectrumBands(){
		ArrayList<int[]> res = new ArrayList<>();
		
		for (int fsb[] : this.freeSpectrumBands) {
			res.add(fsb.clone());
		}
		
		return res;
	}
	
	/**
	 * Returns the free spectrum bands checking the guard bands and guard band required for the establishment of a given circuit
	 * 
	 * @param guardBand
	 * @return List<int[]>
	 */
	public List<int[]> getFreeSpectrumBands(int guardBand){
		ArrayList<int[]> res = new ArrayList<>();
		
		int numDownGB;
		int numUpperGB;
		int downGB[];
		int upperGB[];
		
		for (int fsb[] : this.freeSpectrumBands) {
			
			numDownGB = 0;
			numUpperGB = 0;
			
			downGB = upperGuardBandList.get(fsb[0]); // Upper guard bands of the circuits are down guard bands for free spectrum bands
			if (downGB != null) {
				numDownGB = downGB[1] - downGB[0] + 1;
			}
			
			upperGB = downGuardBandList.get(fsb[1]); // Down guard bands of the circuits are upper guard bands for free spectrum bands
			if (upperGB != null) {
				numUpperGB = upperGB[1] - upperGB[0] + 1;
			}
			
			if (guardBand > numDownGB) { // Tries to leave enough slots to respect the guard band required by the circuit
				numDownGB = guardBand;
			}
			
			if (guardBand > numUpperGB) { // Tries to leave enough slots to respect the guard band required by the circuit
				numUpperGB = guardBand;
			}
			
			if (fsb[0] == 1) { // Check if the band starts on the first slot
				numDownGB = 0;
			}
			
			if (fsb[1] == numOfSlots) { // Check if the band ends on the last slot
				numUpperGB = 0;
			}
			
			// Check that there are still free slots after removing the slots belonging to the guard bands
			if ((fsb[1] - fsb[0] + 1) - (numDownGB + numUpperGB) > 0) {
				
				// Creates a new slots band by removing the slots from the guard bands and leaving only the slots free of fact
				int newfsb[] = new int[2];
				newfsb[0] = fsb[0] + numDownGB;
				newfsb[1] = fsb[1] - numUpperGB;
				
				res.add(newfsb);
			}
		}
		
		return res;
	}
	
	/**
	 * Returns the free spectrum band without considering the guard band 
	 * Uses the guard band required for the circuit to check the free spectrum bands
	 * 
	 * @param freeSpectrumBand
	 * @param guardBand
	 * @return List<int[]>
	 */
	public List<int[]> getFreeSpectrumForAllocationWithoutGuardBand(List<int[]> freeSpectrumBandList, int guardBand){
		ArrayList<int[]> res = new ArrayList<>();
		
		for (int freeSpectrumBand[] : freeSpectrumBandList) {
			
			int leftGB = 0;
			int rightGB = 0;
			
			int numSlotsOfFreeBand = freeSpectrumBand[1] - freeSpectrumBand[0] + 1;
			
			int index = -1;
			int contLeftBG = 0;
			for(int n = 0; n < numSlotsOfFreeBand; n++) {
				index = (freeSpectrumBand[0] - 1) + n;
				
				if(index < 0 || index >= numOfSlots) {
					break;
				}
				
				if (spectrum[index] == GB_FOR_ONE) {
					contLeftBG++;
					
				} else {
					break;
				}
			}
			
			index = -1;
			int contRightBG = 0;
			for(int n = 0; n < numSlotsOfFreeBand; n++) {
				index = (freeSpectrumBand[1] - 1) - n;
				
				if(index < 0 || index >= numOfSlots) {
					break;
				}
				
				if (spectrum[index] == GB_FOR_ONE) {
					contRightBG++;
				
				} else {
					break;
				}
			}
			
			// Selects the largest band guard of left
			if(contLeftBG > guardBand) {
				leftGB = contLeftBG;
				
			}else {
				leftGB = guardBand;
			}
			
			// Selects the largest band guard of right
			if(contRightBG > guardBand) {
				rightGB = contRightBG;
				
			}else {
				rightGB = guardBand;
			}
			
			if (freeSpectrumBand[0] == 1) {
				leftGB = 0;
			}
			
			if (freeSpectrumBand[1] == numOfSlots) {
				rightGB = 0;
			}
			
			if (numSlotsOfFreeBand - (leftGB + rightGB) > 0) {
				
				int newfsb[] = freeSpectrumBand.clone();
				newfsb[0] = newfsb[0] + leftGB;
				newfsb[1] = newfsb[1] - rightGB;
				
				res.add(newfsb);
			}
		}
		
		return res;
	}
	
	/**
	 * Returns the spectrum usage ranging from 0 to 1
	 * 
	 * @return double
	 */
	public double utilization(){
		return ((double)usedSlots)/((double)numOfSlots);
	}

	/**
	 * Returns the spectrum bandwidth
	 * 
	 * @return double the slotSpectrumBand
	 */
	public double getSlotSpectrumBand() {
		return slotSpectrumBand;
	}

	/**
	 * Return the number of slots
	 * 
	 * @return int the numOfSlots
	 */
	public int getNumOfSlots() {
		return numOfSlots;
	}
	
	/**
	 * Returns the number of used slots
	 * 
	 * @return the usedSlots int
	 */
	public int getUsedSlots(){
		return usedSlots;
	}
	
}

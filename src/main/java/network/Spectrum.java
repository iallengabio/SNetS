package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
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
		
		int fsin[] = new int[2];
		fsin[0] = 1;
		fsin[1] = numOfSlots;
		freeSpectrumBands = new TreeSet<int[]>(new MyComparator());
		freeSpectrumBands.add(fsin);
		
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

		if(spectrumBand[0] > spectrumBand[1]){
			throw new Exception("invalid spectrum band");
		}

		for (int freeSpecBand[] : this.freeSpectrumBands) {
			if(isInInterval(spectrumBand, freeSpecBand)){
				
				usedSpectrumUpdate(spectrumBand, guardBand);
				
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
		
		if(inter1[0]>=inter2[0])
			if(inter1[1]<=inter2[1])
				return true;
		
		return false;
	}
	
	/**
	 * Mark as free a certain spectrum band
	 * 
	 * @param spectrumBand int[]
	 */
	public void freeSpectrum(int spectrumBand[], int guardBand) throws Exception {
		
		if(spectrumBand[0]>spectrumBand[1]){
			throw new Exception("invalid spectrum band");
		}

		for (int freeSpecBand[] : this.freeSpectrumBands) {
			if(isInInterval(spectrumBand, freeSpecBand)){
				throw new Exception("spectrum is already free. spectrum band: " + spectrumBand[0] + " - " + spectrumBand[1]);
			}
		}
		
		freeSpectrumUpdate(spectrumBand, guardBand);
		
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
			if(spectrum[index] == FREE) {
				spectrum[index] = BUSY;
				
			}else{
				throw new Exception("Spectrum is not free. Spectrum: " + spectrum[index]);
			}
			index++;
		}
		
		for(int i = 0; i < leftGuardBand; i++) {
			index = (spectrumBand[0] - 2) + i;
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
			if(spectrum[index] == BUSY) {
				spectrum[index] = FREE;
				
			}else{
				throw new Exception("Spectrum is not busy. Spectrum: " + spectrum[index]);
			}
			index++;
		}
		
		for(int i = 0; i < leftGuardBand; i++) {
			index = (spectrumBand[0] - 2) + i;
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
		
		for (int[] is : this.freeSpectrumBands) {
			res.add(is.clone());
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
	public List<int[]> getFreeSpectrumForAllocationWithoutGuardBand(int[] freeSpectrumBand, int guardBand){
		
		int leftGB = 0;
		int rightGB = 0;
		
		int numSlotsOfFreeBand = freeSpectrumBand[1] - freeSpectrumBand[0] + 1;
		
		int index = -1;
		int contLeftBG = 0;
		for(int n = 0; n < numSlotsOfFreeBand; n++) {
			index = (freeSpectrumBand[0] - 1) + n;
			
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
		
		int newfsb[] = freeSpectrumBand.clone();
		newfsb[0] = newfsb[0] + leftGB;
		newfsb[1] = newfsb[1] - rightGB;
		
		ArrayList<int[]> res = new ArrayList<>();
		res.add(newfsb);
		
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

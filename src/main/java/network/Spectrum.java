package network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * This class represents the spectrum in the network links
 * 
 * @author Iallen
 */
public class Spectrum {
	
	private TreeSet<int[]> freeSpectrumBands;
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
		
		freeSpectrumBands = new TreeSet<int[]>(new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				Integer i1, i2;
				i1 = o1[0];
				i2 = o2[0];
				return i1.compareTo(i2);
			}
		});
		this.numOfSlots = numOfSlots;
		this.slotSpectrumBand = slotSpectrumBand;
		int fsin[] = new int[2];
		fsin[0] = 1;
		fsin[1] = numOfSlots;		
		freeSpectrumBands.add(fsin);
		usedSlots = 0; 
	}
	
	/**
	 * Mark as used a certain spectrum band
	 * 
	 * @param spectrumBand int
	 * @return boolean
	 */
	public boolean useSpectrum(int spectrumBand[]) throws Exception {

		if(spectrumBand[0]>spectrumBand[1]){
			throw new Exception("invalid spectrum band");
		}

		for (int freSpecBand[] : this.freeSpectrumBands) {
			if(isInInterval(spectrumBand, freSpecBand)){
				freeSpectrumBands.remove(freSpecBand); // Remove free bands
				
				// Create new free bands
				int newSpecBand[];
				if(spectrumBand[0] - freSpecBand[0] != 0){ // Create band of what's left behind
					newSpecBand = new int[2];
					newSpecBand[0] = freSpecBand[0];
					newSpecBand[1] = spectrumBand[0] - 1;
					this.freeSpectrumBands.add(newSpecBand);
				}
				
				if(freSpecBand[1] - spectrumBand[1] != 0){ // Create band of what's left ahead
					newSpecBand = new int[2];
					newSpecBand[0] = spectrumBand[1] + 1;
					newSpecBand[1] = freSpecBand[1];
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
	public void freeSpectrum(int spectrumBand[]) throws Exception {

		if(spectrumBand[0]>spectrumBand[1]){
			throw new Exception("invalid spectrum band");
		}

		for (int freSpecBand[] : this.freeSpectrumBands) {
			if(isInInterval(spectrumBand, freSpecBand)){
				throw new Exception("spectrum is already free. spectrum band: " + spectrumBand[0] + " - " + spectrumBand[1]);
			}
		}

		
		this.freeSpectrumBands.add(spectrumBand); //liberando spectro

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
	 * @return the usedSlots
	 */
	public int getUsedSlots(){
		return usedSlots;
	}
	
}

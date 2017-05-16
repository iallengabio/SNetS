package network;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class Spectrum {
	
	private TreeSet<int[]> freeSpectrumBands;
	private int numOfSlots;
	private double slotSpectrumBand;
	private int slotsUsados;
	
	
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
		slotsUsados = 0; 
	}
	
	
	/**
	 * marca como utilizada uma determinada faixa de espectro
	 * @param spectrumBand
	 * @return
	 */
	public boolean useSpectrum(int spectrumBand[]){
		
		for (int freSpecBand[] : this.freeSpectrumBands) {
			if(isInInterval(spectrumBand, freSpecBand)){
				freeSpectrumBands.remove(freSpecBand); //remover faixa livre
				
				//criar novas faixas livres
				int newSpecBand[];
				if(spectrumBand[0] - freSpecBand[0] != 0){ //criar faixa do que restou para tr�s
					newSpecBand = new int[2];
					newSpecBand[0] = freSpecBand[0];
					newSpecBand[1] = spectrumBand[0] - 1;
					this.freeSpectrumBands.add(newSpecBand);
				}
				
				if(freSpecBand[1] - spectrumBand[1] != 0){ //criar faixa do que restou para frente
					newSpecBand = new int[2];
					newSpecBand[0] = spectrumBand[1] + 1;
					newSpecBand[1] = freSpecBand[1];
					this.freeSpectrumBands.add(newSpecBand);
				}
				
				slotsUsados = slotsUsados + (spectrumBand[1] - spectrumBand[0] + 1);
				
				return true;
			}			
		}
		
		return false;
	}
	
	/**
	 * Verifica se o primeiro intervalo est� contido no segundo
	 * @param inter1
	 * @param inter2
	 * @return
	 */
	private boolean isInInterval(int inter1[], int inter2[]){
		
		if(inter1[0]>=inter2[0])
			if(inter1[1]<=inter2[1])
				return true;
		
		return false;
	}
	
	/**
	 * marca como livre uma determinada faixa de espectro
	 * @param spectrumBand
	 */
	public void freeSpectrum(int spectrumBand[]){
		
		this.freeSpectrumBands.add(spectrumBand); //liberando spectro
		
		slotsUsados = slotsUsados - (spectrumBand[1] - spectrumBand[0] + 1);
		
		//necess�rio fazer o merge de espectros livres quando necess�rio
		int merge[];
		//primeiro merge com espectro livre anterior
		int aux[] = {spectrumBand[0]-1,spectrumBand[1]};
		int flor[] = this.freeSpectrumBands.floor(aux);
		
		if(flor!=null && flor[1] == (spectrumBand[0] - 1)){ //necess�rio fazer o merge
			merge = new int[2];
			merge[0] = flor[0];
			merge[1] = spectrumBand[1];
			this.freeSpectrumBands.remove(flor);
			this.freeSpectrumBands.remove(spectrumBand);
			this.freeSpectrumBands.add(merge);
			spectrumBand = merge;
		}
		
		//segundo fazer merge com espectro livre posterior
		int after[] = this.freeSpectrumBands.higher(spectrumBand);
		if(after != null && (after[0] - 1) == spectrumBand[1]){//necess�rio fazer o merge
			merge = new int[2];
			merge[0] = spectrumBand[0];
			merge[1] = after[1];
			this.freeSpectrumBands.remove(after);
			this.freeSpectrumBands.remove(spectrumBand);
			this.freeSpectrumBands.add(merge);
		}	
		
	}
	
	
	/**
	 * retorna as faixas de espectro livres no momento
	 * @return
	 */
	public List<int[]> getFreeSpectrumBands(){
		
		ArrayList<int[]> res = new ArrayList<>();
		
		for (int[] is : this.freeSpectrumBands) {
			res.add(is.clone());
		}
		
		return res;
	}
	
	/**
	 * retorna a utiliza��o do spectro variando entre 0 e 1
	 * @return
	 */
	public double utilization(){
		
		return ((double)slotsUsados)/((double)numOfSlots);
		
	}


	/**
	 * @return the slotSpectrumBand
	 */
	public double getSlotSpectrumBand() {
		return slotSpectrumBand;
	}


	/**
	 * @return the numOfSlots
	 */
	public int getNumOfSlots() {
		return numOfSlots;
	}
	
	
	
	
}

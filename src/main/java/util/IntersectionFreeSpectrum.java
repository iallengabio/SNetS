package util;

import java.util.ArrayList;
import java.util.List;

import grmlsa.Route;
import network.Link;

/**
 * This class is responsible for performing the merge between lists of spectrum.
 * 
 * @author Iallen
 */
public class IntersectionFreeSpectrum {

	 /**
     * This method returns a list of available spectrum in both lists passed by parameter
     *
     * @param l1 List<int[]>
     * @param l2 List<int[]>
     * @return List<int[]>
     */
    public static List<int[]> merge(List<int[]> l1, List<int[]> l2) {
        List<int[]> res = new ArrayList<>();
        
        int indL1 = 0;
        int indL2 = 0;
        
        int aux1[] = null;
        int aux2[] = null;
        int aux3[];
        
        while (indL1 < l1.size() && indL2 < l2.size()) {
        	
            if (aux1 == null) aux1 = l1.get(indL1).clone();
            if (aux2 == null) aux2 = l2.get(indL2).clone();
            aux3 = new int[2];
            
            if (aux1[0] >= aux2[0]) {
                aux3[0] = aux1[0];
            } else {
                aux3[0] = aux2[0];
            }
            
            if (aux1[1] < aux2[0]) { // Intervals does not overlap, pick up the next free intervals in list 1
                indL1++;
                aux1 = null;
                continue;
            }
            
            if (aux2[1] < aux1[0]) { // Intervals does not overlap, pick up the next free intervals in list 2
                indL2++;
                aux2 = null;
                continue;
            }
            
            if (aux1[1] < aux2[1]) {
                aux3[1] = aux1[1];
                aux2[0] = aux1[1] + 1;
                indL1++;
                aux1 = null;
                res.add(aux3);
                continue;
            }
            
            if (aux2[1] < aux1[1]) {
                aux3[1] = aux2[1];
                aux1[0] = aux2[1] + 1;
                indL2++;
                aux2 = null;
                res.add(aux3);
                continue;
            }
            
            if (aux1[1] == aux2[1]) {
                aux3[1] = aux2[1];
                indL1++;
                indL2++;
                aux1 = null;
                aux2 = null;
                res.add(aux3);
            }
        }
        
        return res;
    }

    /**
     * Returns a list of available spectrum on all links in the route passed by parameter
     *
     * @param route Route
     * @return List<int[]>
     */
    public static List<int[]> merge(Route route, int guardBand) {
        List<Link> links = new ArrayList<>(route.getLinkList());
        List<int[]> composition = links.get(0).getFreeSpectrumBands(guardBand);
        
        for (int i = 1; i < links.size(); i++) {
            composition = IntersectionFreeSpectrum.merge(composition, links.get(i).getFreeSpectrumBands(guardBand));
        }
        
        return composition;
    }

    /**
     * Returns the adjacent range less than the range passed by parameter.
	 * Used in optical aggregation algorithms.
     * 
     * @param band int[]
     * @param bandsFree List<int[]>
     * @return int[]
     */
    public static int[] bandAdjacentDown(int band[], List<int[]> bandsFree) {
        for (int[] fl : bandsFree) {
            if (fl[1] == (band[0] - 1)) {
                return fl;
            }
        }
        return null;
    }

    /**
     * Returns the adjacent range higher than the range passed by parameter.
     * Used in optical aggregation algorithms.
     * 
     * @param band int[]
     * @param bandsFree List<int[]>
     * @return int[]
     */
    public static int[] bandAdjacentUpper(int band[], List<int[]> bandsFree) {
        for (int[] fl : bandsFree) {
            if (fl[0] == (band[1] + 1)) {
                return fl;
            }
        }
        return null;
    }

    /**
     * Returns the number of free slots from the upper slot band
     * 
     * @param band int[]
     * @param freeBands List<int[]>
     * @return int
     */
    public static int freeSlotsUpper(int band[], List<int[]> freeBands){
        int[] aux = bandAdjacentUpper(band, freeBands);
        if(aux==null) return 0;
        else return aux[1] - aux[0] + 1;
    }

    /**
     * Returns the number of free slots from the down slot band
     * 
     * @param band int[]
     * @param freeBands List<int[]>
     * @return int
     */
    public static int freeSlotsDown(int band[], List<int[]> freeBands){
        int[] aux = bandAdjacentDown(band, freeBands);
        if(aux==null) return 0;
        else return aux[1] - aux[0] + 1;
    }

    /**
     * Returns all the guard bands of all the links of a route
     * Removes from the merge the spectrum bands that are completely filled by the guard bands
     *
     * @param route Route
     * @param merge List<int[]>
     * @return List<int[]>
     */
    public static List<int[]> getFreeSpectrumAndUpperDownGuardBands(Route route, List<int[]> merge) {
        List<Link> links = new ArrayList<>(route.getLinkList());
        ArrayList<int[]> mergeWithGuardBands = new ArrayList<>();
        
        int largerUpperGuardBand; // Largest amount of slots used for the upper guard band
        int largerDownGuardBand; // Largest amount of slots used for the down guard band
        
        int upperGuardBand[];
        int downGuardBand[];
        
        int numSlotsOfFreeBand; // Number of free band slots
        
        for(int[] freeSlotBand : merge) {
        	
        	largerUpperGuardBand = 0;
            largerDownGuardBand = 0;
        	
        	for (int i = 0; i < links.size(); i++) {
                
	    		upperGuardBand = links.get(i).getUpperGuardBandList().get(freeSlotBand[0]);
	    		downGuardBand = links.get(i).getDownGuardBandList().get(freeSlotBand[1]);
	    		
	    		if ((upperGuardBand != null) && (upperGuardBand[1] - upperGuardBand[0] + 1 > largerUpperGuardBand)) {
	    			largerUpperGuardBand = upperGuardBand[1] - upperGuardBand[0] + 1;
	    		}
	    		
	    		if ((downGuardBand != null) && (downGuardBand[1] - downGuardBand[0] + 1 > largerDownGuardBand)) {
	    			largerDownGuardBand = downGuardBand[1] - downGuardBand[0] + 1;
	    		}
        	}
            
        	numSlotsOfFreeBand = freeSlotBand[1] - freeSlotBand[0] + 1;
        	
        	// Checks if the free slot band has free slots that are not part of the guard bands
			if (numSlotsOfFreeBand - (largerUpperGuardBand + largerDownGuardBand) > 0) {
				
				int newfsb[] = new int[4];
				newfsb[0] = freeSlotBand[0]; // Initial slot of the free spectrum band
				newfsb[1] = freeSlotBand[1]; // End slot of the free spectrum band
				newfsb[2] = largerUpperGuardBand; // Amount of slots used for the upper guard band
				newfsb[3] = largerDownGuardBand; // Amount of slots used for the down guard band
				
				mergeWithGuardBands.add(newfsb);
			}
        }
        
        return mergeWithGuardBands;
    }
}

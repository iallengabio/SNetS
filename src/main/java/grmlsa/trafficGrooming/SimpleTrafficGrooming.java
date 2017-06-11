package grmlsa.trafficGrooming;

import java.util.List;

import grmlsa.GRMLSA;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

/**
 * This class represents a dummy technique of traffic grooming.
 * The objective of this algorithm is just demonstrate the traffic grooming capabilities of SNetS.
 * This algorithm only aggregates requests with the same origin and destination.
 *
 * @author Iallen
 *
 */
public class SimpleTrafficGrooming implements TrafficGroomingAlgorithm {

	@Override
	public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane grmlsa) {

		//search for active circuits with the same origin and destination of the new request.
		List<Circuit> activeCircuits = grmlsa.searchForActiveCircuits(rfc.getPair().getSource().getName(), rfc.getPair().getDestination().getName());
		
		for (Circuit circuit : activeCircuits) {
			
			//investigate if the active circuit is able to accommodate the new request
			
			//how many more slots are needed?
			int quantSlotsFinal = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth()+rfc.getRequiredBandwidth());
			int quantSlotsAtual = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int quantSlotsAMais = quantSlotsFinal - quantSlotsAtual;
			
			//Is it possible to expand the channel?
			List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());
			
			int[] faixaLivreAdjInferior = IntersectionFreeSpectrum.bandAdjacentInferior(circuit.getSpectrumAssigned(), composition);
			int quantSlotsLivresAbaixo;
			if(faixaLivreAdjInferior==null){
				quantSlotsLivresAbaixo = 0;
			}else{
				quantSlotsLivresAbaixo = faixaLivreAdjInferior[1]-faixaLivreAdjInferior[0]+1;
			}
			int[] faixaLivreAdjSuperior = IntersectionFreeSpectrum.bandAdjacentSuperior(circuit.getSpectrumAssigned(), composition);
			int quantSlotsLivresAcima;
			if(faixaLivreAdjSuperior==null){
				quantSlotsLivresAcima = 0;
			}else{
				quantSlotsLivresAcima = faixaLivreAdjSuperior[1]-faixaLivreAdjSuperior[0]+1;
			}
			
			int quantTotalSlotsLivresAdjacentes = quantSlotsLivresAcima + quantSlotsLivresAbaixo;
			
			if(quantSlotsAMais<quantTotalSlotsLivresAdjacentes){//yes, it is possible expands the channel to accommodate the new request
				
				//expandir este canal para acomodar a nova requisição
				int expansao[] = decidirExpansão(quantSlotsAMais, faixaLivreAdjInferior, faixaLivreAdjSuperior);
				int faixaExpSup[]=null;
				int faixaExpInf[]=null;
				if(expansao[0]>0){ //expanção abaixo
					faixaExpInf = new int[2];
					faixaExpInf[1] = faixaLivreAdjInferior[1];
					faixaExpInf[0] = faixaExpInf[1] - expansao[0] + 1;					
				}
				if(expansao[1]>0){ //expansão acima
					faixaExpSup = new int[2];
					faixaExpSup[0] = faixaLivreAdjSuperior[0];
					faixaExpSup[1] = faixaExpSup[0]+expansao[1]-1;
				}
				
				if(grmlsa.expandCircuit(circuit, faixaExpSup, faixaExpInf)){//deu certo a expansão
					circuit.addRequest(rfc);
					rfc.setCircuit(circuit);
					return true;
				}
				
			}
			
			
		}
		
		//failed to agregate the new request.
		//Try to create a new circuit to accommodate that.
		Circuit circuit = new Circuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		rfc.setCircuit(circuit);
		
		return grmlsa.establishCircuit(circuit);
	}
	
	/**
	 * This method decides how to expand the channel to accommodate new conexions.
	 * @param expansion
	 * @param upperFreeSlots
	 * @param lowerFreeSlots
	 * @return a vector with lenght 2, the index 0 represents the number of slots to use below, the index 1 represents the number of slots to use above.
	 */
	private int[] decidirExpansão(int expansion, int lowerFreeSlots[], int upperFreeSlots[]){
		int res[] = new int[2];
		
		if(lowerFreeSlots!=null&&lowerFreeSlots[1]-lowerFreeSlots[0]+1>=expansion){//aloca tudo em baixo
			res[0] = expansion;
			expansion = 0;
			if(lowerFreeSlots!=null){
				res[0] = lowerFreeSlots[1] - lowerFreeSlots[0] + 1;
			}else{
				res[0] = 0;
			}
			res[1] = expansion - res[0];
		}
		
		return res;
	}

	@Override
	public void finishConnection(RequestForConnection rfc, ControlPlane grmlsa) {
		// TODO Auto-generated method stub
		
		Circuit circuit = rfc.getCircuit();
		
		if(circuit.getRequests().size()==1){//The connection being terminated is the last to use this channel.
			grmlsa.releaseCircuit(circuit);
		}else{//reduce the number of slots allocated for this channel if it is possible.

			int quantSlotsFinal = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth()-rfc.getRequiredBandwidth());
			int quantSlotsAtual = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int liberar = quantSlotsAtual - quantSlotsFinal;
			int faixaLiberar[] = new int[2];
			faixaLiberar[1] = circuit.getSpectrumAssigned()[1];
			faixaLiberar[0] = faixaLiberar[1] - liberar + 1;

			grmlsa.retractCircuit(circuit, null, faixaLiberar);
			circuit.removeRequest(rfc);
		}
		
		
	}

}

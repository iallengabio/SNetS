package grmlsa.trafficGrooming;

import java.util.ArrayList;
import java.util.List;

import grmlsa.GRMLSA;
import grmlsa.Route;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import request.RequestForConexion;
import util.IntersectionFreeSpectrum;

/**
 * Este algoritmo não trabalha com roteamento multihop, a agregação óptica de tráfego é feita apenas com requisições que compartilham mesma origem e destino.
 * @author Iallen
 *
 */
public class SimpleTrafficGrooming implements TrafficGroomingAlgorithm {

	@Override
	public boolean searchCircuitsForGrooming(RequestForConexion rfc, GRMLSA grmlsa) {
		//primeiro procurar algum circuito já ativo onde pode ser feita a agregação de tráfego
		List<Circuit> circuitosAtivos = grmlsa.getControlPlane().procurarCircuitosAtivos(rfc.getPair().getSource().getName(), rfc.getPair().getDestination().getName());
		
		for (Circuit circuit : circuitosAtivos) {//existem circuitos ativos com mesma origem e mesmo destino da nova requisição
			
			//analizar a possibilidade de usar o circuito para acomodar a nova requisição
			
			//verificar quantos slots são necessários a mais para acomodar a nova requisição
			int quantSlotsFinal = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth()+rfc.getRequiredBandwidth());
			int quantSlotsAtual = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int quantSlotsAMais = quantSlotsFinal - quantSlotsAtual;
			
			//verificar a possibilidade de expandir o canal para acomodar a nova requisição
			List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());
			//verificar quantos slots livres abaixo
			int[] faixaLivreAdjInferior = IntersectionFreeSpectrum.faixaAdjacenteInferior(circuit.getSpectrumAssigned(), composition);
			int quantSlotsLivresAbaixo;
			if(faixaLivreAdjInferior==null){
				quantSlotsLivresAbaixo = 0;
			}else{
				quantSlotsLivresAbaixo = faixaLivreAdjInferior[1]-faixaLivreAdjInferior[0]+1;
			}
			
			//ferificar quantos slots livres acima
			int[] faixaLivreAdjSuperior = IntersectionFreeSpectrum.faixaAdjacenteSuperior(circuit.getSpectrumAssigned(), composition);
			int quantSlotsLivresAcima;
			if(faixaLivreAdjSuperior==null){
				quantSlotsLivresAcima = 0;
			}else{
				quantSlotsLivresAcima = faixaLivreAdjSuperior[1]-faixaLivreAdjSuperior[0]+1;
			}
			
			int quantTotalSlotsLivresAdjacentes = quantSlotsLivresAcima + quantSlotsLivresAbaixo;
			
			if(quantSlotsAMais<quantTotalSlotsLivresAdjacentes){//é possível expandir o canal
				
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
				
				if(grmlsa.getControlPlane().expandirCircuito(circuit, faixaExpSup, faixaExpInf)){//deu certo a expansão
					circuit.addRequest(rfc);
					rfc.setCircuit(circuit);
					return true;
				}
				
			}
			
			
		}
		
		//simplesmente tentar alocar um circuito para esta requisição!!!
		Circuit circuit = new Circuit();
		circuit.setPair(rfc.getPair());
		circuit.addRequest(rfc);
		rfc.setCircuit(circuit);
		
		return grmlsa.getControlPlane().allocarCircuito(circuit);
	}
	
	/**
	 * Este médodo decide quantos slots serão incrementados no canal acima e abaixo. Este método prefere sempre alocar o circuito na faixa mais abaixo, caso não seja suficiente ele aloca também a faixa acima.
	 * @param quantAMais
	 * @param faixaSup
	 * @param faixaInf
	 * @return vetor de duas posições, a posição 0 é quantidade de slots abaixo, a posição 1 é a quantidade de slots acima.
	 */
	private int[] decidirExpansão(int quantAMais, int faixaInf[], int faixaSup[]){
		int res[] = new int[2];
		
		if(faixaInf!=null&&faixaInf[1]-faixaInf[0]+1>=quantAMais){//aloca tudo em baixo
			res[0] = quantAMais;
			quantAMais = 0;
		}else{//aloca o máximo em baixo e o restante em  cima
			if(faixaInf!=null){//dá pra alocar alguma coisa em baixo
				res[0] = faixaInf[1] - faixaInf[0] + 1;
			}else{//não dá pra alocar nada em baixo
				res[0] = 0;
			}
			res[1] = quantAMais - res[0]; //aloca o restante em cima
		}
		
		return res;
	}

	@Override
	public void finalizarConexao(RequestForConexion rfc, GRMLSA grmlsa) {
		// TODO Auto-generated method stub
		
		Circuit circuit = rfc.getCircuit();
		
		if(circuit.getRequests().size()==1){//só há uma conexão ativa no circuito
			grmlsa.getControlPlane().desalocarCircuito(circuit);
		}else{
			
			//verificar quantos slots poderão ser liberados
			int quantSlotsFinal = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth()-rfc.getRequiredBandwidth());
			int quantSlotsAtual = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
			int liberar = quantSlotsAtual - quantSlotsFinal;
			int faixaLiberar[] = new int[2];
			faixaLiberar[1] = circuit.getSpectrumAssigned()[1];
			faixaLiberar[0] = faixaLiberar[1] - liberar + 1;

			grmlsa.getControlPlane().retrairCircuito(circuit, null, faixaLiberar);
			circuit.removeRequest(rfc);
		}
		
		
	}

}

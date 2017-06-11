package grmlsa.integrated;

import network.Circuit;
import network.Mesh;

/**
 * Este algoritmo RSA utiliza um roteamento de K-menores caminhos combinado com uma pol�tica FirstFit para aloca��o de espectro
 * 
 * O caminho selecionado ser� aquele em que puder ser alocada a faixa de espectro mais pr�xima do slot 0
 * @author Iallen
 *
 */
public class Integrated implements IntegratedRMLSAAlgorithmInterface{

	@Override
	public boolean rsa(Circuit request, Mesh mesh) {
		throw new UnsupportedOperationException();
	}

}

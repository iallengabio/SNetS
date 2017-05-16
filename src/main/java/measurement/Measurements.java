package measurement;

import java.util.*;

import network.*;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Measurements implements Serializable {

	 /**
     * Numero minimo de requisicoes a serem geradas
     */
    private int numMinRequest;
    /**
     * indica se a simulacao esta na fase transiente ou n�o.
     */
    private boolean transientStep;
    
    /**
     * utilizado para contar o n�mero de requisi��es geradas at� ent�o, objetivo de verificar o estado transiente
     */
	private double numGeneratedReq;  
	
    /**
     * numero da replica��o
     */
    private int replication;
    
    /**
     * ponto de carga
     */
    private int loadPoint;
    
    
    
    /**
     * calcula a probabilidade de bloqueio de circuitos
     */
    private ProbabilidadeDeBloqueio probabilidadeDeBloqueioMeasurement;    
    
    /**
     * calcular a probabilidade de bloqueio de banda
     */
    private ProbabilidadeDeBloqueioDeBanda probabilidadeDeBloqueioDeBandaMeasurement;
    
    /**
     * calcula a fragmenta��o externa
     */
    private FragmentacaoExterna fragmentacaoExterna;
    
    /**
     * Calcula a fragmenta��o relativa
     */
    private FragmentacaoRelativa fragmentacaoRelativa;
    
    /**
     * Calcula as m�tricas referentes � utiliza��o de espectro
     * 
     */
    private UtilizacaoSpectro utilizacaoSpectro;
    
    /**
     * Atualmente utilizada apenas para analizar o percentual das requisi��es geradas que exigem cada tamanho de faixa livre de espectro
     */
    private SpectrumSizeStatistics spectrumSizeStatistics;
    
    /**
     * Calcula as m�tricas referentes � utiliza��o de transmissores e receptores
     */
    private TransmitersReceiversUtilization transmitersReceiversUtilization;

    private Mesh mesh;

    public Measurements(int numMinRequest, int loadPoint, int replication, Mesh mesh) {
        this.loadPoint = loadPoint;
        this.replication = replication;
    	this.transientStep = true;
        this.numMinRequest = numMinRequest;       
        inicializarMetricas(mesh);
        this.mesh = mesh;
    }
    
    private void inicializarMetricas(Mesh mesh){
    	this.numGeneratedReq = 0.0;
        this.probabilidadeDeBloqueioMeasurement = new ProbabilidadeDeBloqueio(loadPoint, replication); 
        this.probabilidadeDeBloqueioDeBandaMeasurement = new ProbabilidadeDeBloqueioDeBanda(loadPoint, replication);
        this.fragmentacaoExterna = new FragmentacaoExterna(loadPoint, replication,mesh);
        this.utilizacaoSpectro = new UtilizacaoSpectro(loadPoint, replication,mesh);
        this.fragmentacaoRelativa = new FragmentacaoRelativa(loadPoint, replication,mesh);
        this.spectrumSizeStatistics = new SpectrumSizeStatistics(loadPoint, replication);
        this.transmitersReceiversUtilization = new TransmitersReceiversUtilization(loadPoint, replication,mesh);
    }

    // ------------------------------------------------------------------------------
    public int getReplication() {
        return this.replication;
    }

   
    // ------------------------------------------------------------------------------
    /**
     * incrementa o num. de requisi��es geradas.
     */
    public void incNumGeneratedReq() {
        this.numGeneratedReq++;      
    }
    

    public void transientStepVerify(Vector<Node> nodeList) {
        if ((transientStep) && (numGeneratedReq == 0.1 * numMinRequest)) {
            this.transientStep = false;

            inicializarMetricas(mesh);
            
        }
    }


    // ------------------------------------------------------------------------------
    /**
     * Responsavel por determinar o fim da simulacao. Caso retorne true nao deve
     * ser agendado nenhum evento, por�m aqueles j� agendados seram realizados.
     * 
     * @return boolean
     */
    public boolean finished() {
        if (this.numGeneratedReq >= this.numMinRequest) {
            return true;
        }
        return false;
    }

	
    /**
	 * @return the probBlockMeasures
	 */
	public ProbabilidadeDeBloqueio getProbabilidadeDeBloqueioMeasurement() {
		return probabilidadeDeBloqueioMeasurement;
	}

	/**
	 * @return the probabilidadeDeBloqueioDeBandaMeasurement
	 */
	public ProbabilidadeDeBloqueioDeBanda getProbabilidadeDeBloqueioDeBandaMeasurement() {
		return probabilidadeDeBloqueioDeBandaMeasurement;
	}

	/**
	 * @return the fragmentacaoExterna
	 */
	public FragmentacaoExterna getFragmentacaoExterna() {
		return fragmentacaoExterna;
	}
	
	

	/**
	 * @return the fragmentacaoRelativa
	 */
	public FragmentacaoRelativa getFragmentacaoRelativa() {
		return fragmentacaoRelativa;
	}

	/**
	 * @return the utilizacaoSpectro
	 */
	public UtilizacaoSpectro getUtilizacaoSpectro() {
		return utilizacaoSpectro;
	}

	public SpectrumSizeStatistics getSpectrumSizeStatistics() {
		return spectrumSizeStatistics;
	}

	public TransmitersReceiversUtilization getTransmitersReceiversUtilization() {
		return transmitersReceiversUtilization;
	}

	

    
    
}

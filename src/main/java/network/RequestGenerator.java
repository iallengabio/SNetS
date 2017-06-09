package network;

import simulator.*;
import simulator.eventListeners.ArriveRequestForConexionListener;

import java.io.Serializable;

import request.RequestForConnection;
import util.RandGenerator;

public class RequestGenerator implements Serializable {

  private Pair pair;
  private double bandwidth; // em bits por segundo (bps)
  private double holdRate;
  private double arrivedRate;  
  private double incLoad;
  private double atualTimeHours;
  private  RandGenerator randGenerator;

  /**
   * 
   * @param pair
   * @param bandwidth em bits por segundo (bps)
   * @param holdRate
   * @param arrivedRate
   * @param incLoad
   */
  public RequestGenerator(Pair pair, double bandwidth, double holdRate, double arrivedRate,double incLoad, RandGenerator randGenerator) {
    this.pair = pair;
    this.bandwidth = bandwidth;	  
	this.holdRate = holdRate;
    this.arrivedRate = arrivedRate;
    this.incLoad=incLoad;
    atualTimeHours = 0;
    this.randGenerator = randGenerator;
  }

  //---------------------------------------------------------------------------
  /**
   * Agenda uma nova requisicao de conexao,
   * o método irá calcular o instante da próxima requisição e agendar o evento correspondente
   */
  public void scheduleNextRequest(EventMachine em, ArriveRequestForConexionListener arriveRequest) {
	RequestForConnection r = new RequestForConnection();
	double arriveTimeHours = randGenerator.negexp(arrivedRate);
    atualTimeHours = atualTimeHours + arriveTimeHours;
    r.setTimeOfRequestHours(atualTimeHours);
    double holdTimeHours = randGenerator.negexp(getHoldRate());
  	double finalizeTimeHours = r.getTimeOfRequestHours() + holdTimeHours;
  	r.setTimeOfFinalizeHours(finalizeTimeHours);
    r.setPair(pair);
    r.setRequiredBandwidth(bandwidth);
    r.setRequestGenerator(this);
    Event e = new Event(r, arriveRequest, atualTimeHours);
    em.insert(e);
  }

  //---------------------------------------------------------------------------
  /**
   * Retorna a taxa de chegada de requisicoes
   * @return double
   */
  public double getArrivedRate() {
    return arrivedRate;
  }

 

  //---------------------------------------------------------------------------
  /**
   * Retorna a taxa de atendimento de requisicoes.
   * @return double
   */
  public double getHoldRate() {
    return holdRate;
  }

 

  //---------------------------------------------------------------------------
  /**
   * Configura a taxa de chegada de requisicoes.
   * @param arrivedRate double
   */
  public void setArrivedRate(double arrivedRate) {
    this.arrivedRate = arrivedRate;
  }



  //---------------------------------------------------------------------------
  /**
   * Configura a taxa de atendimento de requisicoes.
   * @param holdRate double
   */
  public void setHoldRate(double holdRate) {
    this.holdRate = holdRate;
  }

  

  //---------------------------------------------------------------------------





  //---------------------------------------------------------------------------
  /**
   * Retorna o valor de incremento para a taxa de chegada.
   * @return double
   */
  public double getIncLoad() {
    return incLoad;
  }


  //---------------------------------------------------------------------------
  /**
   * Configura o valor de incremento para a taxa de chegada.
   * @param incLoad double
   */
  public void setIncLoad(double incLoad) {
    this.incLoad = incLoad;
  }

/**
 * @return the pair
 */
public Pair getPair() {
	return pair;
}

/**
 * @param pair the pair to set
 */
public void setPair(Pair pair) {
	this.pair = pair;
}

/**
 * @return the bandwidth
 */
public double getBandwidth() {
	return bandwidth;
}

/**
 * @param bandwidth the bandwidth to set
 */
public void setBandwidth(double bandwidth) {
	this.bandwidth = bandwidth;
}

public void incArrivedRate(int mult){
	this.arrivedRate = this.arrivedRate + mult * this.incLoad;
}


  
  
}

package network;

import request.RequestForConnection;
import simulator.Event;
import simulator.EventMachine;
import simulator.eventListeners.ArriveRequestForConexionListener;
import util.RandGenerator;

import java.io.Serializable;

/**
 * This class represents the network request generator
 * 
 * @author Iallen
 */
public class RequestGenerator implements Serializable {

  private Pair pair;
  private double bandwidth; // In bits per second (bps)
  private double holdRate;
  private double arrivedRate;  
  private double incLoad;
  private double atualTimeHours;
  private  RandGenerator randGenerator;

  /**
   * Creates a new instance of RequestGenerator
   * 
   * @param pair Pair
   * @param bandwidth double
   * @param holdRate double
   * @param arrivedRate double
   * @param incLoad double
   * @param randGenerator RandGenerator
   */
  public RequestGenerator(Pair pair, double bandwidth, double holdRate, double arrivedRate,double incLoad, RandGenerator randGenerator) {
    this.pair = pair;
    this.bandwidth = bandwidth;	  
	this.holdRate = holdRate;
    this.arrivedRate = arrivedRate;
    this.incLoad = incLoad;
    this.atualTimeHours = 0;
    this.randGenerator = randGenerator;
  }

  /**
   * Schedule a new connection request, the method will calculate the instant 
   * of the next request and schedule the corresponding event
   * 
   * @param em EventMachine
   * @param arriveRequest ArriveRequestForConexionListener
   */
  public void scheduleNextRequest(EventMachine em, ArriveRequestForConexionListener arriveRequest) {
	RequestForConnection rfc = new RequestForConnection();
	double arriveTimeHours = randGenerator.negexp(arrivedRate);
    atualTimeHours = atualTimeHours + arriveTimeHours;
    rfc.setTimeOfRequestHours(atualTimeHours);
    double holdTimeHours = randGenerator.negexp(getHoldRate());
  	double finalizeTimeHours = rfc.getTimeOfRequestHours() + holdTimeHours;
  	rfc.setTimeOfFinalizeHours(finalizeTimeHours);
    rfc.setPair(pair);
    rfc.setRequiredBandwidth(bandwidth);
    rfc.setRequestGenerator(this);
    Event e = new Event(rfc, arriveRequest, atualTimeHours);
    em.insert(e);
  }

  /**
   * Returns the arrival rate of requests
   * 
   * @return double
   */
  public double getArrivedRate() {
    return arrivedRate;
  }

  /**
   * Return the hold rate of requests
   * @return double
   */
  public double getHoldRate() {
    return holdRate;
  }

  /**
   * Configures the arrival rate of requests
   * 
   * @param arrivedRate double
   */
  public void setArrivedRate(double arrivedRate) {
    this.arrivedRate = arrivedRate;
  }

  /**
   * Configures the hold rate of requests
   * 
   * @param holdRate double
   */
  public void setHoldRate(double holdRate) {
    this.holdRate = holdRate;
  }

  /**
   * Returns the increment value for the arrival rate
   * 
   * @return double
   */
  public double getIncLoad() {
    return incLoad;
  }

  /**
   * Sets the increment value for the arrival rate
   * 
   * @param incLoad double
   */
  public void setIncLoad(double incLoad) {
    this.incLoad = incLoad;
  }

  /**
	 * Returns the pair
	 * 
	 * @return Pair the pair
	 */
  public Pair getPair() {
	  return pair;
  }

  /**
	 * Sets the pair
	 * 
	 * @param pair Pair the pair to set
	 */
  public void setPair(Pair pair) {
	  this.pair = pair;
  }
  
  /**
	 * Returns the bandwidth
	 * 
	 * @return double the bandwidth
	 */
  public double getBandwidth() {
	  return bandwidth;
  }

  /**
	 * Sets the bandwidth
	 * 
	 * @param bandwidth double the bandwidth to set
	 */
  public void setBandwidth(double bandwidth) {
	  this.bandwidth = bandwidth;
  }
  
  /**
	 * Increases the arrival rate
	 * 
	 * @param mult int
	 */
  public void incArrivedRate(int mult){
	  this.arrivedRate = this.arrivedRate + mult * this.incLoad;
  }
  
}

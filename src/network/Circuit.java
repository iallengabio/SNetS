package network;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import request.RequestForConexion;

import java.util.ArrayList;
import java.util.List;

public class Circuit {


    protected Pair pair;
    protected Route route;
    protected int spectrumAssigned[];
    protected Modulation modulation;
    protected List<RequestForConexion> requests; //requisições atendidas por este circuito

    public Circuit() {
        requests = new ArrayList<>();
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
     * @return the route
     */
    public Route getRoute() {
        return route;
    }

    /**
     * @param route the route to set
     */
    public void setRoute(Route route) {
        this.route = route;
    }

    /**
     * @return the requiredBandwidth
     */
    public double getRequiredBandwidth() {
        double res = 0.0;

        for (RequestForConexion r : requests) {
            res += r.getRequiredBandwidth();
        }

        return res;
    }

    public Node getSource() {
        return pair.getSource();
    }

    public Node getDestination() {
        return pair.getDestination();
    }


    public int[] getSpectrumAssigned() {
        return spectrumAssigned;
    }

    public void setSpectrumAssigned(int sa[]) {
        spectrumAssigned = sa;
    }

    /**
     * @return the modulation
     */
    public Modulation getModulation() {
        return modulation;
    }

    /**
     * @param modulation the modulation to set
     */
    public void setModulation(Modulation modulation) {
        this.modulation = modulation;
    }


    public void addRequest(RequestForConexion rfc) {
        requests.add(rfc);
    }

    public void removeRequest(RequestForConexion rfc) {
        requests.remove(rfc);
    }


    public List<RequestForConexion> getRequests() {
        return requests;
    }


}

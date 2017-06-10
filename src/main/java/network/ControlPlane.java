package network;

import grmlsa.GRMLSA;
import grmlsa.Route;
import request.RequestForConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that represents the control plane.
 * This class should make calls to RSA algorithms, store routes in case of fixed routing, 
 * provide information about the state of the network, etc.
 *
 * @author Iallen
 */
public class ControlPlane {
	
    private Mesh mesh;

    private GRMLSA grmlsa;

    /**
     * The first key represents the source node.
     * The second key represents the destination node.
     */
    private HashMap<String, HashMap<String, List<Circuit>>> activeCircuits;

    /**
     * Instance the control plane with the list of active circuits in empty
     */
    public ControlPlane() {
        activeCircuits = new HashMap<>();
    }

    /**
     * Configures the network mesh
     * 
     * @param mesh the mesh to set
     */
    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
        // Initialize the active circuit list
        for (Node node1 : mesh.getNodeList()) {
            HashMap<String, List<Circuit>> hmAux = new HashMap<>();
            for (Node node2 : mesh.getNodeList()) {
                hmAux.put(node2.getName(), new ArrayList<Circuit>());
            }
            activeCircuits.put(node1.getName(), hmAux);
        }
    }

    /**
     * Returns the network mesh
     * 
     * @return the mesh
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Configures the GRMLSA
     * 
     * @param grmlsa the rsa to set
     */
    public void setGrmlsa(GRMLSA grmlsa) {
        this.grmlsa = grmlsa;
    }
    
    /**
     * This method tries to satisfy a certain request by checking if there are available resources 
     * for the establishment of the circuit
     *
     * @param rfc RequestForConnection
     * @return boolean
     */
    public boolean handleRequisition(RequestForConnection rfc) {
        return this.grmlsa.handleRequisition(rfc);
    }

    /**
     * This method ends a connection
     * 
     * @param rfc RequestForConnection
     */
    public void finalizeConnection(RequestForConnection rfc) {
        this.grmlsa.finalizeConnection(rfc);
    }

    /**
     * Releases the resources being used by a given circuit
     *
     * @param circuit
     */
    public void releaseCircuit(Circuit circuit) {
        Route r = circuit.getRoute();

        releaseSpectrum(circuit.getSpectrumAssigned(), r.getLinkList());

        // Release Tx and Rx
        circuit.getSource().getTxs().freeTx();
        circuit.getDestination().getRxs().freeRx();

        activeCircuits.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).remove(circuit);
    }

    /**
     * This method is called after executing RSA algorithms to allocate resources in the network
     *
     * @param circuit Circuit
     */
    private void allocateCircuit(Circuit circuit) {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[] = circuit.getSpectrumAssigned();
        
        allocateSpectrum(chosen, links);
    }

    /**
     * This method allocates the spectrum band selected for the circuit in the route links
     * 
     * @param chosen int[]
     * @param links List<Link>
     * @return boolean
     */
    private boolean allocateSpectrum(int chosen[], List<Link> links) {
        boolean notAbleAnymore = false;
        Link l;
        int i;
        
        for (i = 0; i < links.size(); i++) {
            l = links.get(i);
            notAbleAnymore = !l.useSpectrum(chosen);
            if (notAbleAnymore) break; // Some resource was no longer available, cancel the allocation
        }

        return notAbleAnymore;
    }

    /**
     * This method releases the allocated spectrum for the circuit
     * 
     * @param chosen int[]
     * @param links List<Link>
     */
    private void releaseSpectrum(int chosen[], List<Link> links) {
        // Release spectrum
        for (Link link : links) {
            link.freeSpectrum(chosen);
        }
    }
    
    /**
     * This method tries to establish a new circuit in the network
     *
     * @param circuit Circuit
     * @return true if the circuit has been successfully allocated, false if the circuit can not be allocated.
     */
    public boolean establishCircuit(Circuit circuit) {

        if (circuit.getSource().getTxs().allocateTx()) {// Can allocate transmitter
            if (circuit.getDestination().getRxs().allocateRx()) {// Can allocate receiver
                if (this.grmlsa.createNewCircuit(circuit)) { // Can allocate spectrum

                	// QoT verification

                    this.allocateCircuit(circuit);
                    activeCircuits.get(circuit.getSource().getName()).get(circuit.getDestination().getName()).add(circuit);

                    return true;
                    
                } else {// Release transmitter e receiver
                    circuit.getSource().getTxs().freeTx();
                    circuit.getDestination().getRxs().freeRx();
                }
            } else {// Release transmitter
                circuit.getSource().getTxs().freeTx();
            }
        }

        return false;
    }

    /**
     * Increase the number of slots used by a given circuit
     *
     * @param circuit Circuit
     * @param upperBand int[]
     * @param bottomBand int[]
     * @return boolean
     */
    public boolean expandCircuit(Circuit circuit, int upperBand[], int bottomBand[]) {

        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[];
        int specAssigAt[] = circuit.getSpectrumAssigned();
        
        if (upperBand != null) {
            chosen = upperBand;
            allocateSpectrum(chosen, links);
            specAssigAt[1] = upperBand[1];
        }
        
        if (bottomBand != null) {
            chosen = bottomBand;
            allocateSpectrum(chosen, links);
            specAssigAt[0] = bottomBand[0];
        }
        circuit.setSpectrumAssigned(specAssigAt);

        return true;
    }

    /**
     * Reduces the number of slots used by a given circuit
     *
     * @param circuit Circuit
     * @param bottomBand int[]
     * @param upperBand int[]
     */
    public void retractCircuit(Circuit circuit, int bottomBand[], int upperBand[]) {
        Route route = circuit.getRoute();
        List<Link> links = new ArrayList<>(route.getLinkList());
        int chosen[];
        int specAssigAt[] = circuit.getSpectrumAssigned();
        
        if (bottomBand != null) {
            chosen = bottomBand;
            releaseSpectrum(chosen, links);
            specAssigAt[0] = bottomBand[1] + 1;
        }
        
        if (upperBand != null) {
            chosen = upperBand;
            releaseSpectrum(chosen, links);
            specAssigAt[1] = upperBand[0] - 1;
        }
        
        circuit.setSpectrumAssigned(specAssigAt);
    }

    /**
     * To find active circuits on the network with specified source and destination
     *
     * @param source String
     * @param destination String
     * @return List<Circuit>
     */
    public List<Circuit> searchForActiveCircuits(String source, String destination) {
        return this.activeCircuits.get(source).get(destination);
    }

}

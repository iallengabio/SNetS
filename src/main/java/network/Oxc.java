package network;

import java.io.Serializable;
import java.util.Vector;

/**
 * This class represents the OXC of the network
 * 
 * @author Iallen
 */
public class Oxc implements Serializable {

    private String name;
    private Vector<Link> linksList;

    /**
     * Creates a new instance of Oxc.
     *
     * @param name String name Oxc
     */
    public Oxc(String name) {
        this.linksList = new Vector<Link>();
        this.name = name;
    }
    
    /**
     * Getter for property name
     *
     * @return String name this Oxc.
     */
    public String getName() {
        return name;
    }

    /**
     * Add a link to the list of links
     *
     * @param link Link
     * @return true case successufully added Link l; false otherwise.
     */
    public boolean addLink(Link link) {
        if (link.getSource().equals(this)) {
            linksList.add(link);
            return true;
        }
        return false;
    }

    /**
     * Return Link to Oxc n.
     *
     * @param n Oxc.
     * @return Link link to Oxc n.
     */
    public Link linkTo(Oxc n) {
        Link auxLink;
        for (int i = 0; i < linksList.size(); i++) {
            auxLink = linksList.get(i);
            if (auxLink.adjacent(n))
                return auxLink;
        }
        return null;
    }

    /**
     * Getter for property linksList
     *
     * @return Vector with links
     */
    public Vector<Link> getLinksList() {
        return linksList;
    }

    /**
     * Remove Link to Oxc n
     *
     * @param n Oxc
     * @return true case success removed Link to Oxc n; false otherwise.
     */
    public boolean removeLink(Oxc n) {
        Link auxLink;
        for (int i = 0; i < linksList.size(); i++) {
            auxLink = linksList.get(i);
            if (auxLink.adjacent(n)) {
                return linksList.remove(auxLink);
            }
        }
        return false;
    }

    /**
     * Is Oxc x adjacent of this Oxc.
     *
     * @param x Oxc
     * @return true if Oxc x is adjacent of this Oxc; false otherwise.
     */
    public boolean isAdjacent(Oxc x) {
        Link auxLink;

        for (int i = 0; i < linksList.size(); i++) {
            auxLink = linksList.get(i);
            if (auxLink.adjacent(x))
                return true;
        }
        return false;
    }

    /**
     * Getter for property cost for Oxc x.
     *
     * @param x Oxc.
     * @return double cost.
     */
    public double getCost(Oxc x) {
        Link auxLink;
        for (int i = 0; i < linksList.size(); i++) {
            auxLink = linksList.get(i);
            if (auxLink.adjacent(x))
                return auxLink.getCost();
        }
        return -1;
    }

    /**
     * Return all adjacents Oxcs of this node.
     *
     * @return Vector with all adjacentes
     */
    public Vector<Oxc> getAllAdjacents() {
        Vector<Oxc> list = new Vector<Oxc>();
        Link auxLink;
        for (int i = 0; i < linksList.size(); i++) {
            auxLink = linksList.get(i);
            list.add(auxLink.getDestination());
        }
        return list;
    }

}

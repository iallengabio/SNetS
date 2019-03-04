package grmlsa;

import network.Link;
import network.Node;

import java.io.Serializable;
import java.util.Vector;

/**
 * This class represents a route between two network nodes
 * 
 * @author Iallen
 */
@SuppressWarnings("serial")
public class Route implements Serializable, Comparable<Route> {

	// List of nodes that are present on the route
    private Vector<Node> nodeList;
    
    // List of links that are present on the route
    private Vector<Link> linkList;
    
    // Route distance
    private double distanceAllLinks;

    /**
     * Creates a new instance of Route with a given list of nodes
     *
     * @param nodeList Vector<Node>
     */
    public Route(Vector<Node> nodeList) {
        this.nodeList = nodeList;
        this.computeLinks();
    }

    /**
     * Creates a new instance of Route with empty list of nodes
     */
    public Route() {
        this.nodeList = new Vector<Node>();
    }

    /**
     * Creates the list of links from the route node list
     */
    private void computeLinks() {
        this.distanceAllLinks = 0;
        this.linkList = new Vector<>();
        for (int i = 0; i < this.nodeList.size() - 1; i++) {
            Link l = this.nodeList.get(i).getOxc().linkTo(this.nodeList.get(i + 1).getOxc());
            if (l != null) {
                this.linkList.add(l);
                this.distanceAllLinks += this.linkList.get(i).getDistance();
            }
        }
    }

    /**
     * Getter for source Node.
     *
     * @return Node. The source Node of this Route.
     */
    public Node getSource() {
        if (nodeList.size() > 0) {
            return (nodeList.firstElement());
        } else {
            System.out.println("The node list is empty.");
            return null;
        }
    }

    /**
     * Getter for destination node.
     *
     * @return Node. The destination node of route.
     */
    public Node getDestination() {
        if (nodeList.size() > 0) {
            return (nodeList.lastElement());
        } else {
            System.out.println("The node list is empty.");
            return null;
        }
    }

    /**
     * Returns the Node at the specified position in this Route.
     *
     * @param index index of Node to return.
     * @return Node at the specified index.
     */
    public Node getNode(int index) {
        return (nodeList.get(index));
    }

   /**
    * Returns the Link at the specified position in this Route.
    *
    * @param index index of Link to return.
    * @return Node at the specified index.
    */
    public Link getLink(int index) {
        return (linkList.get(index));
    }

    /**
     * Returns next Node at Node n.
     *
     * @param n Node.
     * @return Node. Next Node at Node n.
     */
    public Node getNext(Node n) {
        Node aux;
        String nameN = n.getName();
        for (int i = 0; i < nodeList.size() - 1; i++) {
            aux = nodeList.get(i);
            if (aux.getName().equalsIgnoreCase(nameN)) {
                return nodeList.get(i + 1);
            }
        }
        System.err.println("ERROR: next node was not found for node : " + n.getName());
        System.out.println("ERRO: class Route");
        return null;
    }

    /**
     * Checks whether there is a common link between two routes
     *
     * @param a Route
     * @return true if existent link; false otherwise.
     */
    public boolean linkEquals(Route a) {
        Node ori1, ori2, prox1, prox2;

        for (int i = 0; i <= this.nodeList.size() - 2; i++) {
            ori1 = (this.nodeList.get(i));
            for (int j = 0; j <= a.nodeList.size() - 2; j++) {
                ori2 = (a.nodeList.get(j));
                if (ori1.equals(ori2)) { // There are at least two equal nodes !!!

                    prox1 = this.getNext(ori1);
                    prox2 = a.getNext(ori2);
                    if (prox1.equals(prox2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Getter for property nodeList.
     *
     * @return Vector<Node>.
     */
    public Vector<Node> getNodeList() {
        return this.nodeList;
    }

    /**
     * Getter for property linkList.
     *
     * @return Vector<Link>.
     */
    public Vector<Link> getLinkList() {
        return this.linkList;
    }

    /**
     * Returns the number of Nodes in this Route.
     *
     * @return int. The number of Nodes in this Route.
     */
    public int size() {
        return nodeList.size();
    }

    /**
     * Returns the number of hops in this Route.
     *
     * @return int. The number of hops in this Route.
     */
    public int getHops() {
        return this.nodeList.size() - 1;
    }

    /**
     * Print Route.
     */
    public void printRoute() {
        for (int i = 0; i < nodeList.size(); i++) {
            System.out.print(nodeList.get(i).getName() + ";");
        }
        System.out.println();
    }

    /**
     * Print Route.
     */
    public String getRouteInString() {
        String list = "";
        for (int i = 0; i < nodeList.size(); i++) {
            list = list + nodeList.get(i).getName() + ",";
        }
        return list;
    }

    /**
     * Checks if the route contains a given link
     *
     * @param link Link
     * @return boolean
     */
    public boolean containThisLink(Link link) {
        return this.linkList.contains(link);
    }

    /**
     * Returns the sum of the distance of all the links.
     * The distance from the route.
     *
     * @return double
     */
    public double getDistanceAllLinks() {
        return this.distanceAllLinks;
    }

    /**
     * Returns the adjacent node
     *
     * @param link Link
     * @return Node
     */
    public Node getNodeAdjacent(Link link) {
        int pos = this.linkList.indexOf(link);
        if (pos != -1) {
            return this.getNodeList().get(pos + 1);
        } else {
            return null;
        }
    }

    /**
     * Configures the links list of route
     * 
     * @param linkList the linkList to set
     */
    public void setLinkList(Vector<Link> linkList) {
        this.linkList = linkList;
    }
    
    /**
     * Configures the nodes of route
     * 
     * @param nodeList the nodeList to set
     */
    public void setNodeList(Vector<Node> nodeList) {
		this.nodeList = nodeList;
	}

    /**
     * Checks if a given route is equal to the current route
     * 
     * @param o Route
     */
    @Override
    public int compareTo(Route o) {
        Double d1 = this.distanceAllLinks;
        Double d2 = o.distanceAllLinks;
        if (d1 < d2) return -1;
        else return 1;
    }

    /**
     * Checks if a given route is equal to the current route
     * 
     * @param o Route
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Route) {
            return this.getRouteInString().equals(((Route) o).getRouteInString());
        }
        return false;
    }

    /**
     * Adds a node to the route
     * 
     * @param n Node
     */
    public void addNode(Node n) {
        this.nodeList.add(n);
        this.computeLinks();
    }

    /**
     * Returns a clone of the route
     * 
     * @return Route
     */
    @SuppressWarnings("unchecked")
	public Route clone() {
        Route res = new Route();
        res.distanceAllLinks = this.distanceAllLinks;
        res.nodeList = (Vector<Node>) this.nodeList.clone();
        res.linkList = (Vector<Link>) this.linkList.clone();
        return res;
    }

}

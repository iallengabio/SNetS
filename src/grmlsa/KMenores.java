package grmlsa;

import network.Mesh;
import network.Node;

import java.util.*;

public class KMenores {
    private static final String DIV = "-";

    private int k;

    private HashMap<String, List<Route>> routesForAllPairs;

    /**
     * @param mesh topologia de rede
     * @param k    quantidade de rotas a serem computadas para cada par(o,d)
     */
    public KMenores(Mesh mesh, int k) {
        this.k = k;
        this.computeAllRoutes(mesh);

    }

    private void computeAllRoutes(Mesh mesh) {
        routesForAllPairs = new HashMap<>();
        for (Node n1 : mesh.getNodeList()) {
            for (Node n2 : mesh.getNodeList()) {
                if (n1 == n2)
                    continue;

                routesForAllPairs.put(n1.getName() + DIV + n2.getName(),
                        this.computeRoutes(n1, n2, mesh));
            }
        }
    }

    /**
     * calcula as k menores rotas entre dois n?s
     *
     * @param n1
     * @param n2
     * @param mesh
     * @return
     */
    private List<Route> computeRoutes(Node n1, Node n2, Mesh mesh) {
        TreeSet<Route> res = new TreeSet<>();

        List<Vector<Node>> rotasEmConstrucao = new ArrayList<>();

        Vector<Node> ini = new Vector<>();
        ini.add(n1);

        rotasEmConstrucao.add(ini);

        while (!rotasEmConstrucao.isEmpty()) {
            //System.out.println("\n\nrotas em constru??o");
            for (Vector<Node> vector : rotasEmConstrucao) {
                String aux = "";
                for (Node node : vector) {
                    aux = aux + node.getName() + "->";
                }
                //System.out.println(aux);
            }


            Vector<Node> expand = rotasEmConstrucao.remove(0);

            if (expand.get(expand.size() - 1).equals(n2)) {// rota encontrada
                Route r = new Route(expand);


                res.add(r);
                continue;
            }

            for (Node node : mesh.getAdjacents(expand.get(expand.size() - 1))) {
                if (expand.contains(node))
                    continue; // n?o criar rotas com loops
                Vector<Node> aux = (Vector<Node>) expand.clone();
                aux.add(node);
                rotasEmConstrucao.add(aux);
            }
        }


        if (res.size() > k)
            return new ArrayList<>(res).subList(0, k);
        else
            return new ArrayList<>(res);
    }

    public List<Route> getRoutes(Node n1, Node n2) {
        return this.routesForAllPairs.get(n1.getName() + DIV + n2.getName());
    }

}

package simulationControl.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Iallen on 04/05/2017.
 */
public class TempMain {

    public static void main(String args[]){

        String path  = "./simulations/old/usa_cs/network";

        try {
            NetworkConfig nc = new NetworkConfig();
            Scanner sc = new Scanner(new File(path));
            sc.nextLine(); // Line only with 'node:'

            String descNode = sc.nextLine();

            while(!descNode.equals("links:")){// Description of nodes
                String[] split = descNode.split(";");
                if(split.length==3) {
                    nc.getNodes().add(new NetworkConfig.NodeConfig(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), 0));
                }else{//tx and rx infinite
                    nc.getNodes().add(new NetworkConfig.NodeConfig(descNode, 100000, 100000, 0));
                }

                descNode = sc.nextLine();
            }

            while(sc.hasNextLine()){// Links description
                String descLink = sc.nextLine();
                String[] split = descLink.split(";");
                nc.getLinks().add(new NetworkConfig.LinkConfig(split[0],split[1],Integer.parseInt(split[3]),Double.parseDouble(split[4]),Double.parseDouble(split[5])));
            }
            //nc.setGuardBand(1);

            Gson gson = new GsonBuilder().create();
            System.out.println(gson.toJson(nc));
            
            sc.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

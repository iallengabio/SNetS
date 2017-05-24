package simulationControl;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import simulationControl.parsers.TrafficConfig;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Aprendendo a usar o firebase com java
 * Created by Iallen on 23/05/2017.
 */
public class TesteFire {

    private static boolean waiting = true;

    public static void main(String args[]){
        try{
            FileInputStream serviceAccount =
                    new FileInputStream("private-key-firebase.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseUrl("https://snets-2905e.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);

            FirebaseDatabase.getInstance().getReference("simulations/-KkodIszNc26Ahosh8UZ/trafficConfig").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //TrafficConfig tc = (TrafficConfig) dataSnapshot.getValue(false);

                    //System.out.println(dataSnapshot.getValue(TrafficConfig.class));

                    Gson gson = new GsonBuilder().create();
                    TrafficConfig tc = gson.fromJson(dataSnapshot.getValue(false).toString(), TrafficConfig.class);

                    System.out.println("passou " + tc.getRequestGenerators().get(0).getSource());
                    System.out.println("passou " + tc.getRequestGenerators().get(0).getDestination());
                    System.out.println("passou " + tc.getRequestGenerators().get(0).getArrivalRate());
                    System.out.println("passou " + tc.getRequestGenerators().get(0).getArrivalRateIncrease());
                    System.out.println("passou " + tc.getRequestGenerators().get(0).getBandwidth());
                    System.out.println("passou " + tc.getRequestGenerators().get(0).getHoldRate());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("error");
                }
            });


            while(waiting){
                /*try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

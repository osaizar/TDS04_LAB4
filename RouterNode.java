import javax.swing.*;
import java.util.Arrays;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private int[] costs = new int[RouterSimulator.NUM_NODES];

  //  distanceTable[via][to]
  private int[][] distanceTable = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
  private int[] dv = new int[RouterSimulator.NUM_NODES]; // best route
  private int[] route = new int[RouterSimulator.NUM_NODES]; // first hop

  private boolean poison = true;

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    myID = ID;
    this.sim = sim;
    myGUI = new GuiTextArea("  Output window for Router #"+ ID + "  ");

    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

    for (int i = 0; i < costs.length; i++){
      distanceTable[i][i] = costs[i];
      dv[i] = costs[i];
    }

    broadcastCosts();
  }

  public void broadcastCosts(){
    int[] tmpdv = new int[dv.length];

    for(int rId = 0; rId < dv.length; rId++){
      System.arraycopy(dv, 0, tmpdv, 0, RouterSimulator.NUM_NODES);
      if (rId != myID){
        if (poison){
          for (int to = 0; to < dv.length; to++){
            if (rId == route[to] && rId != to && myID != to){
              tmpdv[to] = RouterSimulator.INFINITY;
            }
          }
        }
        sendUpdate(new RouterPacket(myID, rId, tmpdv));
      }
    }
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {

    for (int i = 0; i < pkt.mincost.length; i++){
      //the cost to get to i from pkt.src is the cost to get to pkt.src + the cost to get to i from pkt.src
      distanceTable[pkt.sourceid][i] = pkt.mincost[i]+costs[pkt.sourceid];
      if (distanceTable[pkt.sourceid][i] > RouterSimulator.INFINITY) {
        distanceTable[pkt.sourceid][i] = RouterSimulator.INFINITY;
      }
    }
    updatedv();
  }

  public void updatedv(){
    boolean changes = false;
    int tmpdv[] = new int[dv.length];
    for(int to = 0; to < dv.length; to++){
      if (to != myID){
        int cost = 999;
        for(int via = 0; via < dv.length; via++){
          if (via != myID){
            if (distanceTable[via][to] <= cost && distanceTable[via][to] > 0){
              cost = distanceTable[via][to];
              route[to] = via;
            }
          }
        }
        tmpdv[to] = cost;
      }
    }
    if (!Arrays.equals(tmpdv, dv)){
      System.arraycopy(tmpdv, 0,  dv, 0, RouterSimulator.NUM_NODES);
      broadcastCosts();
    }
  }



  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);
  }


  //--------------------------------------------------
  public void printDistanceTable() {
    String line = "";

    myGUI.println("Current table for " + myID +
    "  at time " + sim.getClocktime());

    myGUI.println("\nDistance Table:");
    line = "via    |";
    for (int i = 0; i < costs.length; i++)
    line += "\t"+i;

    myGUI.println(line);

    for (int i = 0; i < line.length(); i++){
      myGUI.print("-------");
    }

    myGUI.print("\n");

    for (int i = 0; i < costs.length; i++){
      line = "to "+i+" |";
      for (int k = 0; k < costs.length; k++){
        if(k != myID && i != myID) {
          if (distanceTable[k][i] == 0)line += "\t?";
          else line += "\t"+distanceTable[k][i];
        }else{
          line += "\t-";
        }
      }
      myGUI.println(line);
    }

    myGUI.println("\ncosts table:");
    line = "To router #  |";
    for (int i = 0; i < costs.length; i++)
    line += "\t"+i;

    myGUI.println(line);

    for (int i = 0; i < line.length(); i++){
      myGUI.print("----");
    }

    line = "\nDV:";
    for (int i = 0; i < costs.length; i++)
    line += "\t"+dv[i];

    myGUI.println(line);

    line = "First hop:";
    for (int i = 0; i < dv.length; i++){
      if (i != myID)line += "\t"+route[i];
      else line +=  "\t-";
    }


    myGUI.println(line);
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    myGUI.println("New cost to get to "+dest+": "+newcost+" (old cost:"+distanceTable[dest][dest]+")");
    int oldcost = distanceTable[dest][dest];
    for (int to = 0; to < RouterSimulator.NUM_NODES; to++){
      if (to != myID && distanceTable[dest][to] < RouterSimulator.INFINITY){
        distanceTable[dest][to] -= oldcost;
        distanceTable[dest][to] += newcost;
      }
    }
    distanceTable[dest][dest] = newcost;
    costs[dest] = newcost;
    updatedv();
    broadcastCosts();
  }

}

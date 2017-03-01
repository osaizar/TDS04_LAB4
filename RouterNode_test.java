import javax.swing.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private int[] costs = new int[RouterSimulator.NUM_NODES];

  //  dv[via][to]
  private int[][] dv = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
    for (int i = 0; i < pkt.mincost.length; i++){
      if (i != pkt.sourceId){
        //the cost to get to i from pkt.src is the cost to get to pkt.src + the cost to get to i from pkt.src
        dv[pkt.sourceId][i] = pkt.mincost[i]+dv[pkt.sourceId][pkt.sourceId];
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

    myGUI.println("\nDV:");
    line = "via    |";
    for (int i = 0; i < costs.length; i++)
      line += "\t"+i;

    myGUI.println(line);

    for (int i = 0; i < line.length(); i++){
      myGUI.print("-----");
    }

    myGUI.print("\n");

    for (int i = 0; i < costs.length; i++){
      line = "to "+i+" |";
      for (int k = 0; k < costs.length; k++){
        if(k != myID && i != myID) line += "\t"+dv[k][i];
        else line += "\t-";
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

    line = "\ncost:";
    for (int i = 0; i < costs.length; i++)
      line += "\t"+costs[i];

    myGUI.println(line);
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    myGUI.println("New cost to get to "+dest+": "+newcost+" (old cost:"+costs[dest]+")");
    dv[i][i] = newcost;
  }

}

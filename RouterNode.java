import javax.swing.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private int[] costs = new int[RouterSimulator.NUM_NODES];

  private int[][] dv = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

    for(int i = 0; i < costs.length; i++){
      if (costs[i] < 999 && costs[i] > 0){
        sendUpdate(new RouterPacket(myID, i, costs));
      }
    }

  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
    for (int i = 0; i < pkt.mincost.length; i++){
      dv[i][pkt.sourceid] = pkt.mincost[i] + costs[pkt.sourceid];
      if (pkt.mincost[i]+costs[pkt.sourceid] < costs[i])
          updateLinkCost(i,pkt.mincost[i]+costs[pkt.sourceid]);
    }
  }


  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);
  }


  //--------------------------------------------------
  public void printDistanceTable() {

    String line = "";

    myGUI.println("\nMincost table:");
    line = "from   |";
    for (int i = 0; i < costs.length; i++)
      line += "\t"+i;

    myGUI.println(line);

    for (int i = 0; i < line.length(); i++){
      myGUI.print("-");
    }

    line = "\nThis router:";
    for (int i = 0; i < costs.length; i++)
      line += "\t"+costs[i];

    myGUI.println(line);
  }

  private void printFullTable(){ //TODO: Doesn't work as it should
    String line = "";
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());

    myGUI.println("Distancetable:");
    line = "dst   |";
    for (int i = 0; i < costs.length; i++)
      line += "\t"+i;

    myGUI.println(line);

    for (int i = 0; i < costs.length; i++){
      line = "nbr "+i+" |";
      for (int k = 0; k < costs.length; k++){
        line += "\t"+dv[i][k];
      }
      myGUI.println(line);
    }
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    myGUI.println("New cost to get to "+dest+": "+newcost+" (old cost:"+costs[dest]+")");
    costs[dest] = newcost;
  }

}

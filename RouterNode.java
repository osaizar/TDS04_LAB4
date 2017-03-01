import javax.swing.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private int[] costs = new int[RouterSimulator.NUM_NODES];

  //  dv[via][to]
  private int[][] dv = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
  private int[] routes = new int[RouterSimulator.NUM_NODES]; // best route

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

    for (int i = 0; i < costs.length; i++){
    	dv[i][i] = costs[i];
    }

    for(int i = 0; i < costs.length; i++){
		  if (i != myID){
			  sendUpdate(new RouterPacket(myID, i, costs));
		}
	  }
  }

  public void broadcastCosts(){
	 for(int i = 0; i < costs.length; i++){
		  if (i != myID){
			  sendUpdate(new RouterPacket(myID, i, costs));
		}
	  }
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
    for (int i = 0; i < pkt.mincost.length; i++){
      if (i != pkt.sourceid){
        //the cost to get to i from pkt.src is the cost to get to pkt.src + the cost to get to i from pkt.src
        dv[pkt.sourceid][i] = pkt.mincost[i]+dv[pkt.sourceid][pkt.sourceid];
        if (dv[pkt.sourceid][i] > RouterSimulator.INFINITY) {
          dv[pkt.sourceid][i] = RouterSimulator.INFINITY;
        }
      }
    }
    updateRoutes();
  }

  public void updateRoutes(){
	  for(int to = 0; to < routes.length; to++){
		  if (to != myID){
			  int cost = 999;
			  for(int via = 0; via < routes.length; via++){
				  if (via != myID){
					  if (dv[via][to] <= cost && dv[via][to] > 0)cost = dv[via][to];
				  }
			  }
			  routes[to] = cost;
		  }
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
      myGUI.print("-------");
    }

    myGUI.print("\n");

    for (int i = 0; i < costs.length; i++){
      line = "to "+i+" |";
      for (int k = 0; k < costs.length; k++){
        if(k != myID && i != myID) {
          if (dv[k][i] == 0)line += "\t?";
          else line += "\t"+dv[k][i];
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

    line = "\ncost:";
    for (int i = 0; i < costs.length; i++)
      line += "\t"+costs[i];

    myGUI.println(line);

      line = "routes:";
      for (int i = 0; i < routes.length; i++)
        line += "\t"+routes[i];

    myGUI.println(line);
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    myGUI.println("New cost to get to "+dest+": "+newcost+" (old cost:"+dv[dest][dest]+")");
    int oldcost = dv[dest][dest];
    for (int to = 0; to < RouterSimulator.NUM_NODES; to++){
    	if (to != myID && dv[dest][to] < RouterSimulator.INFINITY){
        	dv[dest][to] -= oldcost;
        	dv[dest][to] += newcost;
    	}
    }
    dv[dest][dest] = newcost;
    costs[dest] = newcost;
    updateRoutes();
    broadcastCosts();
  }

}

import javax.swing.*;        

public class RouterNode {
	private int myID;
	private GuiTextArea myGUI;
	private RouterSimulator sim;
	private int[] costs = new int[RouterSimulator.NUM_NODES];
	  
	private int[] routes = new int[RouterSimulator.NUM_NODES];
	private int[][] distanceTable = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
	private boolean poisonedReverse = true; 
	  
	
	//--------------------------------------------------
	public RouterNode(int ID, RouterSimulator sim, int[] costs) {
		myID = ID;
	    this.sim = sim;
	    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
	
	    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
	
	    for(int i = 0; i < RouterSimulator.NUM_NODES; i++) 
		{
	        routes[i] = i; 
	        for(int j = 0; j < RouterSimulator.NUM_NODES; j++) 
			{
	        	if(i == myID)
				{
	        		distanceTable[i][j] = costs[j];
				} else 
				{
	        		distanceTable[i][j] = RouterSimulator.INFINITY;
				}
	        }
	    }
	
	    vector();
	}
	  
	private void vector()
	{
	    for(int nbNode = 0; nbNode < RouterSimulator.NUM_NODES; nbNode++) 
		{
	        if(nbNode != myID) 
			{
	            if(costs[nbNode] != RouterSimulator.INFINITY) 
				{
	                int[] c = new int[RouterSimulator.NUM_NODES];
	                System.arraycopy(costs, 0, c, 0, RouterSimulator.NUM_NODES);
	                if(poisonedReverse) 
					{
	                	for(int j = 0; j < RouterSimulator.NUM_NODES; j++) 
						{
	                		if(routes[j] == nbNode) 
							{
	                			c[j] = RouterSimulator.INFINITY;
	                		}
	                	}
	                }
	                RouterPacket pkt = new RouterPacket(myID, nbNode, c);
	                sendUpdate(pkt);
	            }
	        }
	    }
	}
	  
	private boolean recomputeCost() 
	{
	    int newCost = 0;  
	    boolean ret = false;

	    for(int node = 0; node < RouterSimulator.NUM_NODES; node++) 
		{
	        for(int dest = 0; dest < RouterSimulator.NUM_NODES; dest++) 
			{
	            newCost = distanceTable[node][dest];
	            if(node != myID)
				{
	            	newCost += distanceTable[myID][node];
				}
	
	            if(newCost < costs[dest]) 
				{
	            	costs[dest] = newCost; 
	                routes[dest] = node;
	                ret = true;
	            }
	        }
	    }
	    return ret; 
	}
	
	//--------------------------------------------------
	public void recvUpdate(RouterPacket pkt) {
		System.arraycopy(pkt.mincost, 0, distanceTable[pkt.sourceid], 0, RouterSimulator.NUM_NODES);
	
	    if(recomputeCost()) {
	    	vector();
	    }
	}
	  
	
	//--------------------------------------------------
	private void sendUpdate(RouterPacket pkt) {
		sim.toLayer2(pkt);
	}
	  
	
	//--------------------------------------------------
	public void printDistanceTable() {
		myGUI.println("Current table for "+ myID +"  at time " + sim.getClocktime());		
		
		myGUI.println("Distancetable:");
		myGUI.print(String.format( "%8s %2s", "Dst", "|"));
		for(int nbNode = 0; nbNode < RouterSimulator.NUM_NODES; nbNode++)
		{
			myGUI.print( String.format( "%8s", nbNode ) );
		}

		myGUI.println("\n--------------------------------------------------------");
	
		for(int col = 0; col < RouterSimulator.NUM_NODES; col++)
		{
			if(costs[col] != RouterSimulator.INFINITY &&  col != myID)
			{
				myGUI.print( String.format( "%2s %2s %3s", "nbr" , col, "|" ) );
				for(int row = 0; row < RouterSimulator.NUM_NODES; row++)
				{
					myGUI.print( String.format( "%8s", this.distanceTable[col][row] ) );
				}
				myGUI.println();
			}
		}
		myGUI.println();	
		

		myGUI.println("Our distance vector and routes:");
		myGUI.println();

		myGUI.print(String.format( "%8s %2s", "Dst", "|"));
		for(int nbNode = 0; nbNode < RouterSimulator.NUM_NODES; nbNode++)
		{
			myGUI.print( String.format( "%8s", nbNode ) );
		}
		
		myGUI.println("\n--------------------------------------------------------");
	
		myGUI.print( String.format( "%8s %2s", "Cost", "|" ) );
		for(int row = 0; row < RouterSimulator.NUM_NODES; row++)
		{
			myGUI.print( String.format( "%8s", this.distanceTable[myID][row] ) );
		}
		myGUI.println();
		myGUI.print( String.format( "%8s %2s", "Short", "|" ) );
		for(int row = 0; row < RouterSimulator.NUM_NODES; row++)
		{
			myGUI.print( String.format( "%8s", this.costs[row] ) );
		}
		myGUI.println();
		myGUI.print( String.format( "%7s %2s", "Route", "|" ) );
		for(int row = 0; row < RouterSimulator.NUM_NODES; row++)
		{
			myGUI.print( String.format( "%8s", this.routes[row] ) );
		}
		myGUI.println();
		myGUI.println();
	}
	
	  //--------------------------------------------------
	public void updateLinkCost(int dest, int newcost) {
		distanceTable[myID][dest] = newcost;
		  
	    if(recomputeCost()) 
		{
			vector();
	    }
	  }
}

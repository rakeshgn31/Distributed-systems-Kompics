/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Hosts;

import ClientNode.ClientParent;
import DKVNetwork.DKVAddress;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.java.JavaTimer;

/**
 *
 * @author aruna
 */
public class ClientHost extends ComponentDefinition {
    
     public ClientHost(Init init)
    {
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component timer = create(JavaTimer.class, Init.NONE);
        Component nodeParent = create(ClientParent.class, new ClientParent.Init(init.nClientID, init.self,
                                            init.trackeraddr, init.bPutGetSimulation, init.bCASSimulation));
                
        connect(nodeParent.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
        connect(nodeParent.getNegative(se.sics.kompics.timer.Timer.class), timer.getPositive(se.sics.kompics.timer.Timer.class), Channel.TWO_WAY);        
    }
     
    public static class Init extends se.sics.kompics.Init<TrackerHost> {

        public final int nClientID;
        public final DKVAddress self;
        public final DKVAddress trackeraddr;
        public final boolean bPutGetSimulation, bCASSimulation;
        
        public Init(DKVAddress self, DKVAddress trackeraddr, boolean b, boolean b1) {
            
            this.nClientID = 10;
            this.self = self;
            this.trackeraddr = trackeraddr;
            this.bPutGetSimulation = b;
            this.bCASSimulation = b1;
        }
    }
}

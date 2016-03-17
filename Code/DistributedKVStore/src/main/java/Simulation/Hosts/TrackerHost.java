/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Hosts;

import DKVNetwork.DKVAddress;
import TrackerNode.TrackerParent;
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
public class TrackerHost extends ComponentDefinition {
    
    public TrackerHost(Init init)
    {
         //DKVAddress self = config().getValue("dkvstore.", DKVAddress.class);
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component timer = create(JavaTimer.class, Init.NONE);
        Component TrackerParent = create(TrackerParent.class, new TrackerParent.Init(init.self, 
                                                                init.nNodeCount, init.nFDTimeout));
        
        connect(TrackerParent.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
        connect(TrackerParent.getNegative(se.sics.kompics.timer.Timer.class), timer.getPositive(se.sics.kompics.timer.Timer.class), Channel.TWO_WAY);  
    }
     public static class Init extends se.sics.kompics.Init<TrackerHost> {

        public final DKVAddress self;
        public final int nNodeCount;
        public final int nFDTimeout;

        public Init(DKVAddress self,int nodeCount, int FDTimeout) {
            
            this.self = self;            
            this.nNodeCount = nodeCount;
            this.nFDTimeout = FDTimeout;            
        }
    }
}

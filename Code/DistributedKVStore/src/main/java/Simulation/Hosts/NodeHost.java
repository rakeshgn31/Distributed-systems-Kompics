/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Hosts;

import DKVNetwork.DKVAddress;
import DataStoreNode.NodeParent;
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
public class NodeHost extends ComponentDefinition {
    
    public NodeHost(Init init)
    {
        //DKVAddress self = config().getValue("dkvstore.self", DKVAddress.class);
        //DKVAddress trackeraddr = config().getValue("dkvstore.node.trackeraddr", DKVAddress.class);
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component timer = create(JavaTimer.class, Init.NONE);
        Component nodeParent = create(NodeParent.class, new NodeParent.Init(init.self, init.trackeraddr));
                
        connect(nodeParent.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
        connect(nodeParent.getNegative(se.sics.kompics.timer.Timer.class), timer.getPositive(se.sics.kompics.timer.Timer.class), Channel.TWO_WAY);        
    }
    
    public static class Init extends se.sics.kompics.Init<NodeHost> {

        public final DKVAddress self;
        public final DKVAddress trackeraddr;
        
        public Init(DKVAddress self, DKVAddress trackeraddr) {
            
            this.self = self;
            this.trackeraddr = trackeraddr;
        }
    }
}

/*
 * Welcome to NetBeans...!!!
 */
package DataStoreNode;

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
 * @author admin
 */
public class NodeParent extends ComponentDefinition {
 
    public NodeParent(Init init) {
        
        Component timer = create(JavaTimer.class, Init.NONE);
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component node = create(Node.class, new Node.Init(init.self, init.tracker));
        
        connect(node.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<NodeParent> {

        public final DKVAddress self;
        public final DKVAddress tracker;

        public Init(DKVAddress self, DKVAddress tracker) {
            
            this.self = self;
            this.tracker = tracker;
        }
    }
}
/*
 * Welcome to NetBeans...!!!
 */
package TrackerNode;

import DKVNetwork.DKVAddress;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

/**
 *
 * @author admin
 */
public class TrackerParent extends ComponentDefinition {
    
    public TrackerParent(Init init) {    

        Component network = create(NettyNetwork.class, new NettyInit(init.selfAddress));
        Component timer = create(JavaTimer.class, se.sics.kompics.Init.NONE);        
        Component tracker = create(Tracker.class, new Tracker.Init(init.selfAddress, init.nNumOfNodes, init.nFDTimeout));

        connect(tracker.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);
        connect(tracker.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<TrackerParent> {

        public final DKVAddress selfAddress;
        public final int nNumOfNodes;
        public final int nFDTimeout;

        public Init(DKVAddress self, int numNodes, int fdTimeout) {

            this.selfAddress = self;
            this.nNumOfNodes = numNodes;
            this.nFDTimeout = fdTimeout;
        }
    }
}
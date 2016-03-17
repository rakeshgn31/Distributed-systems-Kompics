/*
 * Welcome to NetBeans...!!!
 */
package ClientNode;

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
 * @author admin
 */
public class ClientParent extends ComponentDefinition {    
      
    public ClientParent(Init init) {
        
        Component timer = create(JavaTimer.class, NodeParent.Init.NONE);
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component client = create(Client.class, new Client.Init(init.nClientID, init.self,
                                        init.tracker, init.bPutGetRequest, init.bCASOpRequest));
        
        connect(client.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
    }
    
    public static class Init extends se.sics.kompics.Init<ClientParent> {

        public final int nClientID;
        public final DKVAddress self;
        public final DKVAddress tracker;
        
        public final boolean bPutGetRequest;
        public final boolean bCASOpRequest;

        public Init(int nClientID, DKVAddress self, DKVAddress tracker, boolean bPGReq, boolean bCASReq) {

            this.nClientID = nClientID;
            this.self = self;
            this.tracker = tracker;
            
            this.bPutGetRequest = bPGReq;
            this.bCASOpRequest = bCASReq;
        }
    }
}

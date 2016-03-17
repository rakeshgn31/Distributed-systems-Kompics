/*
 * Welcome to NetBeans...!!!
 */
package Operations;

import se.sics.kompics.KompicsEvent;

/**
 *
 * @author admin
 */
public class HeartbeatResponse implements KompicsEvent {
    
    public final int NodeID;
    
    public HeartbeatResponse(int nNodeID) {
        
        this.NodeID = nNodeID;
    }
}

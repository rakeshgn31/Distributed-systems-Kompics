/*
 * Welcome to NetBeans...!!!
 */
package Operations;

import se.sics.kompics.KompicsEvent;

/**
 *
 * @author admin
 */
public class JoinApprove implements KompicsEvent {
    
    public final short nNodeID;
    
    public JoinApprove(short nodeID) {
        this.nNodeID = nodeID;
    }
}

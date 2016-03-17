/*
 * Welcome to NetBeans...!!!
 */
package Operations;

import se.sics.kompics.KompicsEvent;

/**
 *
 * @author admin
 */
public class GetRequest implements KompicsEvent {
    
    // Variable that stores the request ID
    public final int RequestID;
    
    // Variable that stores the key name
    public final String Key;
    
    public GetRequest(int nReqID, String strKey) {

        this.RequestID = nReqID;
        this.Key = strKey;
    }
}
/*
 * Welcome to NetBeans...!!!
 */
package Operations;

import java.util.ArrayList;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author admin
 */
public class PutRequest implements KompicsEvent {

    // Variable that stores the request ID
    public final int RequestID;
    
    public final String Key;
    public final ArrayList<String> listValues;
    
    public PutRequest(int nReqID, String strKey, ArrayList<String> listValues) {
        
        this.RequestID = nReqID;
        this.Key = strKey;
        this.listValues = listValues;
    }
}

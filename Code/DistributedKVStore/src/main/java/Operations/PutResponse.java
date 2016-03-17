/*
 * Welcome to NetBeans...!!!
 */
package Operations;

import DKVUtilities.OperationsHelper;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author admin
 */
public class PutResponse implements KompicsEvent {

    // Variable that stores the request ID
    public final int RequestID;
    
    public final String Key;
    public final OperationsHelper.OperationExitCode opExitCode;
    
    public PutResponse(int nReqID, String strKey, OperationsHelper.OperationExitCode opCode) {
        
        this.RequestID = nReqID;
        this.Key = strKey;
        this.opExitCode = opCode;
    }
}

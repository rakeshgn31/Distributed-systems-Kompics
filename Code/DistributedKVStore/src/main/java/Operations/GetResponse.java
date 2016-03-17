/*
 * Welcome to NetBeans...!!!
 */
package Operations;

import DKVUtilities.OperationsHelper;
import java.util.ArrayList;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author admin
 */
public class GetResponse implements KompicsEvent {
    
    // Variable that stores the request ID
    public final int RequestID;
    
    public final String Key;
    public final ArrayList<String> listValues;
    public final OperationsHelper.OperationExitCode opExitCode;
    
    public GetResponse(int nReqID, String strKey, ArrayList<String> listValues,
                                    OperationsHelper.OperationExitCode opExitCode) {
        
        this.RequestID = nReqID;
        this.Key = strKey;
        this.listValues = listValues;
        this.opExitCode = opExitCode;
    }
}

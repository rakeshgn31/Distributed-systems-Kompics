/*
 * Welcome to NetBeans...!!!
 */
package DKVSerializers;

import DKVUtilities.OperationsHelper;
import Operations.GetRequest;
import Operations.GetResponse;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import se.sics.kompics.network.netty.serialization.Serializer;

/**
 *
 * @author admin
 */
public class GETSerializer implements Serializer {

    private static final byte GET_REQUEST = 1;
    private static final byte GET_RESPONSE = 2;
    
    @Override
    public int identifier() {
        
        return 55;        
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        
        if (o instanceof GetRequest) {
            
            GetRequest req = (GetRequest) o;
            buf.writeByte(GET_REQUEST);                     // 1 byte of Identifier
            buf.writeInt(req.RequestID);
            OperationsHelper.serializeString(req.Key, buf); // Length of key followed by key
            
        } else if (o instanceof GetResponse) {

            GetResponse resp = (GetResponse) o;
            buf.writeByte(GET_RESPONSE);                                // 1 byte of Identifier
            buf.writeInt(resp.RequestID);
            OperationsHelper.serializeString(resp.Key, buf);            // Length of Key followed by key
            buf.writeByte(resp.opExitCode.code);                        // 1 byte of operation return value
            OperationsHelper.serializeStringList(resp.listValues, buf); // list of values, if any
        }
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> optnl) {
        
        byte type = buf.readByte(); // Read the identifier
        switch (type) {
            
            case GET_REQUEST:
                int reqID = buf.readInt();                  // Read the Request ID first
                int keyLength = buf.readUnsignedShort();    // Read Key length
                byte[] keyBytes = new byte[keyLength];      
                buf.readBytes(keyBytes);                    // Read Key actual value
                return new GetRequest(reqID, new String(keyBytes));
                
            case GET_RESPONSE:
                int nreqID = buf.readInt();             // Read the Request ID first
                int keylen = buf.readUnsignedShort();   // Read Key length
                byte[] keybytes = new byte[keylen];      
                buf.readBytes(keybytes);                // Read Key actual value
                
                // Read the operation return code
                int nCode = buf.readByte();
                OperationsHelper.OperationExitCode opCode = OperationsHelper.OperationExitCode.values()[nCode];
                
                // Read the values only if the operation was successful
                ArrayList<String> listValues = null;
                if(opCode == OperationsHelper.OperationExitCode.OP_SUCCESS) {
                
                    listValues = OperationsHelper.deSerializeStringList(buf);
                }
                
                return new GetResponse(nreqID, new String(keybytes), listValues, opCode);
                
            default:
                return null;
        } 
    }
}

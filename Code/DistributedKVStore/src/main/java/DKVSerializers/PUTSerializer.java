/*
 * Welcome to NetBeans...!!!
 */
package DKVSerializers;

import DKVUtilities.OperationsHelper;
import Operations.PutRequest;
import Operations.PutResponse;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import se.sics.kompics.network.netty.serialization.Serializer;

/**
 *
 * @author admin
 */
public class PUTSerializer implements Serializer {

    private static final byte PUT_REQUEST = 1;
    private static final byte PUT_RESPONSE = 2;
    
    @Override
    public int identifier() {
        
        return 54;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        
        if (o instanceof PutRequest) {
            
            PutRequest req = (PutRequest) o;
            buf.writeByte(PUT_REQUEST);                     // 1 byte of Identifier
            buf.writeInt(req.RequestID);
            OperationsHelper.serializeString(req.Key, buf); // Length of key followed by key
            OperationsHelper.serializeStringList(req.listValues, buf);  // List size, length of 
                                                                        // each string, each string            
        } else if (o instanceof PutResponse) {

            PutResponse resp = (PutResponse) o;
            buf.writeByte(PUT_RESPONSE);                                // 1 byte of Identifier
            buf.writeInt(resp.RequestID);
            OperationsHelper.serializeString(resp.Key, buf);            // Length of Key followed by key
            buf.writeByte(resp.opExitCode.code);                        // 1 byte of operation return value
        }        
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> optnl) {
        
        byte type = buf.readByte(); // Read the identifier
        switch (type) {
            
            case PUT_REQUEST:
                int nReqID = buf.readInt();
                int keyLength = buf.readUnsignedShort();    // Read Key length
                byte[] keyBytes = new byte[keyLength];      
                buf.readBytes(keyBytes);                    // Read Key actual value
                ArrayList<String> listValues = OperationsHelper.deSerializeStringList(buf);
                return new PutRequest(nReqID, new String(keyBytes), listValues);
                
            case PUT_RESPONSE:
                int respID = buf.readInt();
                // Read the key first
                int keylen = buf.readUnsignedShort();    // Read Key length
                byte[] keybytes = new byte[keylen];      
                buf.readBytes(keybytes);                 // Read Key actual value
                
                // Read the operation return code
                int nCode = buf.readByte();
                OperationsHelper.OperationExitCode opCode = OperationsHelper.OperationExitCode.values()[nCode];
                
                return new PutResponse(respID, new String(keybytes), opCode);
                
            default:
                return null;
        }
    }
}

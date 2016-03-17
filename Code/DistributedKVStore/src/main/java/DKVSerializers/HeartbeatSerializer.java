package DKVSerializers;

import Operations.HeartbeatRequest;
import Operations.HeartbeatResponse;

import io.netty.buffer.ByteBuf;
import com.google.common.base.Optional;
import se.sics.kompics.network.netty.serialization.Serializer;

public class HeartbeatSerializer implements Serializer {

    private static final byte HB_REQ = 1;
    private static final byte HB_REPLY = 2;

    @Override
    public int identifier() {
        
        return 52;
    }

    // Writes a total of 1 bytes indicating the type of heartbeat
    @Override
    public void toBinary(Object o, ByteBuf buf) {
        
        if (o instanceof HeartbeatRequest) {
            
            buf.writeByte(HB_REQ);
            
        } else if (o instanceof HeartbeatResponse) {

            HeartbeatResponse hbResp = (HeartbeatResponse) o;
            buf.writeByte(HB_REPLY);
            buf.writeInt(hbResp.NodeID);
        }
    }

    // Reads 1 byte that indicates the type of heartbeat
    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        
        byte type = buf.readByte();
        switch (type) {
            
            case HB_REQ:
                return new HeartbeatRequest();
                
            case HB_REPLY:
                int nodeID = buf.readInt();
                return new HeartbeatResponse(nodeID);
                
            default:
                return null;
        }        
    }
}

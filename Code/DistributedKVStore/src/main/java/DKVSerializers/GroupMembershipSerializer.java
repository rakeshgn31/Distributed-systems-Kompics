/*
 * Welcome to NetBeans...!!!
 */
package DKVSerializers;

import Operations.JoinApprove;
import Operations.JoinRequest;

import io.netty.buffer.ByteBuf;
import com.google.common.base.Optional;
import se.sics.kompics.network.netty.serialization.Serializer;

/**
 *
 * @author admin
 */
public class GroupMembershipSerializer implements Serializer {
    
    private static final byte JOIN_REQ = 1;
    private static final byte JOIN_APPRV = 2;

    @Override
    public int identifier() {
        
        return 53;
    }

    // Writes a total of 1 bytes indicating the type of heartbeat
    @Override
    public void toBinary(Object o, ByteBuf buf) {
        
        if (o instanceof JoinRequest) {
            
            buf.writeByte(JOIN_REQ);
            
        } else if (o instanceof JoinApprove) {

            JoinApprove approval = (JoinApprove) o;
            buf.writeByte(JOIN_APPRV);
            buf.writeShort(approval.nNodeID);
        }
    }

    // Reads 1 byte that indicates the type of heartbeat
    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        
        byte type = buf.readByte();
        switch (type) {
            
            case JOIN_REQ:
                return new JoinRequest();
                
            case JOIN_APPRV:
                return new JoinApprove((short) buf.readUnsignedShort());
                
            default:
                return null;
        }        
    }
}

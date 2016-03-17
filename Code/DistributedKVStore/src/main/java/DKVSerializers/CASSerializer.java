/*
 * Welcome to NetBeans...!!!
 */
package DKVSerializers;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import se.sics.kompics.network.netty.serialization.Serializer;

/**
 *
 * @author admin
 */
public class CASSerializer implements Serializer {

    private static final byte CAS_REQUEST = 1;
    private static final byte CAS_RESPONSE = 2;
    
    @Override
    public int identifier() {
        
        return 56;
    }

    @Override
    public void toBinary(Object o, ByteBuf bb) {
        
    }

    @Override
    public Object fromBinary(ByteBuf bb, Optional<Object> optnl) {
       return null; 
    } 
}

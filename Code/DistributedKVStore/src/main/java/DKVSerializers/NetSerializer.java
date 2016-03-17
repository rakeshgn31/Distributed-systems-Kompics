package DKVSerializers;

import DKVNetwork.DKVAddress;
import DKVNetwork.DKVMessage;
import DKVNetwork.DKVPacketHeader;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.net.InetAddress;
import java.net.UnknownHostException;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Transport;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;

public class NetSerializer implements Serializer {

    private static final byte ADDR = 1;
    private static final byte HEADER = 2;
    private static final byte MSG = 3;

    @Override
    public int identifier() {
        
        return 51;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        
        if (o instanceof DKVAddress) {
            
            DKVAddress addr = (DKVAddress) o;
            buf.writeByte(ADDR);        // mark which type we are serialising (1 byte)
            addressToBinary(addr, buf); // 6 bytes
            // total 7 bytes
        } else if (o instanceof DKVPacketHeader) {
            
            DKVPacketHeader header = (DKVPacketHeader) o;
            buf.writeByte(HEADER);          // mark which type we are serialising (1 byte)
            headerToBinary(header, buf);    // 13 bytes
            // total 14 bytess
        } else if (o instanceof DKVMessage) {
            
            DKVMessage msg = (DKVMessage) o;
            buf.writeByte(MSG);                             // mark which type we are serialising (1 byte)
            headerToBinary(msg.getHeader(), buf);           // 13 bytes
            Serializers.toBinary(msg.extractValue(), buf);  // no idea what it is, let the framework deal with it
        }
    }
   
    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        
        byte type = buf.readByte(); // Read the first byte to figure out the type
        switch (type) {
            
            case ADDR:
                return addressFromBinary(buf);
                
            case HEADER:
                return headerFromBinary(buf);
                
            case MSG: {
                DKVPacketHeader header = headerFromBinary(buf); // 13 bytes
                KompicsEvent payload = (KompicsEvent) Serializers.fromBinary(buf, Optional.absent()); // don't know what it is but KompicsEvent is the upper bound
                return new DKVMessage(header, payload);                            
            }
            
            default:
                return null;
        }
    }

    private void headerToBinary(DKVPacketHeader header, ByteBuf buf) {
        
        addressToBinary(header.srcAddress, buf);        // 6 bytes
        addressToBinary(header.destAddress, buf);       // 6 bytes
        buf.writeByte(header.transProtocol.ordinal());  // 1 byte is enough
        // total of 13 bytes
    }

    private DKVPacketHeader headerFromBinary(ByteBuf buf) {
        
        DKVAddress src = addressFromBinary(buf);        // 6 bytes
        DKVAddress dst = addressFromBinary(buf);        // 6 bytes
        int protoOrd = buf.readByte();                  // 1 byte
        Transport proto = Transport.values()[protoOrd];
        return new DKVPacketHeader(src, dst, proto);    // total of 13 bytes, check
    }

    private void addressToBinary(DKVAddress addr, ByteBuf buf) {
        
        buf.writeBytes(addr.getIp().getAddress());  // 4 bytes IP (Assuming IPv4)
        buf.writeShort(addr.getPort());             // we only need 2 bytes here
        // total of 6 bytes
    }

    private DKVAddress addressFromBinary(ByteBuf buf) {
        
        byte[] ipBytes = new byte[4];
        buf.readBytes(ipBytes);     // 4 bytes
        
        try {
            InetAddress ip = InetAddress.getByAddress(ipBytes);
            int port = buf.readUnsignedShort(); // 2 bytes
            return new DKVAddress(ip, port);    // total of 6, check
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);     // let Netty deal with this
        }
    }
}
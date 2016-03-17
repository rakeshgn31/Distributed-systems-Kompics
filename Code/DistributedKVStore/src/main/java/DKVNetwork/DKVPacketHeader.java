/*
 * Welcome to NetBeans...!!!
 */
package DKVNetwork;

import se.sics.kompics.network.Header;
import se.sics.kompics.network.Transport;

/**
 *
 * @author admin
 */
public class DKVPacketHeader implements Header<DKVAddress> {
   
    public final DKVAddress srcAddress;
    public final DKVAddress destAddress;
    public final Transport transProtocol;

    public DKVPacketHeader(DKVAddress src, DKVAddress dest, Transport tpProtocol) {
        
        this.srcAddress = src;
        this.destAddress = dest;
        this.transProtocol = tpProtocol;
    }

    @Override
    public DKVAddress getSource() {
        return srcAddress;
    }

    @Override
    public DKVAddress getDestination() {
        return destAddress;
    }

    @Override
    public Transport getProtocol() {
        return transProtocol;
    }
}
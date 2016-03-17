/*
 * Welcome to NetBeans...!!!
 */
package DKVNetwork;

import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Transport;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;

/**
 *
 * @author admin
 */
public class DKVMessage implements Msg<DKVAddress, DKVPacketHeader>, 
                                            PatternExtractor<Class, KompicsEvent> {
    
    private final DKVPacketHeader dkvPackHeader;
    private final KompicsEvent dkvPackPayload;
    
    public DKVMessage(DKVAddress src, DKVAddress dest, Transport tpProtocol, KompicsEvent pktPayload) {
        
        this.dkvPackHeader = new DKVPacketHeader(src, dest, tpProtocol);
        this.dkvPackPayload = pktPayload;
    }
    
    public DKVMessage(DKVPacketHeader pktHeader, KompicsEvent pktPayload) {
        
        this.dkvPackHeader = pktHeader;
        this.dkvPackPayload = pktPayload;
    }
    
    @Override
    public DKVPacketHeader getHeader() {
        
        return this.dkvPackHeader;
    }

    @Override
    public DKVAddress getSource() {
        
        return this.dkvPackHeader.srcAddress;
    }

    @Override
    public DKVAddress getDestination() {
        
        return this.dkvPackHeader.destAddress;
    }

    @Override
    public Transport getProtocol() {
        
        return this.dkvPackHeader.transProtocol;
    }
    
    @Override
    public Class extractPattern() {
        
        return (Class) dkvPackPayload.getClass();
    }

    @Override
    public KompicsEvent extractValue() {
        
        return dkvPackPayload;
    }
}
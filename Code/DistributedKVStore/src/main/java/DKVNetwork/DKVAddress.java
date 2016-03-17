/*
 * Welcome to NetBeans...!!!
 */
package DKVNetwork;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import se.sics.kompics.network.Address;

/**
 *
 * @author admin
 */
public class DKVAddress implements Address {
   
    private final InetSocketAddress isa;
    
    public DKVAddress(InetAddress addr, int port) {
        this.isa = new InetSocketAddress(addr, port);
    }

    @Override
    public InetAddress getIp() {
        return this.isa.getAddress();
    }
    
    @Override
    public int getPort() {
        return this.isa.getPort();
    }

    @Override
    public InetSocketAddress asSocket() {
        return this.isa;
    }

    @Override
    public boolean sameHostAs(Address other) {
        return this.isa.equals(other.asSocket());
    }

    @Override
    public final String toString() {
        return isa.toString();
    }

    @Override
    public int hashCode() {
        
        int hash = 9;
        hash = 11 * hash + (this.isa != null ? this.isa.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final DKVAddress paramObj = (DKVAddress) obj;
        if (this.isa != paramObj.isa && (this.isa == null || !this.isa.equals(paramObj.isa))) {
            return false;
        }
        
        return true;
    }
}
package DKVNetwork;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.config.Converter;

public class DKVAddressConverter implements Converter<DKVAddress> {

    @Override
    public DKVAddress convert(Object o) {
        
        if (o instanceof Map) {
            try {
                Map m = (Map) o;
                String hostname = Conversions.convert(m.get("host"), String.class);
                int port = Conversions.convert(m.get("port"), Integer.class);
                InetAddress ip = InetAddress.getByName(hostname);
                return new DKVAddress(ip, port);
                
            } catch (UnknownHostException ex) { }
        }
        
        if (o instanceof String) {
            try {
                String[] ipport = ((String) o).split(":");
                InetAddress ip = InetAddress.getByName(ipport[0]);
                int port = Integer.parseInt(ipport[1]);
                return new DKVAddress(ip, port);
                
            } catch (UnknownHostException ex) {}
        }
        
        return null;
    }

    @Override
    public Class<DKVAddress> type() {
        
        return DKVAddress.class;
    }
}

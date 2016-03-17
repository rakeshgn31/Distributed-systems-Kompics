/*
 * Welcome to NetBeans...!!!
 */
package DKVUtilities;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.netty.buffer.ByteBuf;
import static java.lang.Math.abs;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author admin
 */
public class OperationsHelper {
    
    private static final int HASH_CODE_LIMITER = 10000;
    
    public enum OperationExitCode {
 
        OP_SUCCESS(0),
        OP_FAILURE(1),
        OP_ABORTED(2),
        OP_ERROR(3),
        OP_EXCEPTION(4),
        OP_REF_OLD_VALUE_MISMATCH(5);

        public final byte code;

        // Enum constructor cannot be public
        private OperationExitCode(int code) {

            this.code = (byte) code;
        }            
    }

    public static int getNodeIDForKeyStorage(String key, int nNumOfNodes) {
        
        int nNodeID = -1;
        
        try {
            // Use key string to get the hash value         
            char[] arrChars = key.toCharArray();
            int nValue = 0;
            for(char c : arrChars) {
                nValue += abs((int)c);
            }
            
            HashFunction hf = Hashing.murmur3_128();
            HashCode hc = hf.newHasher().putInt(nValue).hash();
            int nHash = hc.asInt() % HASH_CODE_LIMITER;        
            int nTemp = ( HASH_CODE_LIMITER / nNumOfNodes );
            for(int n = 0; n < nNumOfNodes; n++) {
                if( (n*nTemp) <= nHash && nHash < ((n+1)*nTemp) ) {
                    nNodeID = n+1;
                    break;
                }
            }
            
            // To handle the remainder
            if(nNodeID == -1) {
                nNodeID = nNumOfNodes;
            }
            
            return nNodeID;
        }
        catch (Exception ex) {
            return 1;
        }
    }
    
    public static void serializeString(String str, ByteBuf buf) {
        
        // Write the length of the string and then the actual data
        // We shall assume that the length of any string in our program 
        // shall not exceed the short datatype range
        buf.writeShort(str.length());   // 2 bytes
        buf.writeBytes(str.getBytes()); // Then the actual string
    }
    
    public static void serializeStringList(ArrayList<String> strList, ByteBuf buf) {
     
        // First write the size of the list
        buf.writeByte(strList.size());     // 1 byte - won't exceed 255
        // Write each string into a buffer
        strList.stream().forEach((str) -> { // Then the actual strings
            
            serializeString(str, buf);
        });
    }
    
    public static ArrayList<String> deSerializeStringList(ByteBuf buf) {
        
        ArrayList<String> listOfValues = new ArrayList<>();
        int nSize = buf.readByte();
        for(int nIndex = 0; nIndex < nSize; nIndex++) {
            
            int strlen = buf.readUnsignedShort();
            byte[] str = new byte[strlen];
            buf.readBytes(str);
            listOfValues.add(new String(str));
        }
        
        return listOfValues;
    }
}
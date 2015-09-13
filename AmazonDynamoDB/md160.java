import java.util.*;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.util.encoders.Hex;
import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;

public class md160 {
    public static void main(String args[]) {
        String resultingHash = "";

        String salt = "free";
        String ssn = "123456789";

        String saltssn = salt + ssn;

        try {
            byte[] r = saltssn.getBytes("US-ASCII");
            RIPEMD160Digest d = new RIPEMD160Digest();
            d.update (r, 0, r.length);
            byte[] o = new byte[d.getDigestSize()];
            d.doFinal (o, 0);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(40);
            Hex.encode (o, baos);
            resultingHash = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            System.out.println(resultingHash);
        } catch(UnsupportedEncodingException e) {
            System.out.println(e);
        } catch(IOException i) {
            System.out.println(i);
        } 
    }
}
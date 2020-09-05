package model.network;

import java.security.SecureRandom;
import java.util.Base64;

public class Token {

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
    private final String token;

    public Token(){
        token = generateNewToken();
    }

    public String getToken(){
        return token;
    }


    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

}

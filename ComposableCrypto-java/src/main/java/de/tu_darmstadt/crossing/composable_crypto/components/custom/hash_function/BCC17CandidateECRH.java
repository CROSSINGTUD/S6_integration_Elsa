package de.tu_darmstadt.crossing.composable_crypto.components.custom.hash_function;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Deque;

public class BCC17CandidateECRH implements HashFunction {
    private static final int bytelength = 384;
    private static final BigInteger two = BigInteger.valueOf(2);
    private static final BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF", 16);
    private static final BigInteger alpha = new BigInteger("e6c7cb290db49f367e3bc76463a963b151cfa9be20d954a1a20e5df57d4910f455655090b059efd46af1560037d74391b74a96c6f355405427ffb111017acceee436eb6dbc1af1f1c2580b015cf9c34f55b588a4b4247d72be1ae1aac18fa2300ad3b725b4dcc5dc6710d8e110951fad55e2ffdd19e034f87777b868a11364a6d8ffe0fd317fac9fdbb4d4feaf6f61061e2f1fbecba8a0ffd6d5f401432887b8b85b6e0f5abf7b7d20342af2cab2bc862c05608868a75ce0058cfc18d4a0b59", 16);
    private static final BigInteger r = new BigInteger("9c7bc92503dce6cb52e5a27ad27456ff6f334a60eb6970736466fc14d2652d155f3b3d6cc4c80bbb73685e540c67f116febde0d26a700b1ad5c7617800756b50750599d57effa301cf3ea86a8aa64419c56af7a53901342b4ec6aabd5223bfac7aa999b2fdd7e4173c98c8bf9cdb979ec5b94dbab7277c1658d0c7e9e5bc7a26e6ddf98846ac9cf657b32f913da8b5f022c64cd273736d156a18a63aa0d9e4326d703c75b8f2b24b9a2b781865e2f1dba24326ba7ad81a518c7580e6e6a69065", 16);
    private static final BigInteger g_r = two.modPow(r, p);

    private BCC17CandidateECRH() {}

    public static class Builder extends CryptographicComponentBuilder<BCC17CandidateECRH> {
        @Override
        public BCC17CandidateECRH build(Deque<NamedEdge> parents) {
            return new BCC17CandidateECRH();
        }
    }

    public static CryptographicComponentBuilder<BCC17CandidateECRH> builder() {
        return new BCC17CandidateECRH.Builder();
    }

    @Override
    public String getName() {
        return "Bitansky2017-TheHuntingOfTheSNARK-Candidate-ECRH";
    }

    @Override
    public Hasher createHasher() {
        return new Hasher() {
            private OutputStream outputStream;
            private BigInteger g_r_x = g_r;

            @Override
            public OutputStream getOutputStream() {
                if (outputStream == null) {
                    outputStream = new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            if ((byte)b != b) {
                                throw new IOException("The supplied value is not a single byte.");
                            }

                            for (int i = 0; i < 8; i++) {
                                g_r_x = g_r_x.modPow(two, p);
                                if ((b & 1) == 1) {
                                    g_r_x = g_r_x.multiply(two).mod(p);
                                }
                                b = b >>> 1;
                            }
                        }
                    };
                }
                return outputStream;
            }

            @Override
            public byte[] hash() {
                try {
                    getOutputStream().write(new byte[] { 77, -92,  -28});
                } catch (IOException ignored) {

                }
                BigInteger g_alpha_r_x = g_r_x.modPow(alpha, p);
                byte[] result = new byte[bytelength];
                byte[] left = g_r_x.toByteArray();
                byte[] right = g_alpha_r_x.toByteArray();
                int leftLen = Math.min(left.length, result.length / 2);
                int rightLen = Math.min(right.length, result.length / 2);
                System.arraycopy(left, left.length - leftLen, result, result.length / 2 - leftLen, leftLen);
                System.arraycopy(right, right.length - rightLen, result, result.length - rightLen, rightLen);
                return result;
            }

            @Override
            public CryptographicComponent getComponent() {
                return BCC17CandidateECRH.this;
            }
        };
    }
}

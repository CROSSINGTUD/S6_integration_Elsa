package de.tu_darmstadt.crossing.composable_crypto.components.custom.hash_function;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Deque;

public class BCC17CandidateECRH implements HashFunction {
    private static final int BYTE_LENGTH_GROUP = 3072 / 8;
    private static final BigInteger two = BigInteger.valueOf(2);
    private static final BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF", 16);
    private static final BigInteger alpha = new BigInteger("6f09b49e1e973cb21de794503e85c6f661f6c3e4de7568ba239c9e48a8868a8ba8f81e2df9d592d4b4885073c2659cabe3aec65ebc03c229794b039b75769d5eb535904bcd367be2fea6e2e4b30b2bd6ed57ca828a765049bf64ce85e6c7d901c235dcee78fccd746a3107bb4a51c946f401888c3ad5b11f81c0814ed1462efe800a6b08b30d6eece9f925b15ab8ec7e09952ecd35b73006f4109188616be6d8a654a2b7c2210bba2e58d68a2486f17c6275e80a9bde05672e78d60ad893b91491bd12dc43077af2516cb3c6c071eaa23a0fe261773008307ce0346facbb4160040ada5205605cb4816b43371a9f02f6f88b880e7910354351f99282d140e388e5dd51caabd1890ad36beb0d4d43c2da91c4f9d4d529f921de7d198f6400622afab18d63351610f512b9e39e1ec25d97c38f970368be9ae1d509d249e68d47e4f39038a04f3a7906c8c60e429c99cb2d744050caa83647602f4c7be7165d8c65ab712757fb0e4baf2c52e6188c64e99d63e84ef0a27dd7484696c8393a867685", 16);
    private static final BigInteger r = new BigInteger("24619b068e468adea1043ea3835f557b56747b379275464c9e98af7abe2cbe33427f8bbf45c87f88ddab33d3636a2b0bc5daecd2985fa609dac003e2182138f1bb6bf0864ba53d05e5a46de11a5090fd0b1f2759dd68b1e1b5c6ecadec7606a6947c50db0f77968a1db3db596699801e5bfa9d857f564701736aecbede8c0257aabbbf0ca334bfbddf6637c7942e458e8b63dbf743955ba5d8aeec7d6cc08392d6312dc3b9c6d4adec0b9108ab157e18d1bff9de6c90438eb485af466633bfc14d8f632f9b771b1d9996baeac76dc9363e11cfb213f8ffa5462e10f854e8ef99a4404a1d70ee87c229529b2413f8fe7477a86870ced8447f8149a2ab4cf12028bfd0bdd56c9e2585c2e629ae8f7b789cb6500e364b1c508a437ccbaa7767932dd6c096564eb67a3b3a040f2a6bff9d73599b8f3f483bc57a54f94ccc3418973382d1641d3237980d1e72a761278701528f3c26d97908f1a0743e647b41cd99eeb484ba1e735c7c899effefda8600fb8f4b916c0fc99f506d12adf160f46d5ca6", 16);
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
                byte[] result = new byte[BYTE_LENGTH_GROUP + BYTE_LENGTH_GROUP];
                byte[] left = g_r_x.toByteArray();
                byte[] right = g_alpha_r_x.toByteArray();
                if (left.length > BYTE_LENGTH_GROUP + 1 || right.length > BYTE_LENGTH_GROUP + 1) {
                    throw new RuntimeException("Unexpected group element size");
                }
                int leftLen = Math.min(left.length, BYTE_LENGTH_GROUP);
                int rightLen = Math.min(right.length, BYTE_LENGTH_GROUP);
                System.arraycopy(left, left.length - leftLen, result, BYTE_LENGTH_GROUP - leftLen, leftLen);
                System.arraycopy(right, right.length - rightLen, result, BYTE_LENGTH_GROUP + BYTE_LENGTH_GROUP - rightLen, rightLen);
                return result;
            }

            @Override
            public CryptographicComponent getComponent() {
                return BCC17CandidateECRH.this;
            }
        };
    }

    @Override
    public int getHashLengthInBytes() {
        return BYTE_LENGTH_GROUP * 2;
    }
}

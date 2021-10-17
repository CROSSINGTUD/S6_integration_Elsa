package de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SignatureScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.TimestampScheme;

import java.util.Deque;

public class RemoteNonInteractiveSignatureBasedTimestamp implements TimestampScheme {
    private final SignatureScheme signatureScheme;

    private RemoteNonInteractiveSignatureBasedTimestamp(SignatureScheme signatureScheme) {
        this.signatureScheme = signatureScheme;
    }

    public static class Builder extends CryptographicComponentBuilder<RemoteNonInteractiveSignatureBasedTimestamp> {
        private final CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder;

        public Builder(CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder) {
            this.signatureSchemeBuilder = signatureSchemeBuilder;
        }

        @Override
        public RemoteNonInteractiveSignatureBasedTimestamp build(Deque<NamedEdge> parents) {
            return new RemoteNonInteractiveSignatureBasedTimestamp(signatureSchemeBuilder.build(parents, "Signature"));
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder) {
        return new Builder(signatureSchemeBuilder);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Stamper createTimestamp() {
        return null;
    }

    @Override
    public Verifier verifyTimestamp() {
        return null;
    }
}

package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.*;

import java.util.Deque;

public class ELSA implements LongTermStorage {
    private final TransportSecurity transportSecurity;
    private final SecretSharingScheme secretSharingScheme;
    private final SignatureScheme signatureScheme;
    private final TimestampScheme timestampScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;

    private ELSA(TransportSecurity transportSecurity, SecretSharingScheme secretSharingScheme, SignatureScheme signatureScheme, TimestampScheme timestampScheme, VectorCommitmentScheme vectorCommitmentScheme) {
        this.transportSecurity = transportSecurity;
        this.secretSharingScheme = secretSharingScheme;
        this.signatureScheme = signatureScheme;
        this.timestampScheme = timestampScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
    }

    public static class Builder extends CryptographicComponentBuilder<ELSA> {
        private final CryptographicComponentBuilder<? extends TransportSecurity> transportSecurityBuilder;
        private final CryptographicComponentBuilder<? extends SecretSharingScheme> secretSharingSchemeBuilder;
        private final CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder;
        private final CryptographicComponentBuilder<? extends  TimestampScheme> timestampSchemeBuilder;
        private final CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder;

        public Builder(CryptographicComponentBuilder<? extends TransportSecurity> transportSecurityBuilder, CryptographicComponentBuilder<? extends SecretSharingScheme> secretSharingSchemeBuilder, CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder, CryptographicComponentBuilder<? extends TimestampScheme> timestampSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
            this.transportSecurityBuilder = transportSecurityBuilder;
            this.secretSharingSchemeBuilder = secretSharingSchemeBuilder;
            this.signatureSchemeBuilder = signatureSchemeBuilder;
            this.timestampSchemeBuilder = timestampSchemeBuilder;
            this.vectorCommitmentSchemeBuilder = vectorCommitmentSchemeBuilder;
        }

        @Override
        public ELSA build(Deque<NamedEdge> parents) {
            return new ELSA(
                    transportSecurityBuilder.build(parents, "Channel"),
                    secretSharingSchemeBuilder.build(parents, "Share"),
                    signatureSchemeBuilder.build(parents, "Signature"),
                    timestampSchemeBuilder.build(parents, "Timestamp"),
                    vectorCommitmentSchemeBuilder.build(parents, "VC")
            );
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends TransportSecurity> transportSecurityBuilder, CryptographicComponentBuilder<? extends SecretSharingScheme> secretSharingSchemeBuilder, CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder, CryptographicComponentBuilder<? extends  TimestampScheme> timestampSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
        return new Builder(transportSecurityBuilder, secretSharingSchemeBuilder, signatureSchemeBuilder, timestampSchemeBuilder, vectorCommitmentSchemeBuilder);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public DataOwner createClient() {
        return null;
    }

    @Override
    public EvidenceService createEvidenceService() {
        return null;
    }

    @Override
    public Shareholder createShareholder() {
        return null;
    }
}

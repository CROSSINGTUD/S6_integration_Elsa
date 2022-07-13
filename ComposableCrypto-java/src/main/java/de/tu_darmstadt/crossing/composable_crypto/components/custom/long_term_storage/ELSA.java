package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.core.ComponentConfiguration;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.*;

import java.util.Deque;

public class ELSA implements LongTermStorage {
    private final SecretSharingScheme secretSharingScheme;
    private final SignatureScheme signatureScheme;
    private final TimestampScheme timestampScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;
    private final String evidenceServiceURL;
    private final String[] shareholderURLs;
    private final EvidenceServiceAdapter evidenceServiceAdapter;
    private final ShareholderAdapter shareholderAdapter;
    private final EvidenceServiceStorageAdapter evidenceServiceStorageAdapter;
    private final Integer secretSharingThreshold;

    private ELSA(SecretSharingScheme secretSharingScheme, SignatureScheme signatureScheme, TimestampScheme timestampScheme, VectorCommitmentScheme vectorCommitmentScheme, String evidenceServiceURL, String[] shareholderURLs, EvidenceServiceAdapter evidenceServiceAdapter, ShareholderAdapter shareholderAdapter, EvidenceServiceStorageAdapter evidenceServiceStorageAdapter, Integer secretSharingThreshold) {
        this.secretSharingScheme = secretSharingScheme;
        this.signatureScheme = signatureScheme;
        this.timestampScheme = timestampScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
        this.evidenceServiceURL = evidenceServiceURL;
        this.shareholderURLs = shareholderURLs;
        this.evidenceServiceAdapter = evidenceServiceAdapter;
        this.shareholderAdapter = shareholderAdapter;
        this.evidenceServiceStorageAdapter = evidenceServiceStorageAdapter;
        this.secretSharingThreshold = secretSharingThreshold;
    }

    public static class Builder extends CryptographicComponentBuilder<ELSA> {
        private final CryptographicComponentBuilder<? extends SecretSharingScheme> secretSharingSchemeBuilder;
        private final CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder;
        private final CryptographicComponentBuilder<? extends  TimestampScheme> timestampSchemeBuilder;
        private final CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder;
        private String evidenceServiceURL;
        private String[] shareholderURLs;
        private EvidenceServiceAdapter evidenceServiceAdapter;
        private ShareholderAdapter shareholderAdapter;
        private EvidenceServiceStorageAdapter evidenceServiceStorageAdapter;
        private Integer secretSharingThreshold;

        public Builder(CryptographicComponentBuilder<? extends SecretSharingScheme> secretSharingSchemeBuilder, CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder, CryptographicComponentBuilder<? extends TimestampScheme> timestampSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
            this.secretSharingSchemeBuilder = secretSharingSchemeBuilder;
            this.signatureSchemeBuilder = signatureSchemeBuilder;
            this.timestampSchemeBuilder = timestampSchemeBuilder;
            this.vectorCommitmentSchemeBuilder = vectorCommitmentSchemeBuilder;
        }

        public Builder evidenceServiceURL(String evidenceServiceURL) {
            this.evidenceServiceURL = evidenceServiceURL;
            return this;
        }

        public Builder shareholderURLs(String[] shareholderURLs) {
            this.shareholderURLs = shareholderURLs;
            return this;
        }

        public Builder evidenceServiceAdapter(EvidenceServiceAdapter evidenceServiceAdapter) {
            this.evidenceServiceAdapter = evidenceServiceAdapter;
            return this;
        }

        public Builder shareholderAdapter(ShareholderAdapter shareholderAdapter) {
            this.shareholderAdapter = shareholderAdapter;
            return this;
        }

        public Builder evidenceServiceStorageAdapter(EvidenceServiceStorageAdapter evidenceServiceStorageAdapter) {
            this.evidenceServiceStorageAdapter = evidenceServiceStorageAdapter;
            return this;
        }

        public Builder secretSharingThreshold(int secretSharingThreshold) {
            this.secretSharingThreshold = secretSharingThreshold;
            return this;
        }

        @Override
        public ELSA build(Deque<NamedEdge> parents) {
            ComponentConfiguration configuration = getConfiguration();

            if (configuration != null) {
                if (evidenceServiceURL == null) {
                    evidenceServiceURL = (String) configuration.query("evidenceServiceURL", parents);
                }

                if (shareholderURLs == null) {
                    shareholderURLs = (String[]) configuration.query("shareholderURLs", parents);
                }

                if (evidenceServiceAdapter == null) {
                    evidenceServiceAdapter = (EvidenceServiceAdapter) configuration.query("evidenceServiceAdapter", parents);
                }

                if (shareholderAdapter == null) {
                    shareholderAdapter = (ShareholderAdapter) configuration.query("shareholderAdapter", parents);
                }

                if (evidenceServiceStorageAdapter == null) {
                    evidenceServiceStorageAdapter = (EvidenceServiceStorageAdapter) configuration.query("evidenceServiceStorageAdapter", parents);
                }

                if (secretSharingThreshold == null) {
                    secretSharingThreshold = (Integer) configuration.query("secretSharingThreshold", parents);
                }
            }

            return new ELSA(
                    secretSharingSchemeBuilder.build(parents, "Share"),
                    signatureSchemeBuilder.build(parents, "Signature"),
                    timestampSchemeBuilder.build(parents, "Timestamp"),
                    vectorCommitmentSchemeBuilder.build(parents, "VC"),
                    evidenceServiceURL,
                    shareholderURLs,
                    evidenceServiceAdapter,
                    shareholderAdapter,
                    evidenceServiceStorageAdapter,
                    secretSharingThreshold
            );
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends SecretSharingScheme> secretSharingSchemeBuilder, CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder, CryptographicComponentBuilder<? extends  TimestampScheme> timestampSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
        return new Builder(secretSharingSchemeBuilder, signatureSchemeBuilder, timestampSchemeBuilder, vectorCommitmentSchemeBuilder);
    }

    @Override
    public String getName() {
        return "ELSA";
    }

    @Override
    public ELSAClient createClient() {
        return new ELSAClient(this, secretSharingScheme, signatureScheme, timestampScheme, vectorCommitmentScheme, evidenceServiceURL, shareholderURLs, evidenceServiceAdapter, shareholderAdapter, secretSharingThreshold);
    }

    @Override
    public ELSAEvidenceService createEvidenceService() {
        return new ELSAEvidenceService(vectorCommitmentScheme, timestampScheme, evidenceServiceStorageAdapter);
    }

    @Override
    public Shareholder createShareholder() {
        // Currently, the shareholders are just a data store without any cryptographic operations.
        throw new UnsupportedOperationException();
    }
}

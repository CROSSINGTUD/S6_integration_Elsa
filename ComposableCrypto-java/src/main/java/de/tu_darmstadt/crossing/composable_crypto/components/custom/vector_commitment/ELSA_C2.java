package de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.CommitmentScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;

import java.util.Deque;

public class ELSA_C2 implements VectorCommitmentScheme {
    private final CommitmentScheme commitmentScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;

    private ELSA_C2(CommitmentScheme commitmentScheme, VectorCommitmentScheme vectorCommitmentScheme) {
        this.commitmentScheme = commitmentScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
    }

    public static class Builder extends CryptographicComponentBuilder<ELSA_C2> {
        private final CryptographicComponentBuilder<? extends CommitmentScheme> commitmentSchemeBuilder;
        private final CryptographicComponentBuilder<? extends  VectorCommitmentScheme> vectorCommitmentSchemeBuilder;

        public Builder(CryptographicComponentBuilder<? extends CommitmentScheme> commitmentSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
            this.commitmentSchemeBuilder = commitmentSchemeBuilder;
            this.vectorCommitmentSchemeBuilder = vectorCommitmentSchemeBuilder;
        }

        @Override
        public ELSA_C2 build(Deque<NamedEdge> parents) {
            return new ELSA_C2(commitmentSchemeBuilder.build(parents, "COM"), vectorCommitmentSchemeBuilder.build(parents, "VC"));
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends CommitmentScheme> commitmentSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
        return new Builder(commitmentSchemeBuilder, vectorCommitmentSchemeBuilder);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Committer createCommitment() {
        return null;
    }

    @Override
    public Opener open() {
        return null;
    }

    @Override
    public Verifier verifyCommitment() {
        return null;
    }
}

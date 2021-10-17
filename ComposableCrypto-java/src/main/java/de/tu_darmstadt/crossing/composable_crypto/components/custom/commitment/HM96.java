package de.tu_darmstadt.crossing.composable_crypto.components.custom.commitment;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.CommitmentScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;

import java.util.Deque;

public class HM96 implements CommitmentScheme {
    private final HashFunction hashFunction;

    private HM96(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public static class Builder extends CryptographicComponentBuilder<HM96> {
        private final CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder;

        public Builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
            this.hashFunctionBuilder = hashFunctionBuilder;
        }

        @Override
        public HM96 build(Deque<NamedEdge> parents) {
            return new HM96(hashFunctionBuilder.build(parents, "Hash"));
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
        return new Builder(hashFunctionBuilder);
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
    public Verifier verifyCommitment() {
        return null;
    }
}

package de.tu_darmstadt.crossing.composable_crypto.components.custom.transport_security;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.TransportSecurity;

import java.util.Deque;

public class TLS13 implements TransportSecurity {
    private TLS13() {}

    public static class Builder extends CryptographicComponentBuilder<TLS13> {

        @Override
        public TLS13 build(Deque<NamedEdge> parents) {
            return new TLS13();
        }
    }

    public static CryptographicComponentBuilder<TLS13> builder() {
        return new Builder();
    }

    @Override
    public String getName() {
        return "TLS1.3";
    }
}

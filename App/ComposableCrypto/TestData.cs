using ComposableCrypto.Core;
using ComposableCrypto.CrySLGen;

namespace ComposableCrypto
{
    public class TestData
    {
        public static Component CreateELSA() => ComponentRegistry.CreateComponent("ELSA", null);
        public static void LoadTestData()
        {
            TypeRegistry.RegisterInterface("commitment-scheme", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");
            TypeRegistry.RegisterInterface("hash-function", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");
            TypeRegistry.RegisterInterface("secure-long-term-storage", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");
            TypeRegistry.RegisterInterface("secret-sharing-scheme", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");
            TypeRegistry.RegisterInterface("signature-scheme", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");
            TypeRegistry.RegisterInterface("timestamp-scheme", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");
            TypeRegistry.RegisterInterface("transport-security", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");
            TypeRegistry.RegisterInterface("vector-commitment-scheme", "de.tu_darmstadt.crossing.composable_crypto.interfaces.");

            TypeRegistry.RegisterComponentClass("HM96", "de.tu_darmstadt.crossing.composable_crypto.components.custom.commitment.HM96");
            TypeRegistry.RegisterComponentClass("BCC17-ECRH", "de.tu_darmstadt.crossing.composable_crypto.components.custom.hash_function.BCC17CandidateECRH");
            TypeRegistry.RegisterComponentClass("ELSA", "de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSA");
            TypeRegistry.RegisterComponentClass("Shamirs_Secret_Sharing", "de.tu_darmstadt.crossing.composable_crypto.components.custom.secret_sharing.ShamirsSecretSharing");
            TypeRegistry.RegisterComponentClass("Remote_interactive_Signature_based_Timestamp", "de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp.RemoteInteractiveSignatureBasedTimestamp");
            TypeRegistry.RegisterComponentClass("Remote_non_interactive_Signature_based_Timestamp", "de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp.RemoteNonInteractiveSignatureBasedTimestamp");
            TypeRegistry.RegisterComponentClass("TLS13", "de.tu_darmstadt.crossing.composable_crypto.components.custom.transport_security.TLS13");
            TypeRegistry.RegisterComponentClass("ELSA_C2", "de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment.ELSA_C2");
            TypeRegistry.RegisterComponentClass("ELSA_C1", "de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment.MerkleTreeVectorCommitment");
            TypeRegistry.RegisterComponentClass("SHA256", "de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256");
            TypeRegistry.RegisterComponentClass("RSA_Signature_With_Hash", "de.tu_darmstadt.crossing.composable_crypto.components.jca.signature.RSAWithHash");

            AssumptionRegistry.RegisterAssumption("DLP.is-hard");
            AssumptionRegistry.RegisterAssumption("RSA.is-hard");
            AssumptionRegistry.RegisterAssumption("1-KEA.holds");
            AssumptionRegistry.RegisterAssumption("model-tls-as-secure-channel");
            AssumptionRegistry.RegisterAssumption("model-hash-functions-as-random-oracles");

            AssumptionProfile assumptionProfile = new AssumptionProfile { Name = "(Default)" };
            assumptionProfile.Assumptions.Add("DLP.is-hard", true);
            assumptionProfile.Assumptions.Add("RSA.is-hard", true);
            assumptionProfile.Assumptions.Add("1-KEA.holds", true);
            assumptionProfile.Assumptions.Add("model-tls-as-secure-channel", true);
            assumptionProfile.Assumptions.Add("model-hash-functions-as-random-oracles", true);
            AssumptionRegistry.RegisterAssumptionProfile(assumptionProfile);

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("secure-long-term-storage")
                .Name("ELSA")
                .DisplayName("ELSA")
                .WithChild("Share", "Secret sharing scheme", "secret-sharing-scheme")
                .WithChild("Signature", "Signature scheme", "signature-scheme")
                .WithChild("VC", "Vector commitment scheme", "vector-commitment-scheme")
                .WithChild("Channel", "Secure channel", "secure-channel")
                .WithChild("Timestamp", "Timestamp scheme", "timestamp-scheme")
                .WithProperty("ensures-unforgeability",
                    And.Of(
                        new ChildPropertyCondition("Signature", "ensures-unforgeability"),
                        new ChildPropertyCondition("VC", "extractable-binding"),
                        new ChildPropertyCondition("Channel", "ensures-authenticity"),
                        new ChildPropertyCondition("Channel", "ensures-integrity"),
                        new ChildPropertyCondition("Timestamp", "ensures-unforgeability")
                    )
                )
                .WithProperty("ensures-inf-th-confidentiality",
                    And.Of(
                        new ChildPropertyCondition("Share", "statistically-hiding"),
                        new ChildPropertyCondition("VC", "statistically-hiding-under-selective-opening"),
                        new ChildPropertyCondition("Channel", "ensures-inf-th-confidentiality")
                    )
                )
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("commitment-scheme")
                .Name("HM96")
                .DisplayName("HM96")
                .WithChild("ECRH", "Hash function", "hash-function")
                .WithProperty("statistically-hiding", Condition.TRUE)
                .WithProperty("extractable-binding", new And(new ChildPropertyCondition("ECRH", "extractable-binding"), new ChildPropertyCondition("ECRH", "collision-resistant")))
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("hash-function")
                .Name("SHA256")
                .DisplayName("SHA-256")
                .WithProperty("collision-resistant", Condition.TRUE)
                .WithProperty("preimage-resistant", Condition.TRUE)
                .WithProperty("extractable-binding", new AssumptionCondition("model-hash-functions-as-random-oracles"))
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("hash-function")
                .Name("BCC17-ECRH")
                .DisplayName("BCC+17 ECRH")
                .WithProperty("collision-resistant", new AssumptionCondition("DLP.is-hard"))
                .WithProperty("extractable", new AssumptionCondition("1-KEA.holds"))
                .WithProperty("extractable-binding", new And(new AssumptionCondition("DLP.is-hard"), new AssumptionCondition("1-KEA.holds")))
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("secret-sharing-scheme")
                .Name("Shamirs_Secret_Sharing")
                .DisplayName("Shamir's Secret Sharing Scheme")
                .WithProperty("statistically-hiding", Condition.TRUE)
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("timestamp-scheme")
                .Name("Remote_interactive_Signature_based_Timestamp")
                .DisplayName("Remote interactive signature-based timestamp")
                .WithChild("Signature", "Signature scheme", "signature-scheme")
                .WithProperty("ensures-unforgeability", new ChildPropertyCondition("Signature", "ensures-unforgeability"))
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("timestamp-scheme")
                .Name("Remote_non_interactive_Signature_based_Timestamp")
                .DisplayName("Remote non-interactive signature-based timestamp")
                .WithChild("Signature", "Signature scheme", "signature-scheme")
                .WithProperty("ensures-unforgeability", new ChildPropertyCondition("Signature", "ensures-unforgeability"))
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("secure-channel")
                .Name("TLS13")
                .DisplayName("TLS 1.3")
                .WithProperty("ensures-integrity", Condition.TRUE)
                .WithProperty("ensures-confidentiality", Condition.TRUE)
                .WithProperty("ensures-authenticity", Condition.TRUE)
                .WithProperty("ensures-inf-th-confidentiality", new AssumptionCondition("model-tls-as-secure-channel"))
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("vector-commitment-scheme")
                .Name("ELSA_C2")
                .DisplayName("ELSA Construction 2")
                .WithChild("COM", "Commitment scheme", "commitment-scheme")
                .WithChild("VC", "Vector commitment scheme", "vector-commitment-scheme")
                .WithProperty("statistically-hiding-under-selective-opening", new ChildPropertyCondition("COM", "statistically-hiding"))
                .WithProperty("extractable-binding", new And(new ChildPropertyCondition("COM", "extractable-binding"), new ChildPropertyCondition("VC", "extractable-binding")))
                .Build()
            );

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("vector-commitment-scheme")
                .Name("ELSA_C1")
                .DisplayName("ELSA Construction 1")
                .WithChild("ECRH", "Hash function", "hash-function")
                .WithProperty("extractable-binding", new ChildPropertyCondition("ECRH", "extractable-binding"))
                .Build()
            );

            //ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
            //    .Type("hash-function")
            //    .Name("Hash-Concat")
            //    .DisplayName("Concatenation of two hashes")
            //    .WithChild("H1", "Hash1", "hash-function")
            //    .WithChild("H2", "Hash2", "hash-function")
            //    .WithProperty("collision-resistant", new Or(new ChildPropertyCondition("H1", "collision-resistant"), new ChildPropertyCondition("H2", "collision-resistant")))
            //    .WithProperty("preimage-resistant", new Or(new ChildPropertyCondition("H1", "preimage-resistant"), new ChildPropertyCondition("H2", "preimage-resistant")))
            //    .WithProperty("extractable-binding", new Or(new ChildPropertyCondition("H1", "extractable-binding"), new ChildPropertyCondition("H2", "extractable-binding")))
            //    .Build()
            //);

            ComponentRegistry.RegisterComponent(ComponentDescription.Builder()
                .Type("signature-scheme")
                .Name("RSA_Signature_With_Hash")
                .DisplayName("RSA with hash")
                .WithChild("Hash", "Hash function", "hash-function")
                .WithProperty("ensures-unforgeability", new And(new ChildPropertyCondition("Hash", "collision-resistant"), new AssumptionCondition("RSA.is-hard")))
                .Build()
            );
        }
    }
}

package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

public class SignedFileWithPoI {
    private final SignedFile signedFile;
    private final ProofOfIntegrity proofOfIntegrity;

    public SignedFileWithPoI(SignedFile signedFile, ProofOfIntegrity proofOfIntegrity) {
        this.signedFile = signedFile;
        this.proofOfIntegrity = proofOfIntegrity;
    }

    public byte[] toByteArray() {
        return toByteArray(proofOfIntegrity.getEvidenceItems().size());
    }

    public byte[] toByteArray(int firstNPoIItems) {
        byte[] result = new byte[signedFile.getByteSize() + proofOfIntegrity.getByteSize(firstNPoIItems)];
        int index = signedFile.writeToByteArray(result, 0);
        proofOfIntegrity.writeToByteArray(result, index, firstNPoIItems);
        return result;
    }
}

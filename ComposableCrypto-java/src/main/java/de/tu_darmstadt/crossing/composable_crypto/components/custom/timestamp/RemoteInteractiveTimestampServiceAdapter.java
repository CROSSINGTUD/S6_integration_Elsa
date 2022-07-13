package de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp;

import de.tu_darmstadt.crossing.composable_crypto.interfaces.TimestampScheme;

public interface RemoteInteractiveTimestampServiceAdapter extends RemoteNonInteractiveTimestampServiceAdapter {
    boolean verifyTimestamp(byte[] data, TimestampScheme.Timestamp timestamp);
}

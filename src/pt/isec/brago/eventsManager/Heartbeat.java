package pt.isec.brago.eventsManager;

import java.io.Serial;
import java.io.Serializable;

public class Heartbeat implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int port;
    private final String registryName;
    private int dbVersion;

    public Heartbeat(int port, int dbVersion, String registryName) {
        this.port = port;
        this.dbVersion = dbVersion;
        this.registryName = registryName;
    }

    public void changeVersion(int version) {
        this.dbVersion = version;
    }
}

package java.security.cert;

public class LDAPCertStoreParameters implements CertStoreParameters {
    private static final int LDAP_DEFAULT_PORT = 389;
    private int port;
    private String serverName;
    
    public LDAPCertStoreParameters(String serverName, int port) {
        
        if (serverName == null) throw new NullPointerException();
        this.serverName = serverName;
        this.port = port;
    }
    
    public LDAPCertStoreParameters(String serverName) {
        this(serverName, LDAP_DEFAULT_PORT);
    }
    
    public LDAPCertStoreParameters() {
        this("localhost", LDAP_DEFAULT_PORT);
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public int getPort() {
        return port;
    }
    
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LDAPCertStoreParameters: [\n");
        sb.append("  serverName: " + serverName + "\n");
        sb.append("  port: " + port + "\n");
        sb.append("]");
        return sb.toString();
    }
}

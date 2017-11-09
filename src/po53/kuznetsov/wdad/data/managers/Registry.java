package po53.kuznetsov.wdad.data.managers;

public class Registry {
    private boolean createRegistry;
    private String address;
    private int port;

    public Registry(boolean createRegistry, String address, int port) {
        checkPort(port);
        this.createRegistry = createRegistry;
        this.address = address;
        this.port = port;
    }

    public boolean isCreateRegistry() {
        return createRegistry;
    }

    public void setCreateRegistry(boolean createRegistry) {
        this.createRegistry = createRegistry;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        checkPort(port);
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Registry registry = (Registry) o;

        if (createRegistry != registry.createRegistry) return false;
        if (port != registry.port) return false;
        return address != null ? address.equals(registry.address) : registry.address == null;
    }

    @Override
    public int hashCode() {
        int result = (createRegistry ? 1 : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    private void checkPort(int port) {
        if (port < 0 || port >  65535) {
            throw new IllegalArgumentException("Port: " + port + ". Expected: 0 <= port <= 65535.");
        }
    }
}

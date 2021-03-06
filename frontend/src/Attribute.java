import java.io.Serializable;

public class Attribute implements Serializable {
    private String name;
    private String type;
    private boolean primaryKey;
    private boolean autoIncrement;
    private boolean index;
    private boolean notNull;

    public Attribute() { }

    public Attribute(String name, String type, boolean primaryKey, boolean autoIncrement, boolean index, boolean notNull) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
        this.index = index;
        this.notNull = notNull;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", primaryKey=" + primaryKey +
                ", autoIncrement=" + autoIncrement +
                ", index=" + index +
                ", notNull=" + notNull +
                '}';
    }
}

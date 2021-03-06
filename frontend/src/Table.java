import java.io.Serializable;
import java.util.List;

public class Table implements Serializable {
    private String name;
    private List<Attribute>attributes;
    private List<ForeignKey>foreignKeys;

    public Table() { }

    public Table(String name, List<Attribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public Table(String name, List<Attribute> attributes, List<ForeignKey> foreignKeys) {
        this.name = name;
        this.attributes = attributes;
        this.foreignKeys = foreignKeys;
    }

    @Override
    public String toString() {
        return "Table{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                ", foreignKeys=" + foreignKeys +
                '}';
    }
}

import java.util.List;

public class Table {
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
}

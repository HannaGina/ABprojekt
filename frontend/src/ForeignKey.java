import java.io.Serializable;

public class ForeignKey implements Serializable {
    private String attrName;
    private String otherTable;
    private String attrOnOtherTable;

    @Override
    public String toString() {
        return "ForeignKey{" +
                "attrName='" + attrName + '\'' +
                ", otherTable='" + otherTable + '\'' +
                ", attrOnOtherTable='" + attrOnOtherTable + '\'' +
                '}';
    }
}

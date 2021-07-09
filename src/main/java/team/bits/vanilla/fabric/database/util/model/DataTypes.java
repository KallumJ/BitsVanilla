package team.bits.vanilla.fabric.database.util.model;

import org.jetbrains.annotations.NotNull;

public final class DataTypes {

    public static final PrimitiveDataType<Integer> INTEGER = new PrimitiveDataType(Integer.class);
    public static final PrimitiveDataType<Long> LONG = new PrimitiveDataType(Long.class);
    public static final PrimitiveDataType<Short> SHORT = new PrimitiveDataType(Short.class);
    public static final PrimitiveDataType<String> STRING = new PrimitiveDataType(String.class);
    public static final PrimitiveDataType<Float> FLOAT = new PrimitiveDataType(Float.class);
    public static final PrimitiveDataType<Boolean> BOOLEAN = new PrimitiveDataType(Boolean.class);
    private static final IDataObjectType[] TYPES = {INTEGER, LONG, SHORT, STRING, FLOAT, BOOLEAN};

    private DataTypes() {
    }

    public static @NotNull IDataObjectType getType(Class type) {
        for (IDataObjectType dataType : TYPES) {
            if (dataType.getType().equals(type)) {
                return dataType;
            }
        }
        throw new RuntimeException("Cannot find data type for class " + type.getSimpleName());
    }
}

package team.bits.vanilla.fabric.database.util.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PrimitiveDataType<T> implements IDataObjectType<T> {

    private final Class type;

    public PrimitiveDataType(@NotNull Class<? extends T> type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public @NotNull Class<? extends T> getType() {
        return this.type;
    }

    @Override
    public @NotNull IDataObject<T> create(T object) {
        return new PrimitiveDataObject(object);
    }

    private record PrimitiveDataObject<T>(T value) implements IDataObject<T> {

        @Override
        public @NotNull Object serialize() {
            return this.value;
        }

        @Override
        public @NotNull T get() {
            return this.value;
        }
    }
}

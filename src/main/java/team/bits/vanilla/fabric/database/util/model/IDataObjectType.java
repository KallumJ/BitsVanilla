package team.bits.vanilla.fabric.database.util.model;

import org.jetbrains.annotations.NotNull;

public interface IDataObjectType<T> {

    @NotNull Class<? extends T> getType();

    @NotNull IDataObject<T> create(T object);
}

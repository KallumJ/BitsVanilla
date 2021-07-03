package team.bits.vanilla.fabric.database.util.model;

import org.jetbrains.annotations.NotNull;

public interface IDataObject<T> {

    @NotNull Object serialize();

    @NotNull T get();
}

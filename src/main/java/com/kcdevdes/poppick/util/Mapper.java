package com.kcdevdes.poppick.util;

public interface Mapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);
}
package com.lucky.jacklamb.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity与Dto相互转化的泛型接口
 * @param <E> Entity
 * @param <D> Dto
 */
public interface LuckyConversion<E,D> {

    /**
     * Dao转为Entity
     * @param dto Dto对象
     * @return Entity对象
     */
     E toEntity(D dto);

    /**
     * List[Dao]转为List[Entity]
     * @param dtoList Dao集合
     * @return
     */
     default List<E> toEntityList(List<D> dtoList){
         return dtoList.stream().map(d->toEntity(d)).collect(Collectors.toList());
     }

    /**
     * Entity转为Dao
     * @param entity Entity对象
     * @return Dao对象
     */
     D toDto(E entity);

    /**
     * List[Entity]转为List[Dao]
     * @param entityList Entity集合
     * @return
     */
     default List<D> toDtoList(List<E> entityList){
         return entityList.stream().map(e->toDto(e)).collect(Collectors.toList());
     }

}

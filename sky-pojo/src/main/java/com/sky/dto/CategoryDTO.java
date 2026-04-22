package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class CategoryDTO implements Serializable {

    //主键
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CategoryDTO that = (CategoryDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(name, that.name) && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, name, sort);
    }

    //类型 1 菜品分类 2 套餐分类
    private Integer type;

    //分类名称
    private String name;

    //排序
    private Integer sort;

}

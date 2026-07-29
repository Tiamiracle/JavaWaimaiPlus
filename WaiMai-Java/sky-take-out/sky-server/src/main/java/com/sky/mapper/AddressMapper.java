package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressMapper {
    @Insert("INSERT INTO address_book (user_id, consignee, phone, sex, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) VALUES (#{userId}, #{consignee}, #{phone}, #{sex}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void save(AddressBook addressBook);
    @Select("SELECT * FROM address_book WHERE user_id = #{userId}")
    List<AddressBook> getlist(Long userId);
    @Select("SELECT * FROM address_book WHERE id = #{id}")
    AddressBook getAddressById(Long id);
    void update(AddressBook addressBook);
    @Delete("DELETE FROM address_book WHERE id = #{id}")
    void delete(Long id);
    @Update("UPDATE address_book SET is_default = 0 WHERE user_id = #{userId}")
    void cleanDefault(Long userId);
    @Select("SELECT * FROM address_book WHERE user_id = #{userId} AND is_default = 1")
    AddressBook getDefaultAddress(Long userId);
}

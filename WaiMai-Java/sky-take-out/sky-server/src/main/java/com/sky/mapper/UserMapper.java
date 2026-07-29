package com.sky.mapper;

import com.sky.aspect.AutoFill;
import com.sky.dto.UserCountDTO;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User getByOpenid(String openid);
    @AutoFill(value = OperationType.INSERT)
    void insert(User user);
    @Select("SELECT * FROM user WHERE id = #{id}")
    User getById(Long id);
    UserCountDTO getUserList(LocalDate begin, LocalDate end);
    @Update("update user set name = #{name}, sex = #{sex}, avatar = #{avatar}, id_number = #{idNumber} WHERE id = #{id}")
    void update(User user1);
}

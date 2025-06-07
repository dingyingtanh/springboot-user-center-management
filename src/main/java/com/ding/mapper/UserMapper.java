package com.ding.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ding.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author dingy
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-05-28 17:12:10
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}





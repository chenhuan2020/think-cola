package com.amos.think.gateway.impl;

import com.amos.think.common.util.DesSecretUtil;
import com.amos.think.common.util.RandomUtil;
import com.amos.think.convertor.UserConvertor;
import com.amos.think.domain.user.gateway.UserGateway;
import com.amos.think.domain.user.model.UserEntity;
import com.amos.think.dto.query.UserListByNameQuery;
import com.amos.think.gateway.impl.database.dataobject.UserDO;
import com.amos.think.gateway.impl.database.mapper.UserMapper;
import com.amos.think.gateway.impl.database.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserGatewayImpl
 *
 * @author <a href="mailto:daoyuan0626@gmail.com">amos.wang</a>
 * @date 2021/1/8
 */
@Component("userGateway")
public class UserGatewayImpl implements UserGateway {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRepository userRepository;

    @Override
    public void save(UserEntity userEntity) {
        UserDO userDO = null;
        if (StringUtils.isNotBlank(userEntity.getId())) {
            Optional<UserDO> byId = userRepository.findById(userEntity.getId());
            if (byId.isPresent()) {

                // 更新
                userDO = byId.get();
                UserConvertor.mergeDataObject(userEntity, userDO);
            }
        }

        if (userDO == null) {
            // 生成密码盐
            String salt = RandomUtil.generateLetterString(8);
            String encryptPassword = DesSecretUtil.encrypt(userEntity.getPassword(), salt);
            userEntity.setSalt(salt);
            userEntity.setPassword(encryptPassword);

            // 新增
            userDO = UserConvertor.toDataObject(userEntity);
        }

        userRepository.save(userDO);
    }

    @Override
    public UserEntity getPasswordInfo(String username) {
        return UserConvertor.toEntity(userMapper.getPasswordInfo(username));
    }

    @Override
    public UserEntity getUserInfo(String username) {
        return UserConvertor.toEntity(userMapper.getUserInfo(username));
    }

    @Override
    public List<UserEntity> listByName(UserListByNameQuery query) {
        return userMapper.listByName(query).stream()
                .map(UserConvertor::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean existUsername(String userId, String username) {
        return userMapper.existUsername(userId, username);
    }

}

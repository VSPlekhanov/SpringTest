package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.converter.contract.AllFieldModelDtoConverter;
import com.epam.lstrsum.converter.contract.BasicModelDtoConverter;
import com.epam.lstrsum.dto.user.UserAllFieldsDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAggregator implements BasicModelDtoConverter<User, UserBaseDto>,
        AllFieldModelDtoConverter<User, UserAllFieldsDto> {
    private final UserDtoMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public UserAllFieldsDto modelToAllFieldsDto(User user) {
        return userMapper.modelToAllFieldsDto(user);
    }

    @Override
    public UserBaseDto modelToBaseDto(User user) {
        return userMapper.modelToBaseDto(user);
    }

    public List<UserBaseDto> allowedSubsToListOfUserBaseDtos(List<User> allowedSubs) {
        return userMapper.allowedSubsToListOfUserBaseDtos(allowedSubs);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> {
                log.debug("No user with such email = {}", email);
                return new NoSuchUserException("No user with such email = "+ email);
        });
    }
}
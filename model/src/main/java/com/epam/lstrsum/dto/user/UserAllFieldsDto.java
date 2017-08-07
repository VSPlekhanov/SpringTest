package com.epam.lstrsum.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
public class UserAllFieldsDto extends UserBaseDto {
    private String[] roles;
    private Instant createdAt;
    private Boolean isActive;

    public UserAllFieldsDto(String userId, String firstName, String lastName, String email, String[] roles,
                            Instant createdAt, Boolean isActive) {
        super(userId, firstName, lastName, email);
        this.roles = roles;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserAllFieldsDto that = (UserAllFieldsDto) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(roles, that.roles)) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        return isActive != null ? isActive.equals(that.isActive) : that.isActive == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(roles);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        return result;
    }
}

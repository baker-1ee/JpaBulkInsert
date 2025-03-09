package com.example.bookstore.domain.user.entity;

import com.example.bookstore.common.jpa.entity.BasicEntityJ;
import com.example.bookstore.domain.user.dto.UserSaveDto;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Getter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Users")
public class User extends BasicEntityJ {

    @Id
    @Column
    private Long userSeq;

    @Column
    private String name;

    public static User from(UserSaveDto dto) {
        return User.builder()
                .userSeq(UUID.randomUUID().getMostSignificantBits())
                .name(dto.getName())
                .build();
    }

}

package org.example.service.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeUserInfoIData {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String currentPassword; // for verification
}

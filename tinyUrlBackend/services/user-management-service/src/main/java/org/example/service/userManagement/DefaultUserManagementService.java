package org.example.service.userManagement;

import org.example.service.data.UserInfoIData;
import org.example.service.data.UserInfoOData;
import org.example.service.data.ChangeUserInfoIData;
import org.example.service.data.ChangeUserInfoOData;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserManagementService implements UserManagementService {

    @Override
    public UserInfoOData getUserInfo(UserInfoIData input) {
        // TODO: Implement get user info logic
        return null;
    }

    @Override
    public ChangeUserInfoOData changeUserInfo(ChangeUserInfoIData input) {
        // TODO: Implement change user info logic
        return null;
    }
}

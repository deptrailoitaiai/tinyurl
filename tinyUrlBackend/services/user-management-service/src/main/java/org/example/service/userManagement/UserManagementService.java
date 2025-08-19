package org.example.service.userManagement;

import org.example.service.data.UserInfoIData;
import org.example.service.data.UserInfoOData;
import org.example.service.data.ChangeUserInfoIData;
import org.example.service.data.ChangeUserInfoOData;

public interface UserManagementService {
    UserInfoOData getUserInfo(UserInfoIData input);

    ChangeUserInfoOData changeUserInfo(ChangeUserInfoIData input);
}

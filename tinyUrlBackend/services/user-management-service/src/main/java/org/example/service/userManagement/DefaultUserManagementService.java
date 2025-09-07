package org.example.service.userManagement;

import jakarta.transaction.Transactional;
import org.example.constants.ErrorCode;
import org.example.entity.User;
import org.example.repository.master.UserMasterRepository;
import org.example.service.data.*;
import org.example.util.HashAndCompareUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service("DefaultUserManagementService")
public class DefaultUserManagementService implements UserManagementService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public UserInfoOData getUserInfo(UserInfoIData input) {
        UserInfoOData ret = new UserInfoOData();

        try {
            // Try to get from cache first
            UserInfoCacheData cachedData = getCachedUserInfo(input.getUserId());
            if (cachedData != null) {
                return cachedData.toUserInfoOData(input.getUserId());
            }

            // Cache miss - fetch from database (using master for read operations)
            Optional<User> getUser = userMasterRepository.findById(input.getUserId());

            if (getUser.isEmpty()) {
                ret.setErrCode(ErrorCode.USER_NOT_EXISTED);
                return ret;
            }

            User user = getUser.get();

            // Cache the user info for future use
            UserInfoCacheData cacheData = UserInfoCacheData.fromUser(user);
            cacheUserInfo(input.getUserId(), cacheData);

            // Set response
            ret.setErrCode(ErrorCode.SUCCESS);
            ret.setUserId(user.getId());
            ret.setEmail(user.getEmail());
            ret.setFullName(user.getFullName());
            ret.setLastUpdate(user.getUpdatedAt());
            ret.setLastLoginAt(user.getLastLoginAt());

            return ret;

        } catch (Exception e) {
            ret.setErrCode(ErrorCode.SYSTEM_ERROR);
            return ret;
        }
    }

    @Override
    @Transactional
    public ChangeUserInfoOData changeUserInfo(ChangeUserInfoIData input) {
        ChangeUserInfoOData ret = new ChangeUserInfoOData();
        ret.setSuccess(false);

        // Try to acquire lock for this user
        boolean locked = tryLock(input.getUserId());
        if (!locked) {
            ret.setMessage("System is busy, please try again later");
            return ret;
        }

        try {
            // Fetch current user info from master DB
            Optional<User> userOpt = userMasterRepository.findById(input.getUserId());
            if (userOpt.isEmpty()) {
                ret.setMessage("User not found");
                return ret;
            }

            User user = userOpt.get();

            // Verify current password if provided
            if (input.getCurrentPassword() != null && !input.getCurrentPassword().isEmpty()) {
                if (!HashAndCompareUtil.compare(input.getCurrentPassword(), user.getPasswordHash())) {
                    ret.setMessage("Current password is incorrect");
                    return ret;
                }
            }

            // Update user information
            boolean hasChanges = false;

            if (input.getFirstName() != null || input.getLastName() != null) {
                String newFullName = buildFullName(input.getFirstName(), input.getLastName(), user.getFullName());
                if (!newFullName.equals(user.getFullName())) {
                    user.setFullName(newFullName);
                    hasChanges = true;
                }
            }

            // Note: phoneNumber is not in User entity, would need to add it or handle separately
            // For now, we'll skip it as it's not in the current User entity structure

            if (!hasChanges) {
                ret.setMessage("No changes detected");
                ret.setSuccess(true);
                
                // Return current user info
                UserInfoOData currentInfo = new UserInfoOData();
                currentInfo.setUserId(user.getId());
                currentInfo.setEmail(user.getEmail());
                currentInfo.setFullName(user.getFullName());
                currentInfo.setLastUpdate(user.getUpdatedAt());
                currentInfo.setLastLoginAt(user.getLastLoginAt());
                currentInfo.setErrCode(ErrorCode.SUCCESS);
                ret.setUpdatedUserInfo(currentInfo);
                
                return ret;
            }

            // Save changes
            userMasterRepository.save(user);

            // Update cache with new data
            UserInfoCacheData cacheData = UserInfoCacheData.fromUser(user);
            cacheUserInfo(input.getUserId(), cacheData);

            // Prepare response
            ret.setSuccess(true);
            ret.setMessage("User information updated successfully");
            
            UserInfoOData updatedInfo = new UserInfoOData();
            updatedInfo.setUserId(user.getId());
            updatedInfo.setEmail(user.getEmail());
            updatedInfo.setFullName(user.getFullName());
            updatedInfo.setLastUpdate(user.getUpdatedAt());
            updatedInfo.setLastLoginAt(user.getLastLoginAt());
            updatedInfo.setErrCode(ErrorCode.SUCCESS);
            ret.setUpdatedUserInfo(updatedInfo);

        } catch (Exception e) {
            ret.setMessage("System error occurred: " + e.getMessage());
            ret.setSuccess(false);
        } finally {
            // Release lock
            boolean released = releaseLock(input.getUserId());
            if (!released) {
                System.err.println("Failed to release lock for userId: " + input.getUserId());
            }
        }

        return ret;
    }

    /**
     * Build full name from first name and last name
     */
    private String buildFullName(String firstName, String lastName, String currentFullName) {
        if (firstName == null && lastName == null) {
            return currentFullName;
        }

        if (firstName != null && lastName != null) {
            return firstName.trim() + " " + lastName.trim();
        }

        // If only one name is provided, try to parse current full name
        if (currentFullName != null && !currentFullName.trim().isEmpty()) {
            String[] nameParts = currentFullName.trim().split("\\s+");
            if (firstName != null) {
                // Replace first name, keep last name
                if (nameParts.length > 1) {
                    return firstName.trim() + " " + nameParts[nameParts.length - 1];
                } else {
                    return firstName.trim();
                }
            } else {
                // Replace last name, keep first name
                if (nameParts.length > 1) {
                    return nameParts[0] + " " + lastName.trim();
                } else {
                    return lastName.trim();
                }
            }
        }

        // If no current full name, just use what's provided
        return firstName != null ? firstName.trim() : lastName.trim();
    }

    /**
     * Try to acquire distributed lock for user operations
     */
    private boolean tryLock(Long userId) {
        String lockKey = "updateUserInfoLock::" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            return lock.tryLock(1, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Release distributed lock for user operations
     */
    private boolean releaseLock(Long userId) {
        String lockKey = "updateUserInfoLock::" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            return true;
        }

        return false;
    }

    /**
     * Cache user info using manual cache operations
     */
    @CachePut(value = "userInfoCache", key = "#userId")
    private UserInfoCacheData cacheUserInfo(Long userId, UserInfoCacheData cacheData) {
        return cacheData;
    }

    /**
     * Get cached user info
     */
    @Cacheable(value = "userInfoCache", key = "#userId", unless = "#result == null")
    private UserInfoCacheData getCachedUserInfo(Long userId) {
        return null; // Will be populated by cache if exists
    }

    /**
     * Evict user info from cache
     */
    @CacheEvict(value = "userInfoCache", key = "#userId")
    private void evictUserInfoCache(Long userId) {
        // Cache will be evicted automatically
    }
}

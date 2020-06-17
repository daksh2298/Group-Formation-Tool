package com.app.group15.UserManagement.LoginManagement;

import com.app.group15.UserManagement.User;
import com.app.group15.UserManagement.UserDaoMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginServiceTest {

    private UserDaoMock userDaoMock = new UserDaoMock();

    @Test
    public void validateLoginTest() {

        User user = userDaoMock.getUserByBannerIdMock("B00843468");
        assertEquals(user.getFirstName(), "Daksh");

        user = userDaoMock.getUserByBannerIdMock("B00843467");
        assertNull(user.getPassword());
    }
}
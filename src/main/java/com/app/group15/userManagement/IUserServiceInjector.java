package com.app.group15.userManagement;

public interface IUserServiceInjector {
	public void injectUserDao(UserAbstractDao userDao);
	public void injectUserRoleDao( UserRoleAbstractDao userRoleAbstractDao);
}
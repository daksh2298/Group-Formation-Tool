package com.app.group15.userManagement;

import com.app.group15.passwordPolicyManagement.UserPasswordHistory;
import com.app.group15.passwordPolicyManagement.UserPasswordHistoryAbstractDao;
import com.app.group15.persistence.DatabaseManager;
import com.app.group15.persistence.IDao;
import com.app.group15.persistence.Persistence;
import com.app.group15.utility.GroupFormationToolLogger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings("rawtypes")
public class UserDao extends UserAbstractDao implements IUserRoleDaoInjector {

	private UserRoleAbstractDao userRoleDao;
	private UserPasswordHistoryAbstractDao passwordHistoryDao;

	public UserDao() {

	}

	@Override
	public User get(int id) {
		String query = "SELECT * FROM table_users WHERE id=?";
		User user = new User();
		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, id);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					user.setBannerId(result.getString("banner_id"));
					user.setEmail(result.getString("email"));
					user.setFirstName(result.getString("first_name"));
					user.setLastName(result.getString("last_name"));
					user.setId(id);

				}
			}
		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return user;
	}

	@Override
	public User getUserByBannerId(String bannerId) {
		String query = "SELECT * FROM table_users WHERE banner_id=?";
		User user = new User();
		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, bannerId);
			try (ResultSet result = statement.executeQuery()) {
				System.out.println(result);
				while (result.next()) {
					user.setBannerId(result.getString("banner_id"));
					user.setEmail(result.getString("email"));
					user.setFirstName(result.getString("first_name"));
					user.setLastName(result.getString("last_name"));
					user.setId(result.getInt("id"));
					user.setPassword(result.getString("password"));
				}
			}
		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return user;
	}

	@Override
	public ArrayList<User> getAll() {
		String query = "SELECT * FROM table_users";
		ArrayList<User> usersList = new ArrayList<>();
		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query);
				ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				User user = new User();
				user.setBannerId(result.getString("banner_id"));
				user.setEmail(result.getString("email"));
				user.setFirstName(result.getString("first_name"));
				user.setLastName(result.getString("last_name"));
				user.setId(result.getInt("id"));
				usersList.add(user);
			}

		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return usersList;
	}

	@Override
	public int saveUser(User user, String role) {
		String query = "INSERT INTO table_users(first_name,last_name,email, banner_id,password) " + "VALUES(?,?,?,?,?)";
		int userId = 0;
		try (Connection connection = DatabaseManager.getDataSource().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
				connection.setAutoCommit(false);
				statement.setString(1, user.getFirstName());
				statement.setString(2, user.getLastName());
				statement.setString(3, user.getEmail());
				statement.setString(4, user.getBannerId());
				statement.setString(5, user.getPassword());
				statement.executeUpdate();
				connection.commit();
				try (ResultSet result = statement.getGeneratedKeys()) {

					if (result.first()) {

						userId = result.getInt(1);
					}
				}
				userRoleDao.addRole(userId, role);

			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException e1) {
					GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
				}
				GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
			}

		} catch (SQLException e) {

			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return userId;

	}

	@Override
	public void update(Persistence user, int id) {
		User userEntity = (User) user;
		String query = "UPDATE table_users SET first_name=?,last_name=?,email=? WHERE id=?";
		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {
			connection.setAutoCommit(false);
			statement.setString(1, userEntity.getFirstName());
			statement.setString(2, userEntity.getLastName());
			statement.setString(3, userEntity.getEmail());
			statement.setInt(4, id);
			statement.executeUpdate();
			connection.commit();
		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	@Override
	public void updateUserRole(int userId, String role) {
		String query = "UPDATE table_user_role_mapper SET role_id=? WHERE user_id=?";
		try (Connection connection = DatabaseManager.getDataSource().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				connection.setAutoCommit(false);
				statement.setInt(1, userRoleDao.getRoleId(role));
				statement.setInt(2, userId);
				statement.executeUpdate();
				connection.commit();

			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException e1) {
					GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
				}
				GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
			}
		} catch (SQLException e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	private String getUserPassword(int userId) {
		String query = "SELECT password from table_users  WHERE id=?";
		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query);) {
			statement.setInt(1, userId);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				return result.getString("password");
			}

		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;

	}

	@Override
	public boolean updateUserPassword(int userId, String password) {
		String currentPassword = getUserPassword(userId);
		String query = "UPDATE table_users SET password=? WHERE id=?";
		try (Connection connection = DatabaseManager.getDataSource().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				connection.setAutoCommit(false);
				statement.setString(1, password);
				statement.setInt(2, userId);
				statement.executeUpdate();
				connection.commit();
				passwordHistoryDao.savePasswordHistory(createPasswordHistory(currentPassword, userId));
				return true;
			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException e1) {
					GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
				}

			}
		} catch (SQLException e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return false;

	}

	private UserPasswordHistory createPasswordHistory(String password, int userId) {
		UserPasswordHistory passwordHistory = new UserPasswordHistory();
		passwordHistory.setHistoryPassword(password);
		passwordHistory.setUserId(userId);
		return passwordHistory;

	}

	@Override
	public void injectUserRoleDao(IDao userRoleDao) {
		this.userRoleDao = (UserRoleDao) userRoleDao;
	}

	@Override
	public User getUserByEmailId(String emailId) {
		String query = "SELECT * FROM table_users WHERE email like ?";
		User user = new User();
		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, emailId);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					user.setBannerId(result.getString("banner_id"));
					user.setEmail(result.getString("email"));
					user.setFirstName(result.getString("first_name"));
					user.setLastName(result.getString("last_name"));
					user.setId(result.getInt("id"));
				}
			}
		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return user;
	}

	@Override
	public boolean insertForgotPasswordToken(int id, String token) {
		String query = "INSERT INTO table_forgot_password_tokens(id,token,token_generation_date_time) "
				+ "VALUES(?,?,?)";
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = sdf.format(date);
		try (Connection connection = DatabaseManager.getDataSource().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
				connection.setAutoCommit(false);
				statement.setInt(1, id);
				statement.setString(2, token);
				statement.setString(3, currentDateTime);
				statement.executeUpdate();
				connection.commit();
			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException e1) {
					GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
				}
				GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
			}

		} catch (SQLException e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return true;

	}

	@Override
	public boolean checkIfTokenAlreadyExists(int id) {
		boolean exist = false;
		String query = "SELECT id FROM table_forgot_password_tokens WHERE id=?";

		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setInt(1, id);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					if (id == result.getInt("id")) {
						exist = true;
					}
				}
			}
		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return exist;
	}

	@Override
	public boolean deleteForgotPasswordToken(int id) {
		String query = "delete from table_forgot_password_tokens where id=?";
		try (Connection connection = DatabaseManager.getDataSource().getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
				connection.setAutoCommit(false);
				statement.setInt(1, id);
				statement.executeUpdate();
				connection.commit();
				return true;
			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException e1) {
					GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
				}
				GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
			}
		} catch (SQLException e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}

		return false;
	}

	@Override
	public Map<String, String> getUserFromToken(String token) {
		String query = "SELECT * FROM table_forgot_password_tokens WHERE token like ?";
		HashMap<String, String> row = new HashMap<>();
		try (Connection connection = DatabaseManager.getDataSource().getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, token);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					row.put("dateTime", result.getString("token_generation_date_time"));
					row.put("id", String.valueOf(result.getInt("id")));
				}
			}
		} catch (Exception e) {
			GroupFormationToolLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		return row;
	}

	@Override
	public void injectPasswordHistoryDao(UserPasswordHistoryAbstractDao passwordHistoryDao) {
		this.passwordHistoryDao = passwordHistoryDao;

	}

}
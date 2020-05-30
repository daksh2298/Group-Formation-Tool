package com.app.group15.injectors;

import com.app.group15.config.AppConfig;
import com.app.group15.dao.CourseDao;
import com.app.group15.persistence.DatabaseManager;

public class CourseDaoInjectorService {
	
	private CourseDao courseDao;

	public CourseDaoInjectorService() {
		courseDao=new CourseDao();
		courseDao.injectConnection(DatabaseManager.getConnection());
		courseDao.injectCourseInstructorMapperDao(AppConfig.instance().getCourseInstructorMapperDaoInjectorService().getCourseInstructorMapperDao());
		courseDao.injectCourseStudentMapperDao(AppConfig.instance().getCourseStudentMapperDaoInjectorService().getCourseStudentMapperDao());

	}

	public CourseDao getCourseDao() {
		return courseDao;
	}
}
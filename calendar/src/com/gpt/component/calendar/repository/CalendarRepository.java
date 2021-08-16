package com.gpt.component.calendar.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.calendar.model.CalendarModel;
import com.gpt.platform.cash.repository.CashRepository;

/**
 * We handle caching manually here because the query result of null must also be cached to reduce query of non existent date to db
 *
 */
@Repository
public interface CalendarRepository extends JpaRepository<CalendarModel, Date>, CashRepository<CalendarModel>{
	
	@Query("from CalendarModel a where a.holidayDate between ?1 and ?2 order by a.holidayDate")
	List<CalendarModel> findByHolidayDateBetween(Date startHolidayDate, Date endHolidayDate);

	@Override
	@Cacheable(cacheNames = "HolidayCalendar", key = "#p0")
	CalendarModel findOne(Date id);
	
	@Query("from CalendarModel a where a.holidayDate = ?1 and a.type = ?2")
	List<CalendarModel> findByHolidayDate(Date holidayDate, String type);
	
	@Override
	@CacheEvict(value = "HolidayCalendar", key = "#p0.holidayDate")
	<S extends CalendarModel> S save(S entity);

	@Override
	@CacheEvict(value = "HolidayCalendar", key = "#p0.holidayDate")
	<S extends CalendarModel> S persist(S entity);
	
	@CacheEvict(value = "HolidayCalendar", key = "#p0")
	@Modifying
	@Query("delete from CalendarModel where holidayDate = ?1")
	void deleteByHolidayDate(Date id);
	
}
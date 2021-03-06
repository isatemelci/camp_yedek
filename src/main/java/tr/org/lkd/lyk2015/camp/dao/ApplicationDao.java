package tr.org.lkd.lyk2015.camp.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import tr.org.lkd.lyk2015.camp.models.Application;

@Repository
public class ApplicationDao extends GenericDao<Application> {

	public Application getByUuid(String uuid) {

		Criteria criteria = this.createCriteria();

		criteria.add(Restrictions.eq("validationId", uuid));

		return (Application) criteria.uniqueResult();

	}

}

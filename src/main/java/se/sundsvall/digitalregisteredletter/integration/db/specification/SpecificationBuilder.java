package se.sundsvall.digitalregisteredletter.integration.db.specification;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;

import static java.util.Objects.nonNull;

public class SpecificationBuilder<T> {

	/**
	 * Method builds an equal ignore case filter if value is not null. If value is null, method returns an always-true
	 * predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  nestedAttribute name of the nested attribute that will be used in filter
	 * @param  attribute       name that will be used in filter
	 * @param  value           value (or null) to compare against
	 * @return                 Specification<T> matching sent in comparison
	 */
	public Specification<T> buildEqualsIgnoreCaseFilter(final String nestedAttribute, final String attribute, final String value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.equal(cb.lower(entity.get(nestedAttribute).get(attribute)), value.toLowerCase()) : cb.and();
	}

	/**
	 * Method builds an equal filter if value is not null. If value is null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  nestedAttribute name of the nested attribute that will be used in filter
	 * @param  attribute       name that will be used in filter
	 * @param  value           value (or null) to compare against
	 * @return                 Specification<T> matching sent in comparison
	 */
	public Specification<T> buildEqualFilter(final String nestedAttribute, String attribute, Object value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.equal(entity.get(nestedAttribute).get(attribute), value) : cb.and();
	}

	/**
	 * Method builds an equal filter if value is not null. If value is null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name that will be used in filter
	 * @param  value     value (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildEqualFilter(String attribute, Object value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.equal(entity.get(attribute), value) : cb.and();
	}

	/**
	 * Method builds a filter depending on sent in time stamp. If value is null, method returns an always-true
	 * predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name that will be used in filter
	 * @param  value     value (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildDateIsEqualOrAfterFilter(final String attribute, final OffsetDateTime value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.greaterThanOrEqualTo(entity.get(attribute), value) : cb.and();
	}

	/**
	 * Method builds a filter depending on sent in time stamp. If value is null, method returns an always-true
	 * predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name that will be used in filter
	 * @param  value     value (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildDateIsEqualOrBeforeFilter(final String attribute, final OffsetDateTime value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.lessThanOrEqualTo(entity.get(attribute), value) : cb.and();
	}
}

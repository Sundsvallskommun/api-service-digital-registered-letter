package se.sundsvall.digitalregisteredletter.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SigningInformationEntityTest {

	@BeforeAll
	static void setup() {
		final var random = new Random();
		registerValueGenerator(() -> now().plusDays(random.nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(SigningInformationEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var contentKey = "contentKey";
		final var givenName = "givenName";
		final var id = "id";
		final var ipAddress = "ipAddress";
		final var mrtd = true;
		final var name = "name";
		final var ocspResponse = "ocspResponse";
		final var orderRef = "orderRef";
		final var personalNumber = "personalNumber";
		final var internalId = "internalId";
		final var signature = "signature";
		final var signed = OffsetDateTime.now();
		final var status = "status";
		final var surname = "surname";

		final var bean = SigningInformationEntity.create()
			.withContentKey(contentKey)
			.withGivenName(givenName)
			.withId(id)
			.withIpAddress(ipAddress)
			.withMrtd(mrtd)
			.withName(name)
			.withOcspResponse(ocspResponse)
			.withOrderRef(orderRef)
			.withPersonalNumber(personalNumber)
			.withInternalId(internalId)
			.withSignature(signature)
			.withSigned(signed)
			.withStatus(status)
			.withSurname(surname);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getContentKey()).isEqualTo(contentKey);
		assertThat(bean.getGivenName()).isEqualTo(givenName);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getIpAddress()).isEqualTo(ipAddress);
		assertThat(bean.getMrtd()).isEqualTo(mrtd);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getOcspResponse()).isEqualTo(ocspResponse);
		assertThat(bean.getOrderRef()).isEqualTo(orderRef);
		assertThat(bean.getPersonalNumber()).isEqualTo(personalNumber);
		assertThat(bean.getInternalId()).isEqualTo(internalId);
		assertThat(bean.getSignature()).isEqualTo(signature);
		assertThat(bean.getSigned()).isEqualTo(signed);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getSurname()).isEqualTo(surname);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SigningInformationEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new SigningInformationEntity()).hasAllNullFieldsOrProperties();
	}
}

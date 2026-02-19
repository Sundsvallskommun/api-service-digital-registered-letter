package se.sundsvall.digitalregisteredletter.integration.db.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.requestid.RequestId;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.core.AllOf.allOf;
import static se.sundsvall.TestDataFactory.createAttachmentEntity;
import static se.sundsvall.TestUtil.isValidUUID;

class LetterEntityTest {

	@BeforeAll
	static void setup() {
		RequestId.init();

		final var random = new Random();
		registerValueGenerator(() -> now().plusDays(random.nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(LetterEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSettersExcluding("requestId"),
			hasValidBeanHashCodeExcluding("organization", "tenant", "user", "requestId"),
			hasValidBeanEqualsExcluding("organization", "tenant", "user", "requestId"),
			hasValidBeanToStringExcluding("organization", "tenant", "user", "requestId")));
	}

	@Test
	void testBuilderMethods() {

		final var offsetDateTime = OffsetDateTime.of(2025, 6, 18, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
		final var id = "id";
		final var municipalityId = "1234";
		final var created = offsetDateTime.minusDays(1);
		final var updated = offsetDateTime.minusHours(1);
		final var body = "body";
		final var subject = "subject";
		final var partyId = "partyId";
		final var user = UserEntity.create();
		final var organization = OrganizationEntity.create();
		final var signingInformation = SigningInformationEntity.create();
		final var tenant = TenantEntity.create();
		final var contentType = "text/plain";
		final var status = "status";
		final var deleted = true;
		final var attachments = List.of(createAttachmentEntity());
		final var supportInformation = SupportInformation.create()
			.withSupportText("support text")
			.withContactInformationUrl("https://example.com/contact")
			.withContactInformationEmail("support@email.com")
			.withContactInformationPhoneNumber("+46123456789");

		final var letterEntity = LetterEntity.create()
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withCreated(created)
			.withUpdated(updated)
			.withBody(body)
			.withContentType(contentType)
			.withStatus(status)
			.withDeleted(deleted)
			.withAttachments(attachments)
			.withPartyId(partyId)
			.withUser(user)
			.withOrganization(organization)
			.withSigningInformation(signingInformation)
			.withTenant(tenant)
			.withSubject(subject)
			.withSupportInformation(supportInformation);

		assertThat(letterEntity.getAttachments()).isEqualTo(attachments);
		assertThat(letterEntity.getId()).isEqualTo(id);
		assertThat(letterEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(letterEntity.getCreated()).isEqualTo(created);
		assertThat(letterEntity.getUpdated()).isEqualTo(updated);
		assertThat(letterEntity.getBody()).isEqualTo(body);
		assertThat(letterEntity.getContentType()).isEqualTo(contentType);
		assertThat(letterEntity.getStatus()).isEqualTo(status);
		assertThat(letterEntity.isDeleted()).isEqualTo(deleted);
		assertThat(letterEntity.getSupportInformation()).isEqualTo(supportInformation);
		assertThat(letterEntity.getPartyId()).isEqualTo(partyId);
		assertThat(letterEntity.getUser()).isEqualTo(user);
		assertThat(letterEntity.getOrganization()).isEqualTo(organization);
		assertThat(letterEntity.getSigningInformation()).isEqualTo(signingInformation);
		assertThat(letterEntity.getTenant()).isEqualTo(tenant);
		assertThat(letterEntity.getSubject()).isEqualTo(subject);

		assertThat(letterEntity).hasNoNullFieldsOrPropertiesExcept("requestId");
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(LetterEntity.create()).hasAllNullFieldsOrPropertiesExcept("attachments", "deleted");
		assertThat(new LetterEntity()).hasAllNullFieldsOrPropertiesExcept("attachments", "deleted");
	}

	@Test
	void testOnPersist() {
		final var letterEntity = LetterEntity.create();

		assertThat(letterEntity).hasAllNullFieldsOrPropertiesExcept("deleted", "attachments");
		assertThat(letterEntity.isDeleted()).isFalse();
		assertThat(letterEntity.getAttachments()).isEmpty();

		letterEntity.onPersist();

		assertThat(letterEntity.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(letterEntity.getUpdated()).isEqualTo(letterEntity.getCreated());
		assertThat(isValidUUID(letterEntity.getRequestId())).isTrue();
	}

	@Test
	void testOnUpdate() {
		final var letterEntity = LetterEntity.create();

		assertThat(letterEntity).hasAllNullFieldsOrPropertiesExcept("deleted", "attachments");
		assertThat(letterEntity.isDeleted()).isFalse();
		assertThat(letterEntity.getAttachments()).isEmpty();

		letterEntity.onUpdate();

		assertThat(letterEntity.getCreated()).isNull();
		assertThat(letterEntity.getUpdated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(isValidUUID(letterEntity.getRequestId())).isTrue();
	}
}

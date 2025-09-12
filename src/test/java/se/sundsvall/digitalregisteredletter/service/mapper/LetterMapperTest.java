package se.sundsvall.digitalregisteredletter.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static se.sundsvall.TestDataFactory.createAttachmentEntity;
import static se.sundsvall.TestDataFactory.createLetterEntity;
import static se.sundsvall.TestDataFactory.createLetterRequest;
import static se.sundsvall.TestDataFactory.createSupportInfoEmbeddable;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.digitalregisteredletter.api.model.Letter.Attachment;
import se.sundsvall.digitalregisteredletter.api.model.OrganizationBuilder;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SupportInfo;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;

class LetterMapperTest {

	@Test
	void toLetterEntity() {
		final var letterRequest = createLetterRequest();

		final var result = LetterMapper.toLetterEntity(letterRequest);

		assertThat(result.getBody()).isEqualTo(letterRequest.body());
		assertThat(result.getContentType()).isEqualTo(letterRequest.contentType());
		assertThat(result.getSupportInfo()).satisfies(supportInfo -> {
			assertThat(supportInfo.getSupportText()).isEqualTo(letterRequest.supportInfo().supportText());
			assertThat(supportInfo.getContactInformationEmail()).isEqualTo(letterRequest.supportInfo().contactInformationEmail());
			assertThat(supportInfo.getContactInformationUrl()).isEqualTo(letterRequest.supportInfo().contactInformationUrl());
			assertThat(supportInfo.getContactInformationPhoneNumber()).isEqualTo(letterRequest.supportInfo().contactInformationPhoneNumber());
		});

	}

	@Test
	void toLetterEntityFromNull() {
		assertThat(LetterMapper.toLetterEntity(null)).isNull();
	}

	@Test
	void toSupportInfoEmbeddable() {
		final var supportInfo = SupportInfoBuilder.create()
			.withSupportText("supportText")
			.withContactInformationEmail("supportEmail")
			.withContactInformationUrl("supportUrl")
			.withContactInformationPhoneNumber("supportPhone")
			.build();

		final var result = LetterMapper.toSupportInfo(supportInfo);

		assertThat(result.getSupportText()).isEqualTo(supportInfo.supportText());
		assertThat(result.getContactInformationEmail()).isEqualTo(supportInfo.contactInformationEmail());
		assertThat(result.getContactInformationUrl()).isEqualTo(supportInfo.contactInformationUrl());
		assertThat(result.getContactInformationPhoneNumber()).isEqualTo(supportInfo.contactInformationPhoneNumber());
	}

	@Test
	void toSupportInfoEmbeddableFromNull() {
		assertThat(LetterMapper.toSupportInfo((se.sundsvall.digitalregisteredletter.api.model.SupportInfo) null)).isNull();
	}

	@Test
	void toOrganizationEntity() {
		final var name = "name";
		final var number = 911;
		final var letterEntity = LetterEntity.create();
		final var organization = OrganizationBuilder.create()
			.withName(name)
			.withNumber(number)
			.build();

		final var bean = LetterMapper.toOrganizationEntity(organization, letterEntity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getLetters()).containsExactly(letterEntity);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNumber()).isEqualTo(number);
	}

	@Test
	void toOrganizationEntityWithNullLetter() {
		final var name = "name";
		final var number = 911;
		final var organization = OrganizationBuilder.create()
			.withName(name)
			.withNumber(number)
			.build();

		final var bean = LetterMapper.toOrganizationEntity(organization, null);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getLetters()).isEmpty();
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNumber()).isEqualTo(number);
	}

	@Test
	void toOrganizationEntityFromNull() {
		assertThat(LetterMapper.toOrganizationEntity(null, null)).isNull();
		assertThat(LetterMapper.toOrganizationEntity(null, LetterEntity.create())).isNull();
	}

	@Test
	void toUserEntity() {
		final var letterEntity = LetterEntity.create();
		final var username = "username";

		final var bean = LetterMapper.toUserEntity(username, letterEntity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getUsername()).isEqualTo(username);
		assertThat(bean.getLetters()).containsExactly(letterEntity);
	}

	@Test
	void toUserEntityWithNullLetterList() {
		final var username = "username";

		final var bean = LetterMapper.toUserEntity(username, null);

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getUsername()).isEqualTo(username);
		assertThat(bean.getLetters()).isEmpty();
	}

	@Test
	void toUserEntityFromNull() {
		assertThat(LetterMapper.toUserEntity(null, null)).isNull();
		assertThat(LetterMapper.toUserEntity(null, LetterEntity.create())).isNull();
	}

	@Test
	void addLetterToOrganizationEntity() {
		final var oldEntity = LetterEntity.create();
		final var newEntity = LetterEntity.create();
		final var organizationEntity = OrganizationEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = LetterMapper.addLetter(organizationEntity, newEntity);

		assertThat(bean).isSameAs(organizationEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity, newEntity);
	}

	@Test
	void addNullToOrganizationEntityLetterList() {
		final var oldEntity = LetterEntity.create();
		final var organizationEntity = OrganizationEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = LetterMapper.addLetter(organizationEntity, null);

		assertThat(bean).isSameAs(organizationEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity);
	}

	@Test
	void addLetterToOrganizationEntityWithNullList() {
		final var newEntity = LetterEntity.create();
		final var organizationEntity = OrganizationEntity.create()
			.withLetters(null);

		final var bean = LetterMapper.addLetter(organizationEntity, newEntity);

		assertThat(bean).isSameAs(organizationEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(newEntity);
	}

	@Test
	void addLetterToUserEntity() {
		final var oldEntity = LetterEntity.create();
		final var newEntity = LetterEntity.create();
		final var userEntity = UserEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = LetterMapper.addLetter(userEntity, newEntity);

		assertThat(bean).isSameAs(userEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity, newEntity);
	}

	@Test
	void addNullToUserEntityLetterList() {
		final var oldEntity = LetterEntity.create();
		final var userEntity = UserEntity.create()
			.withLetters(new ArrayList<>(List.of(oldEntity)));

		final var bean = LetterMapper.addLetter(userEntity, null);

		assertThat(bean).isSameAs(userEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(oldEntity);
	}

	@Test
	void addLetterToUserEntityWithNullList() {
		final var newEntity = LetterEntity.create();
		final var userEntity = UserEntity.create()
			.withLetters(null);

		final var bean = LetterMapper.addLetter(userEntity, newEntity);

		assertThat(bean).isSameAs(userEntity);
		assertThat(bean.getLetters()).containsExactlyInAnyOrder(newEntity);
	}

	@Test
	void toLetter() {
		final var entity = createLetterEntity();

		final var bean = LetterMapper.toLetter(entity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.body()).isEqualTo("This is the body of the letter");
		assertThat(bean.contentType()).isEqualTo("text/plain");
		assertThat(bean.created()).isEqualTo(entity.getCreated());
		assertThat(bean.id()).isEqualTo("letter-id");
		assertThat(bean.municipalityId()).isEqualTo("municipality-id");
		assertThat(bean.status()).isEqualTo("NEW");
		assertThat(bean.updated()).isEqualTo(entity.getUpdated());
		assertThat(bean.supportInfo()).isNotNull()
			.extracting(
				se.sundsvall.digitalregisteredletter.api.model.SupportInfo::contactInformationEmail,
				se.sundsvall.digitalregisteredletter.api.model.SupportInfo::contactInformationPhoneNumber,
				se.sundsvall.digitalregisteredletter.api.model.SupportInfo::contactInformationUrl)
			.containsExactly(
				"support@email.com",
				"+46123456789",
				"https://example.com/support");
		assertThat(bean.attachments()).hasSize(1)
			.extracting(
				Attachment::contentType,
				Attachment::fileName,
				Attachment::id)
			.containsExactly(tuple(
				"text/plain",
				"file.txt",
				"attachment-id"));
	}

	@Test
	void toLetterFromNull() {
		assertThat(LetterMapper.toLetter(null)).isNull();
	}

	@Test
	void toLetters() {
		final var entity = createLetterEntity();
		final var letter = LetterMapper.toLetter(entity);

		final var list = LetterMapper.toLetters(List.of(entity));

		assertThat(list).hasSize(1).satisfiesExactly(l -> {
			assertThat(l).usingRecursiveAssertion().isEqualTo(letter);
		});
	}

	@Test
	void toLettersWithNullEntry() {
		final var entity = createLetterEntity();
		final var listWithNull = new ArrayList<>(List.of(entity));
		listWithNull.addFirst(null);

		assertThat(LetterMapper.toLetters(listWithNull)).hasSize(1);
	}

	@Test
	void toLettersFromNull() {
		assertThat(LetterMapper.toLetters(null)).isEmpty();
	}

	@Test
	void toSupportInfo() {
		final var embeddable = createSupportInfoEmbeddable();

		final var bean = LetterMapper.toSupportInfo(embeddable);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.contactInformationEmail()).isEqualTo("support@email.com");
		assertThat(bean.contactInformationPhoneNumber()).isEqualTo("+46123456789");
		assertThat(bean.contactInformationUrl()).isEqualTo("https://example.com/support");
	}

	@Test
	void toSupportInfoFromNull() {
		assertThat(LetterMapper.toSupportInfo((SupportInfo) null)).isNull();
	}

	@Test
	void toLetterAttachments() {
		final var entity = createAttachmentEntity();
		final var attachment = LetterMapper.toLetterAttachment(entity);

		final var list = LetterMapper.toLetterAttachments(List.of(entity));

		assertThat(list).hasSize(1).satisfiesExactly(l -> {
			assertThat(l).usingRecursiveAssertion().isEqualTo(attachment);
		});
	}

	@Test
	void toLetterAttachmentsWithNullEntry() {
		final var entity = createAttachmentEntity();
		final var listWithNull = new ArrayList<>(List.of(entity));
		listWithNull.addFirst(null);

		assertThat(LetterMapper.toLetterAttachments(listWithNull)).hasSize(1);
	}

	@Test
	void toLetterAttachmentsFromNull() {
		assertThat(LetterMapper.toLetterAttachments(null)).isEmpty();
	}

	@Test
	void toLetterAttachment() {
		final var entity = createAttachmentEntity();

		final var bean = LetterMapper.toLetterAttachment(entity);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.contentType()).isEqualTo("text/plain");
		assertThat(bean.fileName()).isEqualTo("file.txt");
		assertThat(bean.id()).isEqualTo("attachment-id");
	}

	@Test
	void toLetterAttachmentFromNull() {
		assertThat(LetterMapper.toLetterAttachment(null)).isNull();
	}
}

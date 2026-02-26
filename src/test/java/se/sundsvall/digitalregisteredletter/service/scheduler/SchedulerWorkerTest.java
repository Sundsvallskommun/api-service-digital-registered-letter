package se.sundsvall.digitalregisteredletter.service.scheduler;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValueBuilder;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponseBuilder;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static se.sundsvall.TestDataFactory.createLetterEntity;
import static se.sundsvall.digitalregisteredletter.Constants.STATUS_PENDING;

@ExtendWith(MockitoExtension.class)
class SchedulerWorkerTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORG_NUMBER = "5591628136";

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private LetterRepository letterRepositoryMock;

	@Mock
	private LetterMapper letterMapperMock;

	@InjectMocks
	private SchedulerWorker schedulerWorker;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(kivraIntegrationMock, letterRepositoryMock, letterMapperMock);
	}

	@Test
	void updateLetterInformation() {
		final var tenant = TenantEntity.create().withMunicipalityId(MUNICIPALITY_ID).withOrgNumber(ORG_NUMBER);
		final var existingSigningInformation = SigningInformationEntity.create();
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").withStatus("status1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").withStatus("status2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter1 = createLetterEntity().withId("letterId1");
		final var letter2 = createLetterEntity().withId("letterId2").withSigningInformation(existingSigningInformation);
		final var sentLetter = createLetterEntity().withTenant(tenant);
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(letterRepositoryMock.findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING)).thenReturn(List.of(sentLetter));
		when(kivraIntegrationMock.getAllResponses(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse(letter1.getId(), MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse(letter2.getId(), MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));

		schedulerWorker.updateLetterInformation();

		verify(letterRepositoryMock).findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING);
		verify(kivraIntegrationMock).getAllResponses(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter1.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter2.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse(letter1.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).deleteResponse(letter2.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterMapperMock).updateLetterStatus(letter1, status);
		verify(letterMapperMock).updateLetterStatus(letter2, status);
		verify(letterMapperMock).updateSigningInformation(letter1.getSigningInformation(), registeredLetterResponse1);
		verify(letterMapperMock).updateSigningInformation(letter2.getSigningInformation(), registeredLetterResponse2);
	}

	@Test
	void updateLetterInformationWithMultipleTenants() {
		final var tenant1 = TenantEntity.create().withMunicipalityId(MUNICIPALITY_ID).withOrgNumber(ORG_NUMBER);
		final var tenant2 = TenantEntity.create().withMunicipalityId("2262").withOrgNumber("1234567890");
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").build();
		final var letter1 = createLetterEntity().withId("letterId1");
		final var letter2 = createLetterEntity().withId("letterId2");
		final var sentLetter1 = createLetterEntity().withTenant(tenant1);
		final var sentLetter2 = createLetterEntity().withTenant(tenant2);
		final var status = "signed";
		final var response1 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var response2 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(letterRepositoryMock.findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING)).thenReturn(List.of(sentLetter1, sentLetter2));
		when(kivraIntegrationMock.getAllResponses(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(List.of(keyValue1));
		when(kivraIntegrationMock.getAllResponses("2262", "1234567890")).thenReturn(List.of(keyValue2));
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1", MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(response1);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId2", "2262", "1234567890")).thenReturn(response2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));

		schedulerWorker.updateLetterInformation();

		verify(letterRepositoryMock).findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING);
		verify(kivraIntegrationMock).getAllResponses(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getAllResponses("2262", "1234567890");
		verify(kivraIntegrationMock).getRegisteredLetterResponse("letterId1", MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse("letterId2", "2262", "1234567890");
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse("letterId1", MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).deleteResponse("letterId2", "2262", "1234567890");
		verify(letterMapperMock).updateLetterStatus(letter1, status);
		verify(letterMapperMock).updateLetterStatus(letter2, status);
		verify(letterMapperMock).updateSigningInformation(letter1.getSigningInformation(), response1);
		verify(letterMapperMock).updateSigningInformation(letter2.getSigningInformation(), response2);
	}

	/*
	 * Tests below verifies that loop is not interrupted by a single entity update failure
	 */

	@Test
	void updateLetterInformationWhenTenantProcessingThrowsException() {
		final var tenant1 = TenantEntity.create().withMunicipalityId(MUNICIPALITY_ID).withOrgNumber(ORG_NUMBER);
		final var tenant2 = TenantEntity.create().withMunicipalityId("2262").withOrgNumber("1234567890");
		final var keyValue = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var letter = createLetterEntity().withId("letterId1");
		final var sentLetter1 = createLetterEntity().withTenant(tenant1);
		final var sentLetter2 = createLetterEntity().withTenant(tenant2);
		final var status = "signed";
		final var response = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter.getId())).build();

		when(letterRepositoryMock.findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING)).thenReturn(List.of(sentLetter1, sentLetter2));
		when(kivraIntegrationMock.getAllResponses(MUNICIPALITY_ID, ORG_NUMBER)).thenThrow(Problem.valueOf(I_AM_A_TEAPOT, "Test exception"));
		when(kivraIntegrationMock.getAllResponses("2262", "1234567890")).thenReturn(List.of(keyValue));
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1", "2262", "1234567890")).thenReturn(response);
		when(letterRepositoryMock.findByIdAndDeleted(letter.getId(), false)).thenReturn(Optional.of(letter));

		schedulerWorker.updateLetterInformation();

		verify(letterRepositoryMock).findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING);
		verify(kivraIntegrationMock).getAllResponses(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getAllResponses("2262", "1234567890");
		verify(kivraIntegrationMock).getRegisteredLetterResponse("letterId1", "2262", "1234567890");
		verify(letterRepositoryMock).findByIdAndDeleted(letter.getId(), false);
		verify(letterRepositoryMock).save(letter);
		verify(kivraIntegrationMock).deleteResponse("letterId1", "2262", "1234567890");
		verify(letterMapperMock).updateLetterStatus(letter, status);
		verify(letterMapperMock).updateSigningInformation(letter.getSigningInformation(), response);
	}

	@Test
	void updateLetterInformationWhenUnknownExceptionThrown() {
		final var tenant = TenantEntity.create().withMunicipalityId(MUNICIPALITY_ID).withOrgNumber(ORG_NUMBER);
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter = createLetterEntity().withId("letterId2");
		final var sentLetter = createLetterEntity().withTenant(tenant);
		final var status = "signed";
		final var registeredLetterResponse = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter.getId())).build();

		when(letterRepositoryMock.findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING)).thenReturn(List.of(sentLetter));
		when(kivraIntegrationMock.getAllResponses(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1", MUNICIPALITY_ID, ORG_NUMBER)).thenThrow(Problem.valueOf(I_AM_A_TEAPOT, "Test exception"));
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId2", MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(registeredLetterResponse);
		when(letterRepositoryMock.findByIdAndDeleted(letter.getId(), false)).thenReturn(Optional.of(letter));

		schedulerWorker.updateLetterInformation();

		verify(letterRepositoryMock).findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING);
		verify(kivraIntegrationMock).getAllResponses(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse("letterId1", MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse("letterId2", MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterRepositoryMock).findByIdAndDeleted(letter.getId(), false);
		verify(letterRepositoryMock).save(letter);
		verify(kivraIntegrationMock).deleteResponse("letterId2", MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterMapperMock).updateLetterStatus(letter, status);
		verify(letterMapperMock).updateSigningInformation(letter.getSigningInformation(), registeredLetterResponse);
	}

	@Test
	void updateLetterInformationWhenRepositorySaveThrowsException() {
		final var tenant = TenantEntity.create().withMunicipalityId(MUNICIPALITY_ID).withOrgNumber(ORG_NUMBER);
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter1 = createLetterEntity().withId("letterId1");
		final var letter2 = createLetterEntity().withId("letterId2");
		final var sentLetter = createLetterEntity().withTenant(tenant);
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(letterRepositoryMock.findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING)).thenReturn(List.of(sentLetter));
		when(kivraIntegrationMock.getAllResponses(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1", MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId2", MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));
		when(letterRepositoryMock.save(letter1)).thenThrow(Problem.valueOf(I_AM_A_TEAPOT, "Test exception"));
		when(letterRepositoryMock.save(letter2)).thenReturn(letter2);

		schedulerWorker.updateLetterInformation();

		verify(letterRepositoryMock).findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING);
		verify(kivraIntegrationMock).getAllResponses(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter1.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter2.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse(letter2.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterMapperMock).updateLetterStatus(letter1, status);
		verify(letterMapperMock).updateLetterStatus(letter2, status);
		verify(letterMapperMock).updateSigningInformation(letter1.getSigningInformation(), registeredLetterResponse1);
		verify(letterMapperMock).updateSigningInformation(letter2.getSigningInformation(), registeredLetterResponse2);
	}

	@Test
	void updateLetterInformationKivraRemovalThrowsException() {
		final var tenant = TenantEntity.create().withMunicipalityId(MUNICIPALITY_ID).withOrgNumber(ORG_NUMBER);
		final var keyValue1 = KeyValueBuilder.create().withResponseKey("letterId1").build();
		final var keyValue2 = KeyValueBuilder.create().withResponseKey("letterId2").build();
		final var keyValues = List.of(keyValue1, keyValue2);
		final var letter1 = createLetterEntity().withId("letterId1");
		final var letter2 = createLetterEntity().withId("letterId2");
		final var sentLetter = createLetterEntity().withTenant(tenant);
		final var status = "signed";
		final var registeredLetterResponse1 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter1.getId())).build();
		final var registeredLetterResponse2 = RegisteredLetterResponseBuilder.create().withStatus(status).withSenderReference(new RegisteredLetterResponse.SenderReference(letter2.getId())).build();

		when(letterRepositoryMock.findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING)).thenReturn(List.of(sentLetter));
		when(kivraIntegrationMock.getAllResponses(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(keyValues);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId1", MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(registeredLetterResponse1);
		when(kivraIntegrationMock.getRegisteredLetterResponse("letterId2", MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(registeredLetterResponse2);
		when(letterRepositoryMock.findByIdAndDeleted(letter1.getId(), false)).thenReturn(Optional.of(letter1));
		when(letterRepositoryMock.findByIdAndDeleted(letter2.getId(), false)).thenReturn(Optional.of(letter2));
		when(letterRepositoryMock.save(letter1)).thenReturn(letter2);
		when(letterRepositoryMock.save(letter2)).thenReturn(letter2);
		doThrow(Problem.valueOf(I_AM_A_TEAPOT, "Test exception")).when(kivraIntegrationMock).deleteResponse(letter1.getId(), MUNICIPALITY_ID, ORG_NUMBER);

		schedulerWorker.updateLetterInformation();

		verify(letterRepositoryMock).findBySigningInformationStatusAndDeletedFalseAndTenantIsNotNull(STATUS_PENDING);
		verify(kivraIntegrationMock).getAllResponses(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter1.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).getRegisteredLetterResponse(letter2.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterRepositoryMock).findByIdAndDeleted(letter1.getId(), false);
		verify(letterRepositoryMock).findByIdAndDeleted(letter2.getId(), false);
		verify(letterRepositoryMock).save(letter1);
		verify(letterRepositoryMock).save(letter2);
		verify(kivraIntegrationMock).deleteResponse(letter1.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).deleteResponse(letter2.getId(), MUNICIPALITY_ID, ORG_NUMBER);
		verify(letterMapperMock).updateLetterStatus(letter1, status);
		verify(letterMapperMock).updateLetterStatus(letter2, status);
		verify(letterMapperMock).updateSigningInformation(letter1.getSigningInformation(), registeredLetterResponse1);
		verify(letterMapperMock).updateSigningInformation(letter2.getSigningInformation(), registeredLetterResponse2);
	}
}

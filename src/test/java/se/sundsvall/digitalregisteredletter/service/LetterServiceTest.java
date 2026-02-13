package se.sundsvall.digitalregisteredletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.TestDataFactory.createLetterEntity;
import static se.sundsvall.TestDataFactory.createLetterRequest;

import generated.se.sundsvall.templating.RenderResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.sql.rowset.serial.SerialBlob;
import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.LetterStatus;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.integration.db.RepositoryIntegration;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.integration.templating.TemplatingIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private RepositoryIntegration repositoryIntegrationMock;

	@Mock
	private LetterMapper letterMapperMock;

	@Mock
	private TemplatingIntegration templatingIntegrationMock;

	@InjectMocks
	private LetterService letterService;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(repositoryIntegrationMock, partyIntegrationMock, kivraIntegrationMock, letterMapperMock, templatingIntegrationMock);
	}

	private HttpServletResponse mockHttpServletResponse(final ByteArrayOutputStream outputStream) throws IOException {
		final var response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
			@Override
			public void write(final int b) {
				outputStream.write(b);
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(final WriteListener writeListener) {
				// Not needed for testing
			}
		});
		return response;
	}

	@Test
	void sendLetterWithOrganizationNumber() {
		final var multipartFile = mock(MultipartFile.class);
		final var multipartFileList = List.of(multipartFile);
		final var municipalityId = "2281";
		final var organizationNumber = "5591628136";
		final var letterRequest = createLetterRequest();
		final var legalId = "legalId";
		final var letterEntity = createLetterEntity();
		final var letterMock = mock(Letter.class);
		final var status = "status";
		Identifier.set(Identifier.parse("type=adAccount; test01user"));

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, letterRequest.partyId())).thenReturn(Optional.of(legalId));
		when(repositoryIntegrationMock.persistLetter(any(), any(), any())).thenReturn(letterEntity);
		when(kivraIntegrationMock.sendContent(letterEntity, legalId, municipalityId, organizationNumber)).thenReturn(status);
		when(letterMapperMock.toLetter(letterEntity)).thenReturn(letterMock);

		final var response = letterService.sendLetter(municipalityId, organizationNumber, letterRequest, multipartFileList);

		assertThat(response).isEqualTo(letterMock);

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, letterRequest.partyId());
		verify(repositoryIntegrationMock).persistLetter(municipalityId, letterRequest, multipartFileList);
		verify(kivraIntegrationMock).sendContent(letterEntity, legalId, municipalityId, organizationNumber);
		verify(repositoryIntegrationMock).updateStatus(letterEntity, status);
		verifyNoInteractions(letterMock);
	}

	@Test
	void sendLetterLegacy() {
		final var multipartFile = mock(MultipartFile.class);
		final var multipartFileList = List.of(multipartFile);
		final var municipalityId = "2281";
		final var letterRequest = createLetterRequest();
		final var legalId = "legalId";
		final var letterEntity = createLetterEntity();
		final var letterMock = mock(Letter.class);
		final var status = "status";
		Identifier.set(Identifier.parse("type=adAccount; test01user"));

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, letterRequest.partyId())).thenReturn(Optional.of(legalId));
		when(repositoryIntegrationMock.persistLetter(any(), any(), any())).thenReturn(letterEntity);
		when(kivraIntegrationMock.sendContent(letterEntity, legalId)).thenReturn(status);
		when(letterMapperMock.toLetter(letterEntity)).thenReturn(letterMock);

		final var response = letterService.sendLetter(municipalityId, letterRequest, multipartFileList);

		assertThat(response).isEqualTo(letterMock);

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, letterRequest.partyId());
		verify(repositoryIntegrationMock).persistLetter(municipalityId, letterRequest, multipartFileList);
		verify(kivraIntegrationMock).sendContent(letterEntity, legalId);
		verify(repositoryIntegrationMock).updateStatus(letterEntity, status);
		verifyNoInteractions(letterMock);
	}

	@Test
	void sendLetterForNonExistingPartyId() {
		final var municipalityId = "2281";
		final var letterRequest = createLetterRequest();
		final var multipartFile = mock(MultipartFile.class);
		final var multipartFileList = List.of(multipartFile);
		Identifier.set(Identifier.parse("type=adAccount; test01user"));

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, letterRequest.partyId()))
			.thenThrow(Problem.valueOf(Status.BAD_REQUEST, "The given partyId [%s] does not exist in the Party API or is not of type PRIVATE".formatted(letterRequest.partyId())));

		assertThatThrownBy(() -> letterService.sendLetter(municipalityId, letterRequest, multipartFileList))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Request: The given partyId [%s] does not exist in the Party API or is not of type PRIVATE".formatted(letterRequest.partyId()));

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, letterRequest.partyId());
	}

	@Test
	void getLetter() {
		final var municipalityId = "2281";
		final var letterId = "12345";
		final var letterEntityMock = mock(LetterEntity.class);
		final var letterMock = mock(Letter.class);

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));
		when(letterMapperMock.toLetter(letterEntityMock)).thenReturn(letterMock);

		final var result = letterService.getLetter(municipalityId, letterId);

		assertThat(result).isSameAs(letterMock);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verifyNoInteractions(letterEntityMock, letterMock);
	}

	@Test
	void getLetterNotFound() {
		final var municipalityId = "2281";
		final var letterId = "12345";

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> letterService.getLetter(municipalityId, letterId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Letter with id '%s' and municipalityId '%s' not found", letterId, municipalityId);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
	}

	@Test
	void getLetterStatuses() {
		final var municipalityId = "2281";
		final var id1 = "11111111-1111-1111-1111-111111111111";
		final var id2 = "22222222-2222-2222-2222-222222222222";
		final var id3 = "33333333-3333-3333-3333-333333333333";

		final var e1 = LetterEntity.create().withId(id1).withStatus("NEW");
		final var e2 = LetterEntity.create().withId(id2).withStatus("SIGNED");

		when(repositoryIntegrationMock.getLetterEntities(municipalityId, List.of(id1, id2, id3)))
			.thenReturn(List.of(e2, e1));
		when(letterMapperMock.toLetterStatus(e1))
			.thenReturn(new LetterStatus(id1, "NEW", "NOT_FOUND"));
		when(letterMapperMock.toLetterStatus(e2))
			.thenReturn(new LetterStatus(id2, "SIGNED", "NOT_FOUND"));
		when(letterMapperMock.toLetterStatus(id3, null, null))
			.thenReturn(new LetterStatus(id3, "NOT_FOUND", "NOT_FOUND"));

		final var result = letterService.getLetterStatuses(municipalityId, List.of(id1, id2, id3));

		assertThat(result).containsExactly(
			new LetterStatus(id1, "NEW", "NOT_FOUND"),
			new LetterStatus(id2, "SIGNED", "NOT_FOUND"),
			new LetterStatus(id3, "NOT_FOUND", "NOT_FOUND"));

		verify(repositoryIntegrationMock).getLetterEntities(municipalityId, List.of(id1, id2, id3));
	}

	@Test
	void getSigningInformation() {
		final var municipalityId = "2281";
		final var letterId = "12345";
		final var letterEntityMock = mock(LetterEntity.class);
		final var signingInformationEntityMock = mock(SigningInformationEntity.class);
		final var signingInfoMock = mock(SigningInfo.class);

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));
		when(letterEntityMock.getSigningInformation()).thenReturn(signingInformationEntityMock);
		when(letterMapperMock.toSigningInfo(signingInformationEntityMock)).thenReturn(signingInfoMock);

		final var result = letterService.getSigningInformation(municipalityId, letterId);

		assertThat(result).isSameAs(signingInfoMock);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verify(letterMapperMock).toSigningInfo(signingInformationEntityMock);
		verify(letterEntityMock).getSigningInformation();
		verifyNoMoreInteractions(letterEntityMock, signingInformationEntityMock, signingInfoMock);
	}

	@Test
	void getSigningInformationForMissingLetter() {
		final var municipalityId = "2281";
		final var letterId = "12345";

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> letterService.getSigningInformation(municipalityId, letterId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Letter with id '%s' and municipalityId '%s' not found", letterId, municipalityId);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
	}

	@Test
	void getSigningInformationNotFound() {
		final var municipalityId = "2281";
		final var letterId = "12345";

		final var letterEntityMock = mock(LetterEntity.class);

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));

		assertThatThrownBy(() -> letterService.getSigningInformation(municipalityId, letterId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Signing information belonging to letter with id '%s' and municipalityId '%s' not found", letterId, municipalityId);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verify(letterMapperMock).toSigningInfo(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	void getLetters() {
		final var municipalityId = "2281";
		final var letterFilterMock = mock(LetterFilter.class);
		final var pageableMock = mock(Pageable.class);
		final var pageMock = mock(Page.class);
		final var lettersMock = mock(Letters.class);

		when(repositoryIntegrationMock.getPagedLetterEntities(municipalityId, letterFilterMock, pageableMock)).thenReturn(pageMock);
		when(letterMapperMock.toLetters(pageMock)).thenReturn(lettersMock);

		final var result = letterService.getLetters(municipalityId, letterFilterMock, pageableMock);

		assertThat(result).isSameAs(lettersMock);

		verify(repositoryIntegrationMock).getPagedLetterEntities(municipalityId, letterFilterMock, pageableMock);
		verifyNoInteractions(letterFilterMock, pageableMock, pageMock, lettersMock);
	}

	@Test
	void testReadLetterAttachment() throws Exception {
		// Arrange
		final var municipalityId = "2281";
		final var letterId = "1234";
		final var letterEntityMock = mock(LetterEntity.class);
		final var outputStream = new ByteArrayOutputStream();
		final var response = mockHttpServletResponse(outputStream);
		final var minimalPdf = "JVBERi0xLjUKJbXtrvsKNCAwIG9iago8PCAvTGVuZ3RoIDUgMCBSCiAgIC9GaWx0ZXIgL0ZsYXRlRGVjb2RlCj4+CnN0cmVhbQp4nDNUMABCXUMgYW5ppJCcy1UIhGaGRhBxkBhcRj/RQCG9WEG/wlzBJZ8rEAgBf7IM0QplbmRzdHJlYW0KZW5kb2JqCjUgMCBvYmoKICAgNTAKZW5kb2JqCjMgMCBvYmoKPDwKICAgL0V4dEdTdGF0ZSA8PAogICAgICAvYTAgPDwgL0NBIDEgL2NhIDEgPj4KICAgPj4KICAgL1hPYmplY3QgPDwgL3g3IDcgMCBSID4+Cj4+CmVuZG9iago5IDAgb2JqCjw8IC9MZW5ndGggMTAgMCBSCiAgIC9GaWx0ZXIgL0ZsYXRlRGVjb2RlCiAgIC9UeXBlIC9YT2JqZWN0CiAgIC9TdWJ0eXBlIC9JbWFnZQogICAvV2lkdGggMTcwMAogICAvSGVpZ2h0IDIyMDAKICAgL0NvbG9yU3BhY2UgL0RldmljZUdyYXkKICAgL0ludGVycG9sYXRlIHRydWUKICAgL0JpdHNQZXJDb21wb25lbnQgOAo+PgpzdHJlYW0KeJzt3X+slXUdB/DvRVHclR8KgoJIgkoWiaHFppa1mizdGJrhmjrnJCSjqA2TiubNWIEVXCwBjSlrjpVSJhUGAkIFKt5EUhlTBE0qDJWQxc/u/fScc889PziM7lnIduH1+ud7v9/neZ97v3+9d8/znOekBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAcpbpN3fDu3+e1+/RR8zJX1PQbJuUiZ9X4ZwFAz/X/vv3WVza0//zPzIr4ak2/4uxrl0ZcWOsfBsAx78G4I6Vb2t9RKX261o5KaYqOAqBmvfbHpSl1a6whoqMAOCJGR5xTY0RHAXBENET0rzGSddSEGiNZRw2tMQLAMW9mxJk1RrKOGl9jJOuoITVGADjmzanoqB6DP3haxeHTh5zX7cBI1lFjUqeBF/SvK611OmVArwNOO2lQ6ecpuXcUuwweUv7iB4kAQJtutz70VsS6pqam87LZiRNeiMzmKd0Lh3tP35LNW5675biKVNZRE3/6bnbkzemn5hfqf7F+fzbd1tg1m3wie7WmAenjC/Y+X4pkHTXq8X3ZORvGH18dSfc1FdX6LiIAR6kBjY1/iZjb2NjYL6VTVsemG84598ZXY+PA/NELt8byEQM+NGln/O6k8lTWUbuWzf3+vS9HvJK/ltUr1n/p4kFjdsWy3Kz/jIjPLcoKqLKjti2c9b15WSP+pnN1ZMWeRbPuamiYnhXipUdk4wB0BKX3+hbEW31zY+8tsTbXIydvjj/k/+m5rDnuLY+03dd33LSIlbkfesXFueFHEcNz45iIjXdfPfSRyo7K39fXY2nEd6ojK1qvb82OmP2ebBKADqnYUR+J+G7r0viIG7Ph9ohPtS4siOb3lUVK954/E/GxbOi+OH9lanTEF3Jj1lGjsmHQ0lKkeO95752xo74qsiJ/8JKW+Fv3BAAFxY6a2do3mTMjFmfDuth9QuvCDRHfKIuUOmpsxPSK9fzVpEJHlSt9Pmp+xMiqyEW5C16dX4y4+nBsCYCjRLGjno5ou+tue2yvS/XN8VJhPjTi12WRUkdlB56sXj9kR02IuPNgL5XStyN+9X9uBoCjSrGj3oyWtrWXInqk8yOWF+anVdz/UFYsPaPQYycPv+amsT9pT0d9NgoXtyoimcF7Ykffw7YtAI4CxY7aHbva1p6NOCsNj1hUmNdHbCqLlDoqO7A5Gwb9cn+89tSyNe3pqKsiHqyOpFS3ImLc4d0aAB1csaN2xL62tecjTk8fjlhSmPeIKH8ueqmjekW8kFK/f8TKs1M73+u7NuLH1ZF86E91CQBKih21OaJwi0R6NVq6pAERqwrz/hFPlUVKxfL+iKUpTY0YWL5+yI4aFzG5OpL6vBN7zz+8OwOgoyt21MKIwsOLjt8XL6dU9694o3DO5RH3l0VKHTUy4gcpPRnbKtYP2VHTI66qjqSft91KMexrh29vAHRsxY6aEDG6demSiPuy4dGIPq0L34z4fFmk1FEzIz6Z0tr4Z8X6ITtqXezsWh25MmJ9639xk+86jJsDoEMrdlTPnW23ft8fLcOy4YqIr+TnnTbE1hPLIsWO6v52rKtLaUm09ClfP1RHXRZxT6qK1L9WfAjScg/sA6Cg9CyksdFyfW68rrnwwdz58XbuCzU6zYyWis5p66Kui2LXRdk4OeKxM1Kfm5753x117qbY2K06MiNiVv68LqP2XP+ebBOADmfED/8aseTum/M19eVdsfjOhidi/7RO+YMnPBC7H/rWtBdj+3UVoWHb4/XHHpj9yDvxRv7JFKe+HhHNsfWaiD9+sU/vccsiHp84oiIybm+se3junN/vi9V9qyND/xOxvqmp6c9bmiMqgwAcs8Y3tPpoftbvjoVr1vx2cum744dNfeK5VQ/f1uOAVOfLJ/1s5dpV828uPA79jBlPP/vobfVpYvZKQz7Q+opfr4x0Hzllweq1K+ZcmQ4SuaChTK3fuAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHOv+C0jaOxIKZW5kc3RyZWFtCmVuZG9iagoxMCAwIG9iagogICA1MDM5CmVuZG9iago3IDAgb2JqCjw8IC9MZW5ndGggMTEgMCBSCiAgIC9GaWx0ZXIgL0ZsYXRlRGVjb2RlCiAgIC9UeXBlIC9YT2JqZWN0CiAgIC9TdWJ0eXBlIC9JbWFnZQogICAvV2lkdGggMTcwMAogICAvSGVpZ2h0IDIyMDAKICAgL0NvbG9yU3BhY2UgL0RldmljZUdyYXkKICAgL0ludGVycG9sYXRlIHRydWUKICAgL0JpdHNQZXJDb21wb25lbnQgMQogICAvU01hc2sgOSAwIFIKPj4Kc3RyZWFtCnic7cExAQAAAMKg9U9tCU+gAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD4GJuEAAQplbmRzdHJlYW0KZW5kb2JqCjExIDAgb2JqCiAgIDQ3NwplbmRvYmoKOCAwIG9iago8PCAvVHlwZSAvT2JqU3RtCiAgIC9MZW5ndGggMTIgMCBSCiAgIC9OIDEKICAgL0ZpcnN0IDQKICAgL0ZpbHRlciAvRmxhdGVEZWNvZGUKPj4Kc3RyZWFtCnicM1Mw4IrmiuUCAAY4AV0KZW5kc3RyZWFtCmVuZG9iagoxMiAwIG9iagogICAxNgplbmRvYmoKMTMgMCBvYmoKPDwgL1R5cGUgL09ialN0bQogICAvTGVuZ3RoIDE2IDAgUgogICAvTiA0CiAgIC9GaXJzdCAyMwogICAvRmlsdGVyIC9GbGF0ZURlY29kZQo+PgpzdHJlYW0KeJxVkVFrgzAUhd/9FedlTBlobqrrVqQPVShjDMTubewhpMEKw0gSx/rvl9jaMfJ0P87NOYdLYBHlKFjEQQVFVGC1zqOyRPZ+HhWyRnTKRgCy1/5o8QEOhhafM6r0NDhQtN3OG43Rx0kqg1iK3mhQSk9pjvjk3Gg3WTbTzojx1EubatMlyeUbo4Tr9VALpxDXG854QYwR5Tmj9QPj94wli8lfLNx567DfCKNCjpBsBm/q2Iud/vFxmX+PxLF+5rfQg/Nyi/ym3xs9jSjLMIT54jHTBR08NWKwY/CS5wW/wJlJLVPlVbX67qVq97sAfebAW2X1ZKSyWN08D35Rukt066/wr14lnPjS3bWdv8C1nBf9Aht2bs0KZW5kc3RyZWFtCmVuZG9iagoxNiAwIG9iagogICAyNzgKZW5kb2JqCjE3IDAgb2JqCjw8IC9UeXBlIC9YUmVmCiAgIC9MZW5ndGggNzIKICAgL0ZpbHRlciAvRmxhdGVEZWNvZGUKICAgL1NpemUgMTgKICAgL1cgWzEgMiAyXQogICAvUm9vdCAxNSAwIFIKICAgL0luZm8gMTQgMCBSCj4+CnN0cmVhbQp4nGNgYPj/n4mBl4EBRDAxMixmYGBk4AcRfSAxDiBLdDmQkGgBEozsIG4/iJsLJCRBXElZiF5GEMHMKLUCKCa1n4EBAJM2CBEKZW5kc3RyZWFtCmVuZG9iagpzdGFydHhyZWYKNjg0NwolJUVPRgoxOCAwIG9iag0KPDwvU2l6ZSAxOSAvUm9vdCAxNSAwIFIgL1ByZXYgNjg0NyAvVHlwZSAvWFJlZiAvSW5kZXggWzAgMSAxNCAxIDE4IDEgXSAvVyBbMSA0IDIgXSAvTGVuZ3RoIDIxID4+IHN0cmVhbQ0KAAAAAAH//wAAAAAPAAIBAAAbsAAADQplbmRzdHJlYW0NCg0KZW5kb2JqDQpzdGFydHhyZWYNCjcwODgNCiUlRU9GDQo=";

		final List<AttachmentEntity> attachments = List.of(
			new AttachmentEntity()
				.withContentType("application/pdf")
				.withContent(new SerialBlob(Base64.getDecoder().decode(minimalPdf))));
		final var renderResponse = new RenderResponse().output(minimalPdf);

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));
		when(letterEntityMock.getAttachments()).thenReturn(attachments);
		when(templatingIntegrationMock.renderPdf(municipalityId, letterEntityMock))
			.thenReturn(renderResponse);

		// Act
		letterService.readLetterReceipt(municipalityId, letterId, response);

		// Assert
		final var pdfDocument = Loader.loadPDF(outputStream.toByteArray());

		// Assert
		assertThat(outputStream).isNotNull();
		assertThat(pdfDocument.getNumberOfPages()).isEqualTo(2);
		verify(templatingIntegrationMock).renderPdf(municipalityId, letterEntityMock);
		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);

	}

	@Test
	void testReadLetterAttachment_withAttachmentId() throws Exception {
		// Arrange
		final var municipalityId = "2281";
		final var letterId = "1234";
		final var attachmentId = "attachmentId";
		final var contentType = "application/pdf";
		final var fileName = "test.pdf";
		final var content = "test content";

		final var outputStream = new ByteArrayOutputStream();
		final var response = mockHttpServletResponse(outputStream);

		final var letterEntityMock = mock(LetterEntity.class);
		final var attachmentEntityMock = mock(AttachmentEntity.class);
		final var blobMock = mock(Blob.class);

		// Setup Letter with Attachment metadata
		final var attachment = new Letter.Attachment(attachmentId, fileName, contentType);
		final var letter = new Letter(letterId, null, municipalityId, null, null, null, null, null, null, List.of(attachment));

		// Setup mocks
		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));
		when(letterMapperMock.toLetter(letterEntityMock)).thenReturn(letter);
		when(repositoryIntegrationMock.getAttachmentEntity(municipalityId, letterId, attachmentId)).thenReturn(Optional.of(attachmentEntityMock));
		when(attachmentEntityMock.getContent()).thenReturn(blobMock);
		when(blobMock.getBinaryStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
		when(blobMock.length()).thenReturn((long) content.length());

		// Act
		letterService.readLetterAttachment(municipalityId, letterId, attachmentId, response);

		// Assert
		verify(response).addHeader(CONTENT_TYPE, contentType);
		verify(response).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(response).getOutputStream();
		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verify(letterMapperMock).toLetter(letterEntityMock);
		verify(repositoryIntegrationMock).getAttachmentEntity(municipalityId, letterId, attachmentId);

		assertThat(outputStream).hasToString(content);
	}

	@Test
	void testReadLetterReceipt() throws Exception {
		// Arrange
		final var municipalityId = "2281";
		final var letterId = "1234";
		final var expectedFilename = "kvittens_rekutskick_" + letterId + ".pdf";

		final var outputStream = new ByteArrayOutputStream();
		final var response = mockHttpServletResponse(outputStream);

		final var letterEntityMock = mock(LetterEntity.class);
		final var attachmentEntityMock = mock(AttachmentEntity.class);
		final var blobMock = mock(Blob.class);

		// Minimal valid PDF for testing merge functionality
		final var minimalPdf = "JVBERi0xLjQKJeLjz9MKMyAwIG9iago8PC9UeXBlL1BhZ2UvUGFyZW50IDIgMCBSL01lZGlhQm94WzAgMCA2MTIgNzkyXT4+CmVuZG9iago0IDAgb2JqCjw8L0xlbmd0aCAzNT4+CnN0cmVhbQpCVAovRjEgMTIgVGYKKFRlc3QgUERGKSBUagpFVAplbmRzdHJlYW0KZW5kb2JqCjEgMCBvYmoKPDwvVHlwZS9QYWdlcy9LaWRzWzMgMCBSXS9Db3VudCAxPj4KZW5kb2JqCjIgMCBvYmoKPDwvVHlwZS9DYXRhbG9nL1BhZ2VzIDEgMCBSPj4KZW5kb2JqCnhyZWYKMCA1CjAwMDAwMDAwMDAgNjU1MzUgZiAKMDAwMDAwMDE0NyAwMDAwMCBuIAowMDAwMDAwMTk2IDAwMDAwIG4gCjAwMDAwMDAwMTUgMDAwMDAgbiAKMDAwMDAwMDA3NCAwMDAwMCBuIAp0cmFpbGVyCjw8L1NpemUgNS9Sb290IDIgMCBSPj4Kc3RhcnR4cmVmCjI0NQolJUVPRgo=";
		final var renderResponse = new RenderResponse().output(minimalPdf);
		final var pdfBytes = Base64.getDecoder().decode(minimalPdf);

		// Setup mocks
		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));
		when(letterEntityMock.getAttachments()).thenReturn(List.of(attachmentEntityMock));
		when(attachmentEntityMock.getContent()).thenReturn(blobMock);
		when(blobMock.getBinaryStream()).thenReturn(new ByteArrayInputStream(pdfBytes));
		when(templatingIntegrationMock.renderPdf(municipalityId, letterEntityMock)).thenReturn(renderResponse);

		// Act
		letterService.readLetterReceipt(municipalityId, letterId, response);

		// Assert
		verify(response).addHeader(CONTENT_TYPE, "application/pdf");
		verify(response).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + expectedFilename + "\"");
		verify(response).getOutputStream();
		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verify(templatingIntegrationMock).renderPdf(municipalityId, letterEntityMock);

		assertThat(outputStream.toByteArray()).isNotEmpty();
	}
}

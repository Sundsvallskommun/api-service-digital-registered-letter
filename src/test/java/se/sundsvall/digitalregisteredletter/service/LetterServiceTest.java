package se.sundsvall.digitalregisteredletter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

	@InjectMocks
	private LetterService letterService;

	@Test
	void sendLetterTest() {
		// TODO: Will be implemented in a future task
		letterService.sendLetter(null, null);
	}

	@Test
	void getLetterTest() {
		// TODO: Will be implemented in a future task
		letterService.getLetters(null, null);
	}

	@Test
	void getLettersTest() {
		// TODO: Will be implemented in a future task
		letterService.getLetter(null, null);
	}

}

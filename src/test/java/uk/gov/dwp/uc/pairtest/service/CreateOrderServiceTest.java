package uk.gov.dwp.uc.pairtest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

class CreateOrderServiceTest {

  private CreateOrderService service;

  @BeforeEach
  void setUp() {
    service = new CreateOrderService();
  }

  @Test
  @DisplayName("Child ticket no adult - throws exception")
  void child_no_adult_invalid_request() {

    TicketTypeRequest[] ticketRequest = {
        new TicketTypeRequest(Type.CHILD, 1)
    };

    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(ticketRequest));

    assertEquals("Cannot purchase child or infant tickets without also purchasing an adult ticket", thrown.getMessage());
  }

  @Test
  @DisplayName("Infant ticket no adult - throws exception")
  void infant_no_adult_invalid_request() {

    TicketTypeRequest[] ticketRequest = {
        new TicketTypeRequest(Type.INFANT, 1)
    };

    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(ticketRequest));

    assertEquals("Cannot purchase child or infant tickets without also purchasing an adult ticket", thrown.getMessage());
  }

  @Test
  @DisplayName("Infant and child tickets no adult - throws exception")
  void infant_and_child_no_adult_invalid_request() {

    TicketTypeRequest[] ticketRequest = {
        new TicketTypeRequest(Type.INFANT, 1),
        new TicketTypeRequest(Type.CHILD, 1)
    };
    
    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(ticketRequest));

    assertEquals(
        "Cannot purchase child or infant tickets without also purchasing an adult ticket",
        thrown.getMessage());
  }

  @ParameterizedTest
  @DisplayName("Zero tickets requested - throws exception")
  @EnumSource(Type.class)
  void zero_tickets_on_request(Type ticketType) {

    TicketTypeRequest[] ticketRequest = {
        new TicketTypeRequest(ticketType, 0)
    };
    
    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(ticketRequest));

    assertEquals("Seats to be reserved or order cost cannot be zero.", thrown.getMessage());
  }

  @ParameterizedTest
  @DisplayName("Valid request returns correct seats and cost")
  @MethodSource("validRequests")
  void valid_request_returns_correct_values_adult(TicketTypeRequest[] request, int expectedSeats, int expectedCost) {

    var result = service.createOrderSummary(request);

    assertEquals(expectedSeats, result.get("Seats"));
    assertEquals(expectedCost, result.get("TotalCost"));
  }

  static Stream<Arguments> validRequests() {

    TicketTypeRequest[] oneAdult = {
        new TicketTypeRequest(Type.ADULT, 1)
    };

    TicketTypeRequest[] maxAdults = {
        new TicketTypeRequest(Type.ADULT, 20)
    };

    TicketTypeRequest[] maxChildOneAdult = {
        new TicketTypeRequest(Type.ADULT, 1),
        new TicketTypeRequest(Type.CHILD, 19)
    };

    TicketTypeRequest[] maxInfantsAndAdults = {
        new TicketTypeRequest(Type.ADULT, 10),
        new TicketTypeRequest(Type.INFANT, 10)
    };

    TicketTypeRequest[] oneEachType = {
        new TicketTypeRequest(Type.ADULT, 1),
        new TicketTypeRequest(Type.CHILD, 1),
        new TicketTypeRequest(Type.INFANT, 1)
    };

    TicketTypeRequest[] oneAdultOneChild = {
        new TicketTypeRequest(Type.ADULT, 1),
        new TicketTypeRequest(Type.CHILD, 1)
    };

    TicketTypeRequest[] oneAdultOneInfant = {
        new TicketTypeRequest(Type.ADULT, 1),
        new TicketTypeRequest(Type.INFANT, 1)
    };

    return Stream.of(
        Arguments.of(oneAdult, 1, 20),
        Arguments.of(maxAdults, 20, 400),
        Arguments.of(maxChildOneAdult, 20, 210),
        Arguments.of(maxInfantsAndAdults, 10, 200),
        Arguments.of(oneEachType, 2, 30),
        Arguments.of(oneAdultOneChild, 2, 30),
        Arguments.of(oneAdultOneInfant, 1, 20)
    );
  }
}

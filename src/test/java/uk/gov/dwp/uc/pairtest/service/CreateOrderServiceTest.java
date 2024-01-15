package uk.gov.dwp.uc.pairtest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

class CreateOrderServiceTest {

  private CreateOrderService service;

  @BeforeEach
  void setUp() {
    service = new CreateOrderService();
  }

  @Test
  @DisplayName("Excessive tickets ordered throws exception ")
  void too_many_tickets_requested() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.ADULT, 10);
    order.put(Type.CHILD, 8);
    order.put(Type.INFANT, 3);

    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(order));

    assertEquals("Total ordered tickets (21), exceeds maximum allowed (20).", thrown.getMessage());
  }

  @Test
  @DisplayName("Child ticket no adult - throws exception")
  void child_no_adult_invalid_request() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.CHILD, 1);

    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(order));

    assertEquals("Cannot purchase child or infant tickets without also purchasing an adult ticket", thrown.getMessage());
  }

  @Test
  @DisplayName("Infant ticket no adult - throws exception")
  void infant_no_adult_invalid_request() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.INFANT, 1);

    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(order));

    assertEquals("Cannot purchase child or infant tickets without also purchasing an adult ticket", thrown.getMessage());
  }

  @Test
  @DisplayName("Infant and child tickets no adult - throws exception")
  void infant_and_child_no_adult_invalid_request() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.INFANT, 1);
    order.put(Type.CHILD, 1);

    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(order));

    assertEquals(
        "Cannot purchase child or infant tickets without also purchasing an adult ticket",
        thrown.getMessage());
  }

  @ParameterizedTest
  @DisplayName("Zero tickets requested - throws exception")
  @EnumSource(Type.class)
  void zero_tickets_on_request(Type ticketType) {

    var order = new HashMap<Type, Integer>();
    order.put(ticketType, 0);

    var thrown =
        assertThrows(InvalidPurchaseException.class, () -> service.createOrderSummary(order));

    assertEquals("Seats to be reserved or order cost cannot be zero.", thrown.getMessage());
  }

  @ParameterizedTest
  @DisplayName("Valid request returns correct seats and cost")
  @MethodSource("validRequests")
  void valid_request_returns_correct_values_adult(Map<Type, Integer> order, int expectedSeats, int expectedCost) {

    var result = service.createOrderSummary(order);

    assertEquals(expectedSeats, result.get("Seats"));
    assertEquals(expectedCost, result.get("TotalCost"));
  }

  static Stream<Arguments> validRequests() {

    var oneAdult = new HashMap<Type, Integer>();
    oneAdult.put(Type.ADULT, 1);

    var maxAdults = new HashMap<Type, Integer>();
    maxAdults.put(Type.ADULT, 20);

    var maxChildOneAdult = new HashMap<Type, Integer>();
    maxChildOneAdult.put(Type.ADULT, 1);
    maxChildOneAdult.put(Type.CHILD, 19);

    var maxInfantsAndAdults = new HashMap<Type, Integer>();
    maxInfantsAndAdults.put(Type.ADULT, 10);
    maxInfantsAndAdults.put(Type.INFANT, 10);

    var oneEachType = new HashMap<Type, Integer>();
    oneEachType.put(Type.ADULT, 1);
    oneEachType.put(Type.CHILD, 1);
    oneEachType.put(Type.INFANT, 1);

    var oneAdultOneChild = new HashMap<Type, Integer>();
    oneAdultOneChild.put(Type.ADULT, 1);
    oneAdultOneChild.put(Type.CHILD, 1);

    var oneAdultOneInfant = new HashMap<Type, Integer>();
    oneAdultOneInfant.put(Type.ADULT, 1);
    oneAdultOneInfant.put(Type.INFANT, 1);

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

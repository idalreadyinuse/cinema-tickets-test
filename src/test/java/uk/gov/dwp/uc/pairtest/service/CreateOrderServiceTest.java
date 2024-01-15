package uk.gov.dwp.uc.pairtest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

  @Test
  @DisplayName("Valid request returns correct seats and cost")
  void valid_request_returns_correct_values_adult() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.ADULT, 1);

    var result = service.createOrderSummary(order);

    assertEquals(1, result.get("Seats"));
    assertEquals(20, result.get("TotalCost"));
  }

  @Test
  @DisplayName("Valid request returns correct seats and cost")
  void valid_request_returns_correct_values_adult_child() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.ADULT, 2);
    order.put(Type.CHILD, 3);

    var result = service.createOrderSummary(order);

    assertEquals(5, result.get("Seats"));
    assertEquals(70, result.get("TotalCost"));
  }

  @Test
  @DisplayName("Valid request returns correct seats and cost")
  void valid_request_returns_correct_values_adult_infant() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.ADULT, 2);
    order.put(Type.INFANT, 2);

    var result = service.createOrderSummary(order);

    assertEquals(2, result.get("Seats"));
    assertEquals(40, result.get("TotalCost"));
  }

  @Test
  @DisplayName("Valid request returns correct seats and cost")
  void valid_request_returns_correct_values_adult_child_infant() {

    var order = new HashMap<Type, Integer>();
    order.put(Type.ADULT, 3);
    order.put(Type.INFANT, 2);
    order.put(Type.CHILD, 2);

    var result = service.createOrderSummary(order);

    assertEquals(5, result.get("Seats"));
    assertEquals(80, result.get("TotalCost"));
  }
}

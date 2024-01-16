package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.service.CreateOrderService;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

  @Mock TicketPaymentService ticketPaymentService;

  @Mock SeatReservationService seatReservationService;

  @Mock CreateOrderService createOrderService;

  private TicketServiceImpl ticketService;
  private static final Long ACCOUNT_ID = 123456L;

  @BeforeEach
  void setUpMocks() {
    reset(ticketPaymentService, seatReservationService, createOrderService);
    ticketService =
        new TicketServiceImpl(ticketPaymentService, seatReservationService, createOrderService);
  }

  @ParameterizedTest
  @DisplayName("When account id invalid - exception thrown")
  @NullSource
  @ValueSource(longs = {0L, -1L})
  void account_is_invalid(Long accountId) {

    var ticketRequest = new TicketTypeRequest(Type.ADULT, 1);

    var thrown =
        assertThrows(
            InvalidPurchaseException.class,
            () -> ticketService.purchaseTickets(accountId, ticketRequest));

    assertEquals("Account ID is invalid", thrown.getMessage());

    verifyNoInteractions(createOrderService);
    verifyNoInteractions(ticketPaymentService);
    verifyNoInteractions(seatReservationService);
  }

  @Test
  @DisplayName("More than 20 tickets ordered throws exception ")
  void maximum_20_tickets_can_be_ordered() {

    var adultTickets = new TicketTypeRequest(Type.ADULT, 10);
    var childTickets = new TicketTypeRequest(Type.CHILD, 8);
    var infantTickets = new TicketTypeRequest(Type.INFANT, 3);

    var thrown =
        assertThrows(
            InvalidPurchaseException.class,
            () ->
                ticketService.purchaseTickets(
                    ACCOUNT_ID, adultTickets, childTickets, infantTickets));

    assertEquals("Total ordered tickets (21), exceeds maximum allowed (20).", thrown.getMessage());
  }

  @ParameterizedTest
  @DisplayName("When valid request submitted 3rd party services invoked with correct values")
  @MethodSource("uk.gov.dwp.uc.pairtest.service.CreateOrderServiceTest#validRequests")
  void valid_request_invokes_3rd_party_services(
      TicketTypeRequest[] request, int expectedSeats, int expectedCost) {

    when(createOrderService.createOrderSummary(request)).thenCallRealMethod();

    ticketService.purchaseTickets(ACCOUNT_ID, request);

    verify(seatReservationService, times(1)).reserveSeat(ACCOUNT_ID, expectedSeats);
    verify(ticketPaymentService, times(1)).makePayment(ACCOUNT_ID, expectedCost);
  }
}

package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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
  @DisplayName("When valid request submitted 3rd party services invoked")
  void valid_request_invokes_3rd_party_services() {

    var adultTicketRequest = new TicketTypeRequest(Type.ADULT, 2);
    var childTicketRequest = new TicketTypeRequest(Type.CHILD, 1);
    var infantTicketRequest = new TicketTypeRequest(Type.INFANT, 1);

    var orderSummary =
        Map.of(
            "Seats", 3,
            "TotalCost", 50);

    when(createOrderService.createOrderSummary(any())).thenReturn(orderSummary);

    ticketService.purchaseTickets(
        12345L, adultTicketRequest, childTicketRequest, infantTicketRequest);

    verify(seatReservationService, times(1)).reserveSeat(12345L, 3);
    verify(ticketPaymentService, times(1)).makePayment(12345L, 50);
  }
}

package uk.gov.dwp.uc.pairtest;

import static uk.gov.dwp.uc.pairtest.service.CreateOrderService.ORDER_COST_LABEL;
import static uk.gov.dwp.uc.pairtest.service.CreateOrderService.TOTAL_SEATS_LABEL;

import java.util.Arrays;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.service.CreateOrderService;

@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

  private static final Logger LOG = Logger.getLogger(TicketServiceImpl.class.getName());

  private final TicketPaymentService ticketPaymentService;
  private final SeatReservationService seatReservationService;
  private final CreateOrderService createOrderService;

  private static final int MAX_TICKETS_ALLOWED = 20;

  @Override
  public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
      throws InvalidPurchaseException {

    initialValidation(accountId, ticketTypeRequests);

    var orderSummary = createOrderService.createOrderSummary(ticketTypeRequests);

    var seatsToReserve = orderSummary.get(TOTAL_SEATS_LABEL);
    var orderCost = orderSummary.get(ORDER_COST_LABEL);

    LOG.info(String.format("Requesting payment for account ID %s", accountId));
    ticketPaymentService.makePayment(accountId, orderCost);

    LOG.info(String.format("Reserving %s seats for account ID %s", seatsToReserve, accountId));
    seatReservationService.reserveSeat(accountId, seatsToReserve);
  }

  private static void initialValidation(Long accountId, TicketTypeRequest... ticketTypeRequests) {
    if (accountId == null || accountId <= 0L) {
      var message = "Account ID is invalid";
      LOG.info(message);
      throw new InvalidPurchaseException(message);
    }

    var numberOfTickets =
        Arrays.stream(ticketTypeRequests)
            .mapToInt(TicketTypeRequest::getNoOfTickets)
            .reduce(0, Integer::sum);

    if (numberOfTickets > 20) {
      var message =
          String.format(
              "Total ordered tickets (%s), exceeds maximum allowed (%s).",
              numberOfTickets, MAX_TICKETS_ALLOWED);
      LOG.info(message);
      throw new InvalidPurchaseException(message);
    }
  }
}

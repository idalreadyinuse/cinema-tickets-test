package uk.gov.dwp.uc.pairtest;

import static uk.gov.dwp.uc.pairtest.service.CreateOrderService.ORDER_COST_LABEL;
import static uk.gov.dwp.uc.pairtest.service.CreateOrderService.TOTAL_SEATS_LABEL;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  @Override
  public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
      throws InvalidPurchaseException {

    if (accountId == null || accountId <= 0L) {
      var msg = "Account ID is invalid";
      LOG.info(msg);
      throw new InvalidPurchaseException(msg);
    }

    var ticketMap =
        Stream.of(ticketTypeRequests)
            .collect(
                Collectors.toMap(
                    TicketTypeRequest::getTicketType, TicketTypeRequest::getNoOfTickets));

    var orderSummary = createOrderService.createOrderSummary(ticketMap);

    var seatsToReserve = orderSummary.get(TOTAL_SEATS_LABEL);
    var orderCost = orderSummary.get(ORDER_COST_LABEL);

    LOG.info(String.format("Requesting payment for account ID %s", accountId));
    ticketPaymentService.makePayment(accountId, orderCost);

    LOG.info(String.format("Reserving %s seats for account ID %s", seatsToReserve, accountId));
    seatReservationService.reserveSeat(accountId, seatsToReserve);
  }
}

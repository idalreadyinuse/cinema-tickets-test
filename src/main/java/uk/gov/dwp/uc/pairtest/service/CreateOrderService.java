package uk.gov.dwp.uc.pairtest.service;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.CHILD;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class CreateOrderService {

  private static final Logger LOG = Logger.getLogger(CreateOrderService.class.getName());

  private static final int ADULT_TICKET_COST = 20;
  private static final int CHILD_TICKET_COST = 10;
  private static final int INFANT_TICKET_COST = 0;
  public static final String TOTAL_SEATS_LABEL = "Seats";
  public static final String ORDER_COST_LABEL = "TotalCost";

  public Map<String, Integer> createOrderSummary(TicketTypeRequest... ticketTypeRequests) {

    var ticketMap =
        Arrays.stream(ticketTypeRequests)
            .collect(
                Collectors.toMap(
                    TicketTypeRequest::getTicketType, TicketTypeRequest::getNoOfTickets));

    Map<String, Integer> orderSummary = new HashMap<>();

    final int adultTickets = ticketMap.get(ADULT) == null ? 0 : ticketMap.get(ADULT);
    final int childTickets = ticketMap.get(CHILD) == null ? 0 : ticketMap.get(CHILD);
    final int infantTickets = ticketMap.get(INFANT) == null ? 0 : ticketMap.get(INFANT);

    checkAdultPresent(adultTickets, childTickets, infantTickets);
    checkAdultToInfantRatio(adultTickets, infantTickets);

    var totalCost =
        (adultTickets * ADULT_TICKET_COST)
            + (childTickets * CHILD_TICKET_COST)
            + (infantTickets * INFANT_TICKET_COST);

    var seatsToReserve = adultTickets + childTickets;

    if (seatsToReserve == 0 || totalCost == 0) {
      var message = "Seats to be reserved or order cost cannot be zero.";
      LOG.info(message);
      throw new InvalidPurchaseException(message);
    } else {
      orderSummary.put(TOTAL_SEATS_LABEL, seatsToReserve);
      orderSummary.put(ORDER_COST_LABEL, totalCost);
    }

    return orderSummary;
  }

  private void checkAdultPresent(int adultTickets, int childTickets, int infantTickets) {
    if ((childTickets > 0 || infantTickets > 0) && adultTickets == 0) {
      var message =
          "Cannot purchase child or infant tickets without also purchasing an adult ticket";
      LOG.info(message);
      throw new InvalidPurchaseException(message);
    }
  }

  private void checkAdultToInfantRatio(int adultTickets, int infantTickets) {
    if (infantTickets > adultTickets) {
      var message = "Cannot have more infant tickets than adult tickets";
      LOG.info(message);
      throw new InvalidPurchaseException(message);
    }
  }
}

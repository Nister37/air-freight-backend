package be.kdg.airfreight.loadplanning.core;

import be.kdg.airfreight.common.*;
import be.kdg.airfreight.loadplanning.domain.*;
import be.kdg.airfreight.loadplanning.port.out.LoadPolicyLoadPort;
import be.kdg.airfreight.loadplanning.port.out.AircraftLoadPort;
import be.kdg.airfreight.loadplanning.port.out.FlightRequestPort;
import be.kdg.airfreight.loadplanning.port.out.LoadLoadPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoadReaderTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 11);
    private static final LocalDateTime STD = DATE.atTime(15, 0);
    private static final LocalDateTime NOW = DATE.atTime(10, 0);
    private static final FlightId FLIGHT_ID = new FlightId(new FlightNumber("HJ221"), DATE, new Airport("BRU"));
    private static final AircraftRegistration REGISTRATION = new AircraftRegistration("SXBAA");
    private static final FloorPlan FLOOR_PLAN = new MainDeckFloorPlan(
            Stream.of("C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "P", "R", "S", "T", "U").map(Slot::new).toList(),
            List.of(new Slot("A1"), new Slot("A2"), new Slot("B1")), List.of(new Slot("X")), new Slot("R"));
    private final LoadLoadPort loads = mock(LoadLoadPort.class);
    private final FlightRequestPort flights = mock(FlightRequestPort.class);
    private final LoadPolicyLoadPort policy = mock(LoadPolicyLoadPort.class);
    private final AircraftLoadPort aircraft = mock(AircraftLoadPort.class);
    private final LoadReader reader = new LoadReader(loads, flights, new LoadPolicyReader(policy));

    @Test
    void queriesUseCurrentFlightAndPolicyWithoutChangingTheLoadedAggregate() {
        Load load = checkedLoad();
        LoadFacts stored = load.facts();
        when(loads.load(FLIGHT_ID)).thenReturn(Optional.of(load));
        when(flights.findFlightFacts(FLIGHT_ID)).thenReturn(Optional.of(facts(REGISTRATION, "ATH", STD.plusMinutes(30))));
        when(policy.load()).thenReturn(Optional.of(new LoadPolicy(Severity.HARD,
                600, 30, new BigDecimal("10"))));

        LoadFacts current = new DefaultGetLoadQuery(reader).getLoad(FLIGHT_ID);

        assertEquals(STD.minusMinutes(600), current.loadingWindowOpensAt());
        assertEquals(STD, current.loadingWindowClosesAt());
        assertTrue(current.checks().isEmpty());
        assertEquals(stored, load.facts());

        AircraftRegistration replacement = new AircraftRegistration("SXBBB");
        when(flights.findFlightFacts(FLIGHT_ID)).thenReturn(Optional.of(facts(replacement, "FRA", STD.plusMinutes(30))));
        when(aircraft.floorPlanFor(replacement)).thenReturn(Optional.of(FLOOR_PLAN));

        LoadPlan plan = new DefaultGetLoadPlanQuery(reader, aircraft).getLoadPlan(FLIGHT_ID);

        assertEquals(replacement, plan.load().aircraftRegistration());
        assertEquals(new Airport("FRA"), plan.load().destinationAirport());
        assertEquals(2, plan.load().ulds().size());
        assertTrue(plan.load().placements().isEmpty());
        assertTrue(plan.load().blockages(FLOOR_PLAN).isEmpty());
        assertTrue(plan.load().facts().ulds().stream().allMatch(uld -> uld.position() == null));
        assertEquals(stored, load.facts());
        assertTrue(load.getDomainEvents().isEmpty());
    }

    @Test
    void finalLoadQueriesKeepTheFrozenDataAfterFlightAndPolicyChanges() {
        Load load = checkedLoad();
        load.finalise(false, FLOOR_PLAN, LoadChecks.standard(new LoadRules(Severity.HARD, new BigDecimal("10"))), NOW);
        LoadFacts frozen = load.facts();
        var events = load.getDomainEvents();
        when(loads.load(FLIGHT_ID)).thenReturn(Optional.of(load));
        when(flights.findFlightFacts(FLIGHT_ID)).thenReturn(Optional.of(facts(new AircraftRegistration("SXBBB"), "FRA", STD.plusHours(1))));
        when(policy.load()).thenReturn(Optional.of(new LoadPolicy(Severity.SOFT,
                600, 30, new BigDecimal("5"))));
        when(aircraft.floorPlanFor(REGISTRATION)).thenReturn(Optional.of(FLOOR_PLAN));

        LoadFacts current = new DefaultGetLoadQuery(reader).getLoad(FLIGHT_ID);
        LoadPlan plan = new DefaultGetLoadPlanQuery(reader, aircraft).getLoadPlan(FLIGHT_ID);

        assertEquals(frozen, current);
        assertEquals(frozen, plan.load().facts());
        assertEquals(LoadStatus.FINAL, plan.load().status());
        assertEquals(frozen, load.facts());
        assertEquals(events, load.getDomainEvents());
    }

    private Load checkedLoad() {
        Load load = Load.open(LoadId.create(), FLIGHT_ID, REGISTRATION, new Airport("ATH"),
                LoadingWindow.beforeDeparture(STD, 720, 20));
        List<String> codes = List.of("AKE11111AA", "AKE22222BB");
        List<String> positions = List.of("CL", "CR");
        for (int i = 0; i < codes.size(); i++) {
            ULDCode code = new ULDCode(codes.get(i));
            load.book(ULD.of(code, new Weight(new BigDecimal("1000"), WeightUnit.KG), new Airport("ATH")), NOW);
            load.place(code, FLOOR_PLAN.find(positions.get(i)).orElseThrow(), FLOOR_PLAN, NOW);
        }
        load.check(FLOOR_PLAN, LoadChecks.standard(new LoadRules(Severity.HARD, new BigDecimal("10"))), NOW);
        return load;
    }

    private FlightFacts facts(AircraftRegistration registration, String destination, LocalDateTime etd) {
        return new FlightFacts(FLIGHT_ID, new Airport(destination), registration, STD, STD.plusHours(2),
                etd, etd.plusHours(2), null, null, FlightStatus.SCHEDULED, false, List.of());
    }
}

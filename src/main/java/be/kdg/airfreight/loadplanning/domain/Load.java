package be.kdg.airfreight.loadplanning.domain;

import be.kdg.airfreight.common.AircraftRegistration;
import be.kdg.airfreight.common.Airport;
import be.kdg.airfreight.common.FlightId;
import be.kdg.airfreight.common.events.DomainEvent;
import be.kdg.airfreight.common.PlacedUld;
import be.kdg.airfreight.common.events.LoadFinalisedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;


public final class Load {

    private final LoadId loadId;
    private final FlightId flightId;
    private AircraftRegistration aircraftRegistration;
    private Airport destinationAirport;
    private LoadingWindow loadingWindow;
    private LoadStatus status;
    private final List<ULD> ulds;
    private final List<ULDPlacement> placements;
    private final List<LoadCheck> checks;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Load(LoadId loadId, FlightId flightId, AircraftRegistration aircraftRegistration,
                 Airport destinationAirport, LoadingWindow loadingWindow, LoadStatus status,
                 List<ULD> ulds, List<ULDPlacement> placements, List<LoadCheck> checks){
        this.loadId = Objects.requireNonNull(loadId, "load id is required");
        this.flightId = Objects.requireNonNull(flightId, "flight id is required");
        this.aircraftRegistration = Objects.requireNonNull(aircraftRegistration, "aircraft registration is required");
        this.destinationAirport = Objects.requireNonNull(destinationAirport, "destination is required");
        this.loadingWindow = Objects.requireNonNull(loadingWindow, "loading window is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.ulds = new ArrayList<>(ulds);
        this.placements = new ArrayList<>(placements);
        this.checks = new ArrayList<>(checks);
    }

//    new load for completly new flight
    public static Load open(LoadId loadId, FlightId flightId, AircraftRegistration aircraftRegistration,
                            Airport destinationAirport, LoadingWindow loadingWindow) {
        return new Load(loadId, flightId, aircraftRegistration, destinationAirport, loadingWindow,
                LoadStatus.OPEN, List.of(), List.of(), List.of());
    }

//    rebuild from database
    public static Load restore(LoadId loadId, FlightId flightId, AircraftRegistration aircraftRegistration,
                               Airport destinationAirport, LoadingWindow loadingWindow, LoadStatus status,
                               List<ULD> ulds, List<ULDPlacement> placements, List<LoadCheck> checks) {
        return new Load(loadId, flightId, aircraftRegistration, destinationAirport, loadingWindow,
                status, ulds, placements, checks);
    }

    public void updateFlightFacts(Airport destination, AircraftRegistration registration, LoadingWindow window){
        Objects.requireNonNull(destination);
        Objects.requireNonNull(registration);
        Objects.requireNonNull(window);
        if (status == LoadStatus.FINAL) {
            return;  //finalized load is history
        }
        if(!registration.equals(aircraftRegistration)){
            placements.clear();  // if different aircraft -> different floor plan, old plan is meaningless
        }

        this.aircraftRegistration = registration;
        this.destinationAirport = destination;
        this.loadingWindow = window;
        checks.clear();
    }

    public void book(ULD uld, LocalDateTime now){
        Objects.requireNonNull(uld);
        requireOpen();
        if(loadingWindow.isClosedAt(now)){
            throw new IllegalStateException("Loading window closed at "+ loadingWindow.closesAt());
        }

        Optional<ULD> exisiting = findUld(uld.code());
        if(exisiting.isPresent()){
            if(exisiting.get().hasSameDataAs(uld)){
                return;
            }
            throw new  IllegalStateException("ULD " + uld.code().value() + " is already booked with different data");
        }
        ulds.add(uld);
        checks.clear();
    }

    public ULDStatus statusOf(ULDCode code){
        if (findUld(code).isEmpty()){
            throw new IllegalArgumentException("ULD " + code.value() + " is not booked on this load");
        }
        return isPlaced(code) ? ULDStatus.PLACED : ULDStatus.BOOKED;
    }

    // placing
    // Puts a booked ULD on a free position of this aircraft. Only while the load is OPEN and the loading window is open.
        public void place(ULDCode code, Position position,FloorPlan floorPlan,LocalDateTime now){
        Objects.requireNonNull(code);
        Objects.requireNonNull(position);
        Objects.requireNonNull(floorPlan);
        requireOpen();
        requireWindowOpen(now);

        if (findUld(code).isEmpty()) {
            throw new IllegalArgumentException("ULD " + code.value() + " is not booked on this load");
        }
        if (!floorPlan.contains(position)) {
            throw new IllegalArgumentException("Position " + position.code() + " does not exist on this aircraft");
        }
        if (isPlaced(code)) {
            throw new IllegalStateException("ULD " + code.value() + " is already placed");
        }
        if (isOccupied(position)) {
            throw new IllegalStateException("Position " + position.code() + " is already occupied");
        }

        placements.add(new ULDPlacement(code, position));
        checks.clear();
    }
    // Takes a ULD off its position; the ULD stays booked. Same window/status rules as place.
    public void removePlacement(ULDCode code, LocalDateTime now) {
        Objects.requireNonNull(code);
        requireOpen();
        requireWindowOpen(now);

        boolean removed = placements.removeIf(p -> p.code().equals(code));
        if (!removed) {
            throw new IllegalStateException("ULD " + code.value() + " is not placed");
        }
        checks.clear();
    }

    // CHECKS
    //Runs all load checks against current placements and stores the results. Read-only for the planner; doesn't block anything by itself.
    public List<LoadCheck> check(LoadRules rules){
        Objects.requireNonNull(rules);
        List<LoadCheck> results = List.of(
                destinationCheck(),
                lateralBalanceCheck(rules),
                offloadingCheck(rules)
        );
        checks.clear();
        checks.addAll(results);
        return results;
    }
    //every placed ULD must be going to this flight's destination.
    private LoadCheck destinationCheck() {
        List<String> wrong = placedUldObjects().stream()
                .filter(u -> !u.destination().equals(destinationAirport))
                .map(u -> u.code().value())
                .toList();
        return wrong.isEmpty()
                ? LoadCheck.passed(CheckKind.DESTINATION_MATCH, Severity.HARD,
                "All placed ULDs go to " + destinationAirport.value())
                : LoadCheck.failed(CheckKind.DESTINATION_MATCH, Severity.HARD,
                "Wrong destination: " + String.join(", ", wrong));
    }

    //left/right weight difference must stay within the policy's max percentage. NOSE/TAIL are centreline and ignored.
    private LoadCheck lateralBalanceCheck(LoadRules rules) {
        BigDecimal left = weightOnSide(Side.LEFT);
        BigDecimal right = weightOnSide(Side.RIGHT);
        BigDecimal total = left.add(right);

        if (total.signum() == 0) {
            return LoadCheck.passed(CheckKind.LATERAL_BALANCE, Severity.HARD, "No lateral weight");
        }

        BigDecimal differencePercent = left.subtract(right).abs()
                .multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
        String detail = "Lateral difference " + differencePercent + "% (max "
                + rules.maxLateralDifferencePercent() + "%)";

        return differencePercent.compareTo(rules.maxLateralDifferencePercent()) <= 0
                ? LoadCheck.passed(CheckKind.LATERAL_BALANCE, Severity.HARD, detail)
                : LoadCheck.failed(CheckKind.LATERAL_BALANCE, Severity.HARD, detail);
    }

    //Booked ULDs that aren't placed will be left behind. Severity (HARD/SOFT) comes from the policy.
    private LoadCheck offloadingCheck(LoadRules rules) {
        List<String> unplaced = ulds.stream()
                .filter(u -> !isPlaced(u.code()))
                .map(u -> u.code().value())
                .toList();
        return unplaced.isEmpty()
                ? LoadCheck.passed(CheckKind.OFFLOADING, rules.offloadingSeverity(), "All booked ULDs are placed")
                : LoadCheck.failed(CheckKind.OFFLOADING, rules.offloadingSeverity(),
                "Will be offloaded: " + String.join(", ", unplaced));
    }


    //finalise
   //Locks the load: re-runs checks, refuses on HARD failures, requires confirmation for SOFT failures, then marks FINAL and records LoadFinalisedEvent.
    public void finalise(boolean confirmWarnings, LoadRules rules, LocalDateTime now) {
        Objects.requireNonNull(rules);
        requireOpen();
        if (loadingWindow.isClosedAt(now)) {
            throw new IllegalStateException("Loading window closed at " + loadingWindow.closesAt());
        }

        List<LoadCheck> results = check(rules);   // always re-run — never trust stored results

        List<String> blocking = results.stream().filter(LoadCheck::isBlocking).map(LoadCheck::explanation).toList();
        if (!blocking.isEmpty()) {
            throw new IllegalStateException("Cannot finalise: " + String.join("; ", blocking));
        }

        List<String> warnings = results.stream().filter(LoadCheck::isWarning).map(LoadCheck::explanation).toList();
        if (!warnings.isEmpty() && !confirmWarnings) {
            throw new IllegalStateException("Confirm warnings to finalise: " + String.join("; ", warnings));
        }

        status = LoadStatus.FINAL;
        domainEvents.add(new LoadFinalisedEvent(UUID.randomUUID(), now, flightId, placedUlds()));
    }
    //Converts placements into the shared PlacedUld contract used in LoadFinalisedEvent.
    private List<PlacedUld> placedUlds() {
        return placements.stream()
                .map(p -> {
                    ULD uld = findUld(p.code()).orElseThrow();
                    return new PlacedUld(
                            uld.code().value(),
                            p.position().code(),
                            uld.weight().amount(),
                            uld.weight().unit().name(),
                            uld.destination());
                })
                .toList();
    }


    private BigDecimal weightOnSide(Side side) {
        return placements.stream()
                .filter(p -> p.position().side() == side)
                .map(p -> findUld(p.code()).orElseThrow().weight().toKg())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ULD> placedUldObjects() {
        return ulds.stream().filter(u -> isPlaced(u.code())).toList();
    }


    private boolean isOccupied(Position position) {
        return placements.stream().anyMatch(p -> p.position().equals(position));
    }

    private void requireWindowOpen(LocalDateTime now) {
        if (!loadingWindow.isOpenAt(now)) {
            throw new IllegalStateException("Loading window is not open (" + loadingWindow.opensAt()
                    + " - " + loadingWindow.closesAt() + ")");
        }
    }


    private void requireOpen(){
        if (status != LoadStatus.OPEN) {
            throw new IllegalStateException("Load for flight " + flightId.key() + " is already final");
        }
    }

    private Optional<ULD> findUld(ULDCode code) {
        return ulds.stream().filter(u -> u.code().equals(code)).findFirst();
    }

    private boolean isPlaced(ULDCode code) {
        return placements.stream().anyMatch(p -> p.code().equals(code));
    }


    // DOMAIN EVENTS
    public List<DomainEvent> getDomainEvents(){
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents(){
        domainEvents.clear();
    }

    public LoadId loadId() { return loadId; }
    public FlightId flightId() { return flightId; }
    public AircraftRegistration aircraftRegistration() { return aircraftRegistration; }
    public Airport destinationAirport() { return destinationAirport; }
    public LoadingWindow loadingWindow() { return loadingWindow; }
    public LoadStatus status() { return status; }
    public List<ULD> ulds() { return List.copyOf(ulds); }
    public List<ULDPlacement> placements() { return List.copyOf(placements); }
    public List<LoadCheck> checks() { return List.copyOf(checks); }
}

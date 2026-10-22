# Extending the backend

The examples below show possible extensions, not additional assignment requirements. Imports are omitted; add the classes in the same packages as the existing implementations.

## Other load checks

1. Add a class in `loadplanning/domain` that implements [LoadCheckRule](../src/main/java/be/kdg/airfreight/loadplanning/domain/LoadCheckRule.java).
2. Add its name to `CheckKind` and implement `check(LoadView, FloorPlan, LoadRules)`. Return `LoadCheck.passed(...)` or `LoadCheck.failed(...)` with the severity and a clear explanation.
3. Add an instance to the list in `LoadChecks.standard(...)`. All existing checking and finalisation use cases use this list. A separate Spring bean is unnecessary.
4. Add a focused test for passing and failing loads. Hard failures block finalisation; soft failures require the planner's confirmation.

If the check needs a new configurable setting, also extend `LoadPolicy`, `LoadRules`, their persistence mapping and the policy HTTP contract.

Example: warn when no cargo has been booked. First add `NON_EMPTY_LOAD` to `CheckKind`.

```java
final class NonEmptyLoadCheckRule implements LoadCheckRule {
    @Override
    public LoadCheck check(LoadView load, FloorPlan floorPlan, LoadRules rules) {
        if (load.ulds().isEmpty()) {
            return LoadCheck.failed(CheckKind.NON_EMPTY_LOAD, Severity.SOFT,
                    "No cargo has been booked");
        }
        return LoadCheck.passed(CheckKind.NON_EMPTY_LOAD, Severity.SOFT,
                "Cargo has been booked");
    }
}
```

Keep the existing checks when extending `LoadChecks.standard(...)`:

```java
public static LoadChecks standard(LoadRules rules) {
    return new LoadChecks(List.of(
            new LateralBalanceCheckRule(),
            new OffloadingCheckRule(),
            new UnplacedCargoCheckRule(),
            new NonEmptyLoadCheckRule()), rules);
}
```

## Other floor plans

- A different arrangement of the existing main-deck rows, nose positions, tail positions and door can use `MainDeckFloorPlan`. Store the layout in `lp_aircraft_type` and associate a registration through `lp_aircraft`.
- For different layout behaviour, implement [FloorPlan](../src/main/java/be/kdg/airfreight/loadplanning/domain/FloorPlan.java): list its positions, recognise valid positions, find a position by code and supply routes to the door.
- `routesToDoor(...)` returns alternative routes. Each route lists every position that must be clear, including manoeuvring positions, and excludes the ULD's own position. At least one route must be clear of cargo for another destination.
- Select the implementation in `AircraftPersistenceAdapter`. Update `LoadPlanResponse` and `FloorPlanResponse` if the HTTP layout shape changes, and update the OpenAPI specification.

Example: reuse the existing layout behaviour with three rows and the door at row D. In the adapter, these values come from the aircraft-type row in the database.

```java
FloorPlan floorPlan = new MainDeckFloorPlan(
        List.of(new Slot("C"), new Slot("D"), new Slot("E")),
        List.of(new Slot("A1")),
        List.of(new Slot("X")),
        new Slot("D"));

Position from = floorPlan.find("EL").orElseThrow();
List<List<Position>> routes = floorPlan.routesToDoor(from);
// One route: [DL]. EL is excluded because it is the ULD's own position.
```

This changes the arrangement. A layout with different unloading behaviour needs its own `FloorPlan` implementation and adapter mappings described above.

## Other kinds of positions

1. Implement [Position](../src/main/java/be/kdg/airfreight/loadplanning/domain/Position.java) with a unique code and its left/right weight contributions in kilograms. Use `Weight.toKg()`; return zero for a side to which the position does not contribute.
2. Include the position in its floor plan and define its unloading routes there. The planner must select a position recognised by that floor plan.
3. Extend `LoadPersistenceAdapter` to save and restore the new position. It currently stores `MainDeckPosition` as a side and slot. Adjust the database fields if the new position needs different data.
4. Test valid placement, occupied positions, balance contributions and unloading routes.

The load's placement rules and balance check already use the interfaces. Layout-specific rules belong in the floor plan and position implementations.

Example: a hypothetical centre position that contributes half its weight to each side. Its floor plan must include it and define its route to the door; persistence must also recognise it.

```java
public record CentrePosition(Slot slot) implements Position {
    public CentrePosition {
        Objects.requireNonNull(slot, "slot is required");
    }

    @Override
    public String code() { return slot.value() + "C"; }

    @Override
    public BigDecimal leftWeightInKg(Weight weight) {
        return weight.toKg().divide(BigDecimal.valueOf(2));
    }

    @Override
    public BigDecimal rightWeightInKg(Weight weight) {
        return weight.toKg().divide(BigDecimal.valueOf(2));
    }
}
```

## Other MVT message types and validation rules

1. Add an `MVT_...` value to `MessageKind` and implement [MvtMessageHandler](../src/main/java/be/kdg/airfreight/partnermessaging/adapter/out/messaging/MvtMessageHandler.java). Supply a unique two-letter code, its kind, airport selection, parsing and formatting.
2. Put the type's body validation in `parse(...)` and validate values needed for formatting in `format(...)`. Reject invalid values with `IllegalArgumentException`. `MvtTelegramFormatter` handles the common header and delegates the body to the selected handler.
3. Register the handler in the list in `PartnerRabbitConfiguration.mvtTelegramFormatter()`. Reuse `TimedMvtMessageHandler` only when its UTC `HHmm` format and date rules fit. A separate bean per handler is unnecessary.
4. Add a routing key in `PartnerRabbitTopology` and map it in `PartnerMessageRabbitPublisherAdapter.routingKey(...)`. Connect the new kind to its business operation when it must be produced.
5. Test valid messages, invalid bodies and formatting/parsing around date boundaries.

Example: `XX` is a placeholder code and `/HHmm` is an invented body format; replace them with the agreed message specification. Add `MVT_XX` to `MessageKind`. The handler checks its own prefix and reuses the existing time validation and date resolution.

```java
public class ExampleMvtMessageHandler implements MvtMessageHandler {
    private final TimedMvtMessageHandler time = new TimedMvtMessageHandler(
            "XX", MessageKind.MVT_XX, flight -> flight.id().departureAirport());

    @Override
    public String code() { return "XX"; }

    @Override
    public MessageKind kind() { return MessageKind.MVT_XX; }

    @Override
    public Airport airport(FlightFacts flight) {
        return flight.id().departureAirport();
    }

    @Override
    public LocalDateTime parse(String body, LocalDateTime readAt) {
        if (!body.startsWith("/")) {
            throw new IllegalArgumentException("Expected / before the MVT time");
        }
        return time.parse(body.substring(1), readAt);
    }

    @Override
    public String format(LocalDateTime movementTime) {
        return "/" + time.format(movementTime);
    }
}
```

Keep the existing handlers when updating `PartnerRabbitConfiguration.mvtTelegramFormatter()`:

```java
return new MvtTelegramFormatter(List.of(
        new TimedMvtMessageHandler("ED", MessageKind.MVT_ED, flight -> flight.id().departureAirport()),
        new TimedMvtMessageHandler("EA", MessageKind.MVT_EA, FlightFacts::arrivalAirport),
        new TimedMvtMessageHandler("AD", MessageKind.MVT_AD, flight -> flight.id().departureAirport()),
        new TimedMvtMessageHandler("AA", MessageKind.MVT_AA, FlightFacts::arrivalAirport),
        new ExampleMvtMessageHandler()));
```

For publication, add the matching constant and switch case in the two classes from step 4, then connect a business operation that produces `MVT_XX`:

```java
// In PartnerRabbitTopology:
public static final String MVT_XX = "mvt.xx";

// In the existing PartnerMessageRabbitPublisherAdapter.routingKey(...) switch:
case MVT_XX -> PartnerRabbitTopology.MVT_XX;
```

package be.kdg.airfreight.loadplanning.port.in.usecase;

import be.kdg.airfreight.loadplanning.domain.LoadPolicy;

public interface GetLoadPolicyQuery {
    LoadPolicy currentPolicy();
}

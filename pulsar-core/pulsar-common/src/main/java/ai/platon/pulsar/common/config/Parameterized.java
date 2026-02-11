package ai.platon.pulsar.common.config;

public interface Parameterized {
    default Params getParams() {
        return Params.EMPTY_PARAMS;
    }
}

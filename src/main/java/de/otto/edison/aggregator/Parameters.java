package de.otto.edison.aggregator;

import com.google.common.collect.ImmutableMap;

public final class Parameters {

    private static final Parameters EMPTY_PARAMETERS = new Parameters(ImmutableMap.of());

    private final ImmutableMap<String, Object> params;

    public Parameters(final ImmutableMap<String, Object> params) {
        this.params = params;
    }

    public static Parameters emptyParameters() {
        return EMPTY_PARAMETERS;
    }

    public static Parameters from(final ImmutableMap<String, Object> params) {
        return new Parameters(params);
    }

    public String getString(final String key) {
        return params.containsKey(key) ? params.get(key).toString() : null;
    }

}

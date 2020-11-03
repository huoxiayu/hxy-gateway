package com.hxy.route;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Router implements Route {

    @Override
    public String route(List<String> endpoints) {
        int idx = Math.abs(ThreadLocalRandom.current().nextInt()) % endpoints.size();
        return endpoints.get(idx);
    }

}

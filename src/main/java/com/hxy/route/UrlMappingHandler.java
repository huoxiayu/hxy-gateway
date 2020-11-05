package com.hxy.route;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class UrlMappingHandler {

    private final Router router = new Router();

    public String mapping(FullHttpRequest fullRequest) {
        String uri = fullRequest.uri();
        List<String> endpoints = findAllEndpoints(uri);
        String mappingUri = router.route(endpoints) + uri;
        log.debug("mapping {} -> {}", uri, mappingUri);
        return mappingUri;
    }

    private List<String> findAllEndpoints(String uri) {
        Preconditions.checkArgument(StringUtils.isNotBlank(uri), "empty request url");

        if ("/api/hello".equals(uri)) {
            return ImmutableList.of("http://localhost:8088", "http://localhost:8088");
        }

        log.error("not recognized url {}", uri);
        throw new NotImplementedException("dynamic route not implement");
    }

}

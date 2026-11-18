package com.guvi.gateway.gateway;

import java.util.Enumeration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.guvi.gateway.config.AppProperties;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/gateway")
public class GatewayController {
    private final RestClient restClient;
    private final AppProperties properties;

    public GatewayController(RestClient restClient, AppProperties properties) {
        this.restClient = restClient; this.properties = properties;
    }

    @Operation(
    summary = "Route authenticated API requests",
    security = @SecurityRequirement(name = "bearerAuth")
)

    @RequestMapping(value = "/{service}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<String> route(@PathVariable String service, HttpServletRequest request,
                                        @RequestBody(required = false) String body) {
        if (!service.equals("users") && !service.equals("products") && !service.equals("orders")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown gateway service");
        }
        String path = request.getRequestURI().substring("/gateway".length());
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        String target = properties.routing().baseUrl() + "/internal" + path + query;
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!name.equalsIgnoreCase("host") && !name.equalsIgnoreCase("content-length")
                    && !name.equalsIgnoreCase("authorization")) headers.set(name, request.getHeader(name));
        }
        try {
            return restClient.method(method).uri(target).headers(h -> h.addAll(headers))
                    .body(body == null ? "" : body).retrieve()
                    .toEntity(String.class);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Backend service unavailable");
        }
    }
}

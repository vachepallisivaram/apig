package com.otsi.retail.gateway.authConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

	public static final List<String> nonSecureRoutes=Arrays.asList(
			"/auth/temporary-login",
            "/auth/signup",
            "/auth/confirmEmail",
            "/v3/api-docs",
            "/client/create-client",
            "/plan/getplandetails",
            "plan/getplandetailsByTenure",
            "auth/confirmforgetPassword",
            "/auth/resetUserPassword",
            "/auth/auth-challenge",
            "/auth/create-user",
            "/razorpay/transaction-callback",
            "/paymentgateway/create_creditdebit_order",
            "/paymentgateway/create_client_order"

            
    );	
	//
	 public Predicate<ServerHttpRequest> isSecured =
	            request -> nonSecureRoutes
	                    .stream()
	                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}

package com.otsi.retail.gateway.authConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.otsi.retail.gateway.repository.RequestDataRepository;
import com.otsi.retail.gateway.util.AES;
import com.otsi.retail.gateway.util.ContentLogging;
import com.otsi.retail.gateway.util.RSAUtil;

import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

	@Value("${encryption.enable:}")
	private Boolean encryptionEnabled;

	private Logger log = LogManager.getLogger(AuthFilter.class);

	private static final List<MediaType> VISIBLE_TYPES = Arrays.asList(MediaType.valueOf("text/*"),
			MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
			MediaType.valueOf("application/*+json"), MediaType.valueOf("application/*+xml"),
			MediaType.MULTIPART_FORM_DATA);

	@Value("${Cognito.aws.idTokenPoolUrl}")
	private String ID_TOKEN_URL;

	@Autowired
	private RouteValidator routeValidator;

	@Autowired
	private ConfigurableJWTProcessor configurableJWTProcessor;

	@Autowired
	private RequestDataRepository requestDataRepository;

	private static final Log logger = LogFactory.getLog(AuthFilter.class);

	public AuthFilter() {
		super(Config.class);

	}

	private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {

			ServerHttpRequest request = exchange.getRequest();
			JWTClaimsSet claims = null;
			System.out.println(request.getPath());
			if (routeValidator.isSecured.test(request)) {
				if (isAuthHeaderMissing(request)) {
					logger.error("Authorization header is missing in request");
					return this.onError(exchange, "Authorization header is missing in request",
							HttpStatus.UNAUTHORIZED);
				}

				final String token = this.getAuthHeader(request);

				try {
					claims = getCliamsFromToken(token);
				} catch (ParseException | BadJOSEException | JOSEException e) {
					logger.error("Error occurs while getting cliams from token ==>" + e.getMessage());
					return this.onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
				}
				if (isTokenInvalid(claims)) {
					logger.error("#####Token is expired#####");
					return this.onError(exchange, "JWT Token is Expired", HttpStatus.UNAUTHORIZED);
				}
				if (verifyIfIdToken(claims)) {
					logger.error("JWT Token is not an ID Token");

					return this.onError(exchange, "JWT Token is not an ID Token", HttpStatus.UNAUTHORIZED);
				}
				if (validateIssuer(claims)) {
					logger.error("Issuer  does not match cognito idp ");

					return this.onError(exchange, "Issuer  does not match cognito idp ", HttpStatus.UNAUTHORIZED);
				}

				this.populateRequestWithHeaders(exchange, token, claims);

			}
			if (encryptionEnabled != null && encryptionEnabled) {
				HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
				if (httpHeaders.containsKey("Content-Type")
						&& httpHeaders.get("Content-Type").contains("application/json")) {
					String encKey = httpHeaders.getFirst("enc-key");
					if (StringUtils.isEmpty(encKey)) {
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request received");
					}
					try {
						RSAUtil.decrypt(encKey);
					} catch (Exception ex) {
						logger.info("exception occured while decrypting symmetry key:" + ex);
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request not validated");
					}
					ContentLogging requestData = new ContentLogging();
					try {
						logRequestData(exchange, requestData);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					ModifyRequestBodyGatewayFilterFactory.Config modifyRequestConfig = new ModifyRequestBodyGatewayFilterFactory.Config()
							.setContentType(ContentType.APPLICATION_JSON.getMimeType())
							.setRewriteFunction(String.class, String.class, (exchange1, originalRequestBody) -> {
								String modifiedRequestBody = StringUtils.EMPTY;
								if (StringUtils.isNotBlank(originalRequestBody)) {
									modifiedRequestBody = AES.decrypt(originalRequestBody);
								}
								if (requestData.getId() != null) {
									Optional<ContentLogging> contentLoggingOptional = requestDataRepository
											.findById(requestData.getId());
									if (contentLoggingOptional.isPresent()) {
										ContentLogging contentLogging = contentLoggingOptional.get();
										contentLogging.setPayload(modifiedRequestBody);
										contentLogging.setEncryptedPayload(originalRequestBody);
										requestDataRepository.save(contentLogging);
									}
								}
								return StringUtils.isNotEmpty(modifiedRequestBody) ? Mono.just(modifiedRequestBody)
										: Mono.empty();
							});
					System.out.println("request data:" + requestData);
					Mono<Void> modifiedRequestBody = new ModifyRequestBodyGatewayFilterFactory()
							.apply(modifyRequestConfig).filter(exchange, chain);
					requestDataRepository.save(requestData);
					return modifiedRequestBody;
				}
			}
			return chain.filter(exchange);
		};
	}

	private ContentLogging logRequestData(ServerWebExchange exchange, ContentLogging contentLogging)
			throws UnknownHostException {
		ServerHttpRequest serverHttpRequest = exchange.getRequest();
		Map<String, List<String>> httpHeaders = serverHttpRequest.getHeaders();
		contentLogging.setHeaders(httpHeaders);
		contentLogging.setRequestUrl(serverHttpRequest.getURI().getPath());
		contentLogging.setRequestURI(serverHttpRequest.getURI().toString());
		if (serverHttpRequest.getQueryParams() != null) {

			ObjectMapper obj = new ObjectMapper();
			JsonNode jsonValue = obj.convertValue(serverHttpRequest.getQueryParams(), JsonNode.class);
			contentLogging.setParams(jsonValue);
		}
		contentLogging.setRemoteIp(serverHttpRequest.getRemoteAddress().toString());
		InetAddress inetAddress = InetAddress.getLocalHost();
		if (inetAddress != null) {
			contentLogging.setInetIp(inetAddress.getHostAddress());
		}
		contentLogging.setMethodType(serverHttpRequest.getMethodValue());
		return contentLogging;
	}

	private DataBuffer stringBuffer(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
		DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
		buffer.write(bytes);
		return buffer;
	}

	private String getRequestBody(ServerHttpRequest request) {

		Flux<DataBuffer> dataFlux = request.getBody();

		StringBuffer sb = new StringBuffer();

		Flux<byte[]> fdfd = dataFlux.log().flatMap(dataBuffer -> {
			System.out.println("Thread - " + Thread.currentThread().getName() + ": readable count is  "
					+ dataBuffer.readableByteCount());
			byte[] bytes = new byte[dataBuffer.readableByteCount()];
			dataBuffer.read(bytes);
			DataBufferUtils.release(dataBuffer);
			return Mono.just(bytes);
		});

		fdfd.subscribe(s -> sb.append(new String(s)));
		/*
		 * Flux<DataBuffer> body = request.getBody(); StringBuilder sb = new
		 * StringBuilder(); if (body != null) { body.subscribe(buffer -> { byte[] bytes
		 * = new byte[buffer.readableByteCount()]; buffer.read(bytes);
		 * DataBufferUtils.release(buffer); String bodyString = new String(bytes,
		 * StandardCharsets.UTF_8); sb.append(bodyString); }); } String str =
		 * sb.toString();
		 */

		String str = sb.toString();

		if (StringUtils.isNotEmpty(str)) {
			return AES.decrypt(str);
		}
		return str;
	}

	private void populateRequestWithHeaders(ServerWebExchange exchange, String token, JWTClaimsSet claims) {

		exchange.getRequest().mutate().headers(header -> {
			header.add("Username", String.valueOf(claims.getClaim("cognito:username")));
			header.add("Roles", String.valueOf(claims.getClaim("cognito:groups")));
			header.add("clientId", String.valueOf(claims.getClaim("custom:clientId1")));
			header.add("userId", String.valueOf(claims.getClaim("custom:userId")));
			if (claims.getClaim("custom:isEsSlipEnabled") != null) {
				header.add("isEsSlipEnabled", String.valueOf(claims.getClaim("custom:isEsSlipEnabled")));
			}
			if (claims.getClaim("custom:isTaxIncluded") != null) {
				header.add("isTaxIncluded", String.valueOf(claims.getClaim("custom:isTaxIncluded")));
			}

		}).build();

//			        .header("userName", String.valueOf(claims.getClaim("cognito:username")))
//				.header("roles", String.valueOf(claims.getClaim("cognito:groups")))
//				.build();
		System.out.println("-------------------->>>>>" + exchange.getRequest().getHeaders().get("Username"));
	}

	private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}

	private JWTClaimsSet getCliamsFromToken(String token) throws ParseException, BadJOSEException, JOSEException {
		return configurableJWTProcessor.process(getBearerToken(token), null);

	}

	private String getBearerToken(String token) {
		return token.startsWith("Bearer ") ? token.substring("Bearer ".length()) : token;

	}

	private boolean isTokenInvalid(JWTClaimsSet claims) {
		return claims.getExpirationTime().before(new Date());
	}

	private String getAuthHeader(ServerHttpRequest request) {
		return request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
	}

	private boolean isAuthHeaderMissing(ServerHttpRequest request) {
		return !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
	}

	private boolean verifyIfIdToken(JWTClaimsSet claims) {

		return !claims.getIssuer().equals(ID_TOKEN_URL);

	}

	private boolean validateIssuer(JWTClaimsSet claims) {

		return !claims.getIssuer().equals(ID_TOKEN_URL);

	}

	public static class Config {

	}

}

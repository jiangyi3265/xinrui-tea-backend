package com.ruoyi.web.tea;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.annotation.PostConstruct;
import com.ruoyi.common.core.redis.RedisCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * RuoYi owns authentication and the only persistent state. A private, stateless
 * compatibility engine preserves the recovered H5 contracts. A row lock covers
 * calculation, inventory, ledger and commit as one transaction.
 */
@Service
public class TeaBusinessService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final RestTemplate engine;
    private final RedisCache redis;
    @Value("${tea.engine-url:http://127.0.0.1:8091/execute}") private String engineUrl;
    @Value("${tea.engine-secret:}") private String engineSecret;
    @Value("${token.secret:}") private String jwtSecret;

    @PostConstruct
    public void validateSecrets() {
        if (engineSecret.length() < 32 || jwtSecret.length() < 32)
            throw new IllegalStateException("Configure independent TEA_ENGINE_SECRET and JWT_SECRET of at least 32 characters before startup");
        if (engineSecret.equals(jwtSecret))
            throw new IllegalStateException("Engine and administrator JWT must not share a secret");
    }

    public TeaBusinessService(JdbcTemplate jdbc, ObjectMapper json, RedisCache redis) {
        this.jdbc = jdbc;
        this.json = json;
        this.redis = redis;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(15000);
        engine = new RestTemplate(factory);
    }

    @Transactional(timeout = 25, rollbackFor = Exception.class)
    public Map<String, Object> execute(String route, Map<String, Object> params,
            String token, String method, Map<String, Object> actor) throws Exception {
        if (engineSecret.length() < 32) throw new IllegalStateException("Tea engine secret is not configured");
        String before = jdbc.queryForObject("SELECT state_json FROM tea_business_state WHERE id=1 FOR UPDATE", String.class);
        String throttleKey = null;
        boolean login = "/member/accountLogin".equals(route);
        boolean passwordChange = "/member/editPwd".equals(route) && "POST".equals(method);
        boolean payment = "/member/verificationPayPassword".equals(route)
                || (params.containsKey("pay_password") && ("/pay/balancePay".equals(route) || "/pay/pointsPayment".equals(route) || "/member/toTransfer".equals(route)));
        if (actor == null && (login || payment || passwordChange)) {
            String subject = login ? String.valueOf(params.get("phone")) : json.readTree(before).path("sessions").path(token == null ? "" : token).path("memberId").asText("unknown");
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((engineSecret + (login ? ":login:" : passwordChange ? ":password:" : ":pay:") + subject).getBytes(StandardCharsets.UTF_8));
            throttleKey = "tea:attempts:" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            Number attempts = redis.getCacheObject(throttleKey);
            if (attempts != null && attempts.intValue() >= 5) {
                Map<String,Object> denied = new LinkedHashMap<>();
                denied.put("code",0); denied.put("msg","验证尝试过多，请在十分钟后重试"); denied.put("data",java.util.Collections.emptyMap());
                return denied;
            }
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("state", json.readValue(before, Object.class));
        request.put("route", route);
        request.put("params", params);
        request.put("token", token == null ? "" : token);
        request.put("method", method);
        request.put("actor", actor);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Tea-Engine-Secret", engineSecret);
        String response = engine.postForObject(engineUrl, new HttpEntity<>(json.writeValueAsString(request), headers), String.class);
        Map<String, Object> envelope = json.readValue(response, new TypeReference<Map<String, Object>>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) envelope.get("result");
        if (result == null || !envelope.containsKey("state")) throw new IllegalStateException("Invalid engine response");
        String after = json.writeValueAsString(envelope.get("state"));
        // Engine failures and business rejections must not commit partial state.
        boolean success = Integer.valueOf(actor == null ? 1 : 200).equals(result.get("code"));
        if (throttleKey != null) {
            if (success) redis.deleteObject(throttleKey);
            else {
                // Attempts are security state, intentionally not rolled back
                // with a rejected business transaction. Row locking serializes
                // these counters across Java instances.
                Number attempts = redis.getCacheObject(throttleKey);
                redis.setCacheObject(throttleKey, attempts == null ? 1 : attempts.intValue()+1, 10, TimeUnit.MINUTES);
            }
        }
        if (success && !json.readTree(before).equals(json.readTree(after))) {
            com.fasterxml.jackson.databind.JsonNode oldClaims = json.readTree(before).path("voucherClaims");
            Iterator<Map.Entry<String,com.fasterxml.jackson.databind.JsonNode>> claims = json.readTree(after).path("voucherClaims").fields();
            while (claims.hasNext()) {
                Map.Entry<String,com.fasterxml.jackson.databind.JsonNode> claim = claims.next();
                if (!oldClaims.has(claim.getKey())) jdbc.update("INSERT INTO tea_voucher_claim(sha256,business_owner) VALUES(?,?)", claim.getKey(), claim.getValue().asText());
            }
            jdbc.update("UPDATE tea_business_state SET state_json=?, revision=revision+1, updated_at=CURRENT_TIMESTAMP WHERE id=1", after);
            jdbc.update("INSERT INTO tea_business_audit(revision, actor, route, method) SELECT revision, ?, ?, ? FROM tea_business_state WHERE id=1",
                    String.valueOf(envelope.get("subject")), route, method);
        }
        return result;
    }
}

package com.ruoyi.web.tea;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.PermissionService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
public class TeaController {
    private final TeaBusinessService business;
    private final PermissionService permission;
    private final ObjectMapper json;
    private static final Set<String> MODULES = new HashSet<>(Arrays.asList(
        "dashboard","products","orders","auctions","members","notices","warehouse",
        "settlements","fees","recharges","withdrawals","reports","ledger","bids","content"));

    public TeaController(TeaBusinessService business, PermissionService permission, ObjectMapper json) {
        this.business = business; this.permission = permission; this.json = json;
    }

    @RequestMapping(value="/admin/tea/**", method={RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> admin(HttpServletRequest request) {
        String route = request.getRequestURI().substring(request.getContextPath().length() + "/admin".length());
        String[] parts = route.split("/");
        String module = parts.length > 2 ? parts[2] : "";
        if (!MODULES.contains(module)) return error(404, "管理接口不存在");
        String action = "GET".equals(request.getMethod()) ? "list" : "POST".equals(request.getMethod()) ? "add" : "DELETE".equals(request.getMethod()) ? "remove" : "edit";
        if (!permission.hasPermi("tea:" + module + ":" + action)) return error(403, "没有此业务操作权限");
        Map<String, Object> actor = new LinkedHashMap<>();
        actor.put("userId", SecurityUtils.getUserId());
        actor.put("userName", SecurityUtils.getUsername());
        return dispatch(request, route, actor);
    }

    // Anonymous only to RuoYi's administrator JWT filter. The private engine
    // independently validates the member token on every non-public operation.
    @Anonymous
    @RequestMapping(value="/app/**", method={RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> member(HttpServletRequest request) {
        String route = request.getRequestURI().substring(request.getContextPath().length() + "/app".length());
        if (route.startsWith("/_")) return error(404, "接口不存在");
        if ("/upload/image".equals(route) || "/dg/uploadImage".equals(route)) return upload(request);
        return dispatch(request, route, null);
    }

    private ResponseEntity<?> dispatch(HttpServletRequest request, String route, Map<String, Object> actor) {
        Map<String, Object> params;
        try { params = parameters(request); }
        catch (Exception e) { return error(400, "请求参数格式错误"); }
        try {
            Map<String,Object> result = business.execute(route, params, request.getHeader("token"), request.getMethod(), actor);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Do not disclose state, passwords or service credentials in errors.
            return error(503, "业务服务暂不可用，操作未保存，请稍后重试");
        }
    }

    private Map<String,Object> parameters(HttpServletRequest request) throws Exception {
        Map<String,Object> p = new LinkedHashMap<>();
        request.getParameterMap().forEach((key,value) -> { if (value.length > 0) p.put(key,value[0]); });
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            if (request.getContentLengthLong() > 2 * 1024 * 1024) throw new IllegalArgumentException();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; int count;
            InputStream input = request.getInputStream();
            while ((count = input.read(buffer)) != -1) {
                body.write(buffer, 0, count);
                if (body.size() > 2 * 1024 * 1024) throw new IllegalArgumentException();
            }
            byte[] bytes = body.toByteArray();
            if (bytes.length > 2 * 1024 * 1024) throw new IllegalArgumentException();
            if (bytes.length > 0) p.putAll(json.readValue(bytes, new TypeReference<Map<String,Object>>() {}));
        }
        return p;
    }

    private ResponseEntity<?> upload(HttpServletRequest request) {
        try {
            Map<String,Object> auth = business.execute("/member/getMemberDetails",Collections.emptyMap(),request.getHeader("token"),"GET",null);
            if (!Integer.valueOf(1).equals(auth.get("code"))) return ResponseEntity.status(401).body(auth);
            if (!(request instanceof MultipartHttpServletRequest)) return error(400,"请选择图片");
            Iterator<MultipartFile> files = ((MultipartHttpServletRequest)request).getFileMap().values().iterator();
            if (!files.hasNext()) return error(400,"请选择图片");
            MultipartFile file = files.next();
            if (file.getSize() > 1024 * 1024 || file.isEmpty()) return error(400,"图片不能超过 1MB");
            byte[] bytes = file.getBytes();
            // Inspect dimensions before decoding pixels: a tiny compressed
            // payload must not allocate an unbounded BufferedImage.
            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) return error(400,"不支持的图片");
                ImageReader reader = readers.next();
                try {
                    reader.setInput(input,true,true);
                    int width=reader.getWidth(0), height=reader.getHeight(0);
                    if (width < 1 || height < 1 || width > 4096 || height > 4096 || (long)width * height > 8000000)
                        return error(400,"图片尺寸不能超过 4096 边长或 800 万像素");
                    if (reader.read(0) == null) return error(400,"不支持的图片");
                } finally { reader.dispose(); }
            }
            String mime = bytes.length > 8 && bytes[0] == (byte)137 && bytes[1] == 80 ? "image/png"
                : bytes.length > 3 && bytes[0] == (byte)255 && bytes[1] == (byte)216 ? "image/jpeg"
                : bytes.length > 6 && bytes[0] == 71 && bytes[1] == 73 ? "image/gif" : "";
            if (mime.isEmpty()) return error(400,"只支持 PNG、JPEG、GIF 图片");
            Map<String,Object> params = new HashMap<>();
            // Credentials/vouchers are never exposed via a public /profile URL.
            // The image is persisted with its owning business record on submit.
            params.put("image", "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes));
            return ResponseEntity.ok(business.execute("/_upload",params,request.getHeader("token"),"POST",null));
        } catch (Exception e) { return error(503,"图片处理失败，未保存"); }
    }

    private ResponseEntity<?> error(int status, String message) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("code", status); result.put("msg",message); result.put("data",Collections.emptyMap());
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(result);
    }
}

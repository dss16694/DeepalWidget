package cn.xanderye.android.deepalwidget.util;

import android.util.Log;
import cn.xanderye.android.deepalwidget.constant.Constants;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author XanderYe
 * @description:
 * @date 2023/3/8 13:44
 */
public class DeepalUtil {
    private static final String TAG = DeepalUtil.class.getSimpleName();

    private static final String DEEPAL_ORIGIN = "https://app-api.deepal.com.cn";
    private static final String CHANGAN_ORIGIN = "https://m.iov.changan.com.cn";

    private static String RSA_PUB_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtBA64g3GUwh8gAtGzanHZsWlWpsyV50a0L/D7MLRgCzWNnMicoulzpFnlz8/JAvm/xF+LHubGgMRsR3DA39ZbxoigKDsCxJOm2Ymccuasm18i0ZFgxLCbhN93Vomf7nvliWNVCd0kp4I9uWK8Logurt8TwL0XVbl/v0xVS9ML12W/Aw10JwVfRyF6Si9t0lKa/RsgnREIO6mabBdekwG20Hq5yRNjGkGKDDixHx1XE5VJ0MV4av/Toy/xgkeHgJTV6gO3DFghFxeGM6uegDe9M8XgbeXvC9U3Jnxcc5nAHxXwChOj0+FEKz6bBONEECrz/jGDEs073xL4INyyQeEjQIDAQAB";

    private static volatile String DEVICE_ID = null;
    private static volatile String KEY = null;


    public static JSONObject sessionKeyRetry(String authToken, String key) throws Exception {
        String api = "/appapi/v2/auth/sessionKeyRetry";
        String url = DEEPAL_ORIGIN + api;
        Map<String, Object> headerMap = getHeaderMap();
        headerMap.put("Authorization", authToken);
        JSONObject params = new JSONObject();
        String encrypt = encrypt(key);
        encrypt = encrypt.replace("\r\n", "");
        params.put("sessionKey", encrypt);
        Log.d(TAG, "向服务器提交sessionKey: " + key);
        HttpUtil.ResEntity resEntity = HttpUtil.doPostJSON(url, headerMap, null, params.toJSONString());
        Log.d(TAG, "提交sessionKey返回结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 用accessToken获取车辆信息
     * @param accessToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject getCarByToken(String token, String accessToken) throws IOException {
        String api = "/appapi/v1/message/msg/cars";
        String url = DEEPAL_ORIGIN + api;
        JSONObject params = new JSONObject();
        params.put("token", accessToken);
        params.put("type", 1);
        params.put("vcs-app-id", "inCall");
        Map<String, Object> headerMap = getDeepalHeaderMap(api, params.toJSONString(), null);
        headerMap.put("authorization", token);
        HttpUtil.ResEntity resEntity = HttpUtil.doPostJSON(url, headerMap, null, params.toJSONString());
        Log.d(TAG, "获取车辆列表结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 刷新token
     * @param refreshToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject refreshCacToken(String refreshToken) throws IOException {
        String url = DEEPAL_ORIGIN + "/appapi/v1/member/ms/refreshCacToken";
        Map<String, Object> headerMap = getCAHeaderMap();
        JSONObject params = new JSONObject();
        params.put("refreshToken", refreshToken);
        Log.d(TAG, "刷新token，refreshToken：" + refreshToken);
        HttpUtil.ResEntity resEntity = HttpUtil.doPostJSON(url, headerMap, null, params.toJSONString());
        Log.d(TAG, "刷新token结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取车控token
     * @param authToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject getCacTokenByAuthToken(String authToken) throws IOException {
        String api = "/appapi/v1/member/ms/cacToken";
        String url = DEEPAL_ORIGIN + api;
        Map<String, Object> headerMap = getDeepalHeaderMap(api, null, null);
        headerMap.put("authorization", authToken);
        Log.d(TAG, "获取token的header：" + headerMap.toString());
        HttpUtil.ResEntity resEntity = HttpUtil.doGet(url, headerMap, null, null);
        Log.d(TAG, "获取token结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取配置信息
     * @param cacToken
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/4/4
     */
    public static JSONObject getBaseConfig(String cacToken) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/config/getBaseConfig?token=" + cacToken;
        Map<String, Object> headerMap = getCAHeaderMap();
        Log.d(TAG, "获取配置信息：" + cacToken);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, null);
        Log.d(TAG, "获取配置信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取车辆信息
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/3/16
     */
    public static JSONObject getCarData(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/app2/api/car/data";
        Map<String, Object> headerMap = getCAHeaderMap();
        JSONObject params = new JSONObject();
        params.put("carId", carId);
        params.put("keys", "*");
        params.put("token", accessToken);
        Log.d(TAG, "获取Car信息，请求体：" + params.toJSONString());
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, params);
        Log.d(TAG, "获取Car信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取车辆定位
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/4/24
     */
    public static JSONObject getCarLocation(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/cardata/getCarLocation";
        Map<String, Object> headerMap = getCAHeaderMap();
        JSONObject params = new JSONObject();
        params.put("carId", carId);
        params.put("mapType", "GCJ02");
        params.put("token", accessToken);
        Log.d(TAG, "获取定位，请求体：" + params.toJSONString());
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, params);
        Log.d(TAG, "获取定位信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取流量信息
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author XanderYe
     * @date 2023/4/24
     */
    public static JSONObject getCellularData(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/huservice/balanceInfo";
        Map<String, Object> headerMap = getCAHeaderMap();
        headerMap.put("X-Requested-With", "deepal.com.cn.app");
        String paramStr = "?carId=" + carId + "&token=" + accessToken;
        url += paramStr;
        Log.d(TAG, "获取流量，请求体：" + paramStr);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, null);
        Log.d(TAG, "获取流量信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    /**
     * 获取充电信息
     * @param accessToken
     * @param carId
     * @return com.alibaba.fastjson.JSONObject
     * @author yezhendong
     * @date 2023/4/24
     */
    public static JSONObject getChargeInfo(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/charge/info";
        Map<String, Object> headerMap = getCAHeaderMap();
        headerMap.put("X-Requested-With", "deepal.com.cn.app");
        String paramStr = "?carId=" + carId + "&token=" + accessToken;
        url += paramStr;
        Log.d(TAG, "获取充电状态，请求体：" + paramStr);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, null);
        Log.d(TAG, "获取充电状态信息结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    public static JSONObject getControlActionHistory(String accessToken, String carId) throws IOException {
        String url = CHANGAN_ORIGIN + "/appserver/api/car/getControlActionHistory";
        Map<String, Object> headerMap = getCAHeaderMap();
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMonths(6);
        Map<String, Object> params = new HashMap<>();
        params.put("carId", carId);
        params.put("pageSize", 20);
        params.put("page", 0);
        params.put("startTime", startTime.format(Constants.DATE_FORMAT));
        params.put("endTime", endTime.format(Constants.DATE_FORMAT));
        params.put("toast", false);
        params.put("ErrorAutoProjectile", false);
        params.put("token", accessToken);
        params.put("isNev", 0);
        params.put("type", 0);
        params.put("deviceId", "deviceId");
        Log.d(TAG, "获取车控历史，请求体：" + params);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, headerMap, null, params);
        Log.d(TAG, "获取车控历史结果：" + resEntity.getResponse());
        return JSON.parseObject(resEntity.getResponse());
    }

    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 14; 2304FPN6DC Build/UKQ1.230804.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/119.0.6045.66 Mobile Safari/537.36";

    private static Map<String, Object> getCAHeaderMap() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("User-Agent", USER_AGENT);
        headerMap.put("vcs-app-id", "changanEV");
        return headerMap;
    }

    private static Map<String, Object> getHeaderMap() {
        Map<String, Object> headerMap = new HashMap<>();
        String appVer = "1.4.1";
        headerMap.put("User-Agent", USER_AGENT);
        headerMap.put("Application-Version-Secret", "1700763492511");
        headerMap.put("appId", "sl_android");
        headerMap.put("appType", "android");
        headerMap.put("appVersion", appVer);
        headerMap.put("packageName", "deepal.com.cn.app");
        if (DEVICE_ID != null) {
            headerMap.put("deviceId", DEVICE_ID);
        }
        return headerMap;
    }

    private static Map<String, Object> getDeepalHeaderMap(String url, String bodyJson, Map<String, Object> queryMap) {
        Map<String, Object> headerMap = new HashMap<>();
        String appVer = "1.4.1";
        headerMap.put("User-Agent", USER_AGENT);
        long time = System.currentTimeMillis();
        headerMap.put("Referer", "https://h5.deepal.com.cn/");
        headerMap.put("appId", "sl_android");
        headerMap.put("appType", "android");
        headerMap.put("appVersion", appVer);
        headerMap.put("X-Deepal-Crypto-KeyMd5", "d41d8cd98f00b204e9800998ecf8427e");
        headerMap.put("X-Deepal-Crypto-Type", "Hash");
        headerMap.put("X-Deepal-Crypto-Timestamp", String.valueOf(time));
        String uuid = uuid(19);
        String nonceStr = time + uuid;
        Log.d("sunx",uuid);
        Log.d("sunx",nonceStr);
        headerMap.put("X-Deepal-Crypto-Noncestr", nonceStr);
        String queryString = toQueryString(queryMap);
        String signatureData = getSignatureData(bodyJson, queryString, nonceStr, time, url);
        headerMap.put("X-Deepal-Crypto-SignatureData", CodecUtil.urlEncode(signatureData));
        String data = signatureData.replace("#sign#key#", KEY);
        String md5 = CodecUtil.MD5(data).toUpperCase();
        headerMap.put("X-Deepal-Crypto-Signature", md5);
        Log.d("sunx",headerMap.toString());
        return headerMap;
    }

    private static String toQueryString(Map<String, Object> queryMap) {
        if (queryMap == null || queryMap.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String key : queryMap.keySet()) {
            sb.append(key).append("=").append(queryMap.get(key)).append("&");
        }
        return sb.toString();
    }

    /**
     * 注册设备
     * @param androidId
     * @param deviceName
     * @return void
     * @author XanderYe
     * @date 2023/3/22
     */
    public static void register(String androidId, String deviceName, String version) {
        String url = "https://tool.xanderye.cn/api/deepal/register";
        Map<String, Object> params = new HashMap<>();
        params.put("androidId", androidId);
        params.put("name", deviceName);
        params.put("version", version);
        Log.d(TAG, "注册设备：" + params);
        try {
            HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, params);
            Log.d(TAG, "注册设备返回结果：" + resEntity.getResponse());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查更新
     * @return void
     * @author XanderYe
     * @date 2023/3/22
     */
    public static JSONObject checkUpdate(int appVersion) throws IOException {
        String url = "https://tool.xanderye.cn/api/deepal/checkUpdate?versionCode=" + appVersion;
        HttpUtil.ResEntity resEntity = HttpUtil.doGet(url, null);
        return JSON.parseObject(resEntity.getResponse());
    }

    public static JSONObject apply(String carId, String note) throws IOException {
        String url = "https://tool.xanderye.cn/api/deepal/apply";
        Map<String, Object> params = new HashMap<>();
        params.put("carId", carId);
        params.put("note", note);
        Log.d(TAG, "申请权限：" + carId);
        HttpUtil.ResEntity resEntity = HttpUtil.doPost(url, params);
        return JSON.parseObject(resEntity.getResponse());
    }

    public static JSONObject checkApply(String carId) throws IOException {
        String url = "https://tool.xanderye.cn/api/deepal/checkApply?carId=" + carId;
        Log.d(TAG, "检查权限：" + carId);
        HttpUtil.ResEntity resEntity = HttpUtil.doGet(url, null);
        return JSON.parseObject(resEntity.getResponse());
    }
    private static String getSignatureData(String bodyJson, String queryString, String nonceStr, long time, String url) {
        StringBuilder sb = new StringBuilder();
        if (bodyJson != null && !"".equals(bodyJson)) {
            sb.append("body=").append(bodyJson).append("&");
        }
        sb.append("key=#sign#key#").append("&noncestr=").append(nonceStr).append("&");
        if (queryString != null && !"".equals(queryString)) {
            sb.append("query=").append(queryString);
        }
        sb.append("timestamp=").append(time)
                .append("&url=").append(url);
        return sb.toString();
    }

    public static String uuid(Integer e) {
        Random random = new Random();
        String[] n = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("");
        String[] r = new String[e];
        int a = 16;
        for (int i = 0; i < e; i++) {
            r[i] = n[random.nextInt(a)];
        }
        return String.join("", r);
    }

    private static String encrypt(String origin) throws Exception {
        Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;
        byte[] bytes = origin.getBytes(ISO_8859_1);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(CodecUtil.base64DecodeToByteArray(RSA_PUB_KEY));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey generatePublic = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(1, generatePublic);
        byte[] doFinal = cipher.doFinal(bytes);
        return CodecUtil.base64Encode(doFinal);
    }

    // 配置设备ID
    public static void setDeviceId(String deviceId) {
        DEVICE_ID = deviceId;
    }

    // 配置会话密钥
    public static void setConfig(String deviceId, String key) {
        DEVICE_ID = deviceId;
        KEY = key;
    }
}

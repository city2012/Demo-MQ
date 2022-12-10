package com.yun.practice.midware.rabbit.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yun.practice.midware.rabbit.common.constant.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlowflagHelper {
    protected static Environment env;
    private static String deployEnv;
    private static final Cache<String, Optional<String>> CACHE_KEYPROPERTYS =
            CacheBuilder.newBuilder()
                    .maximumSize(100L)
                    .initialCapacity(50)
                    .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                    .build();

    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    public static Boolean hasFlowtag() {
        return Objects.nonNull(getFlowFlag());
    }


    public static void initCacheKeyFromEnvironment(String key) {
        String value = env.getProperty(key);
        CACHE_KEYPROPERTYS.put(key, Optional.ofNullable(value));
    }

    public static String getProperty(String key, String defaultValue) {
        Optional<String> optional = (Optional)CACHE_KEYPROPERTYS.getIfPresent(key);
        if (optional == null) {
            initCacheKeyFromEnvironment(key);
            optional = (Optional)CACHE_KEYPROPERTYS.getIfPresent(key);
        }

        String value = defaultValue;
        if (optional != null && optional.isPresent()) {
            value = (String)optional.get();
        }

        return value;
    }


    public static boolean isK8s() {
        String k8s = System.getenv("KUBERNETES_SERVICE_HOST");
        return StringUtils.isNotBlank(k8s);
    }

    public static boolean isEcs() {
        String ecs = System.getenv("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI");
        return StringUtils.isNotBlank(ecs);
    }

    public static boolean isJdk11() {
        String jdkVersion = System.getProperty("java.version");
        return StringUtils.containsIgnoreCase(jdkVersion, "JDK11") || StringUtils.startsWith(jdkVersion, "11.0");
    }

    public static boolean isNotMacOs() {
        String OS = System.getProperty("os.name").toLowerCase();
        return !StringUtils.containsIgnoreCase(OS, "mac");
    }

    public static boolean isMacOs() {
        String OS = System.getProperty("os.name").toLowerCase();
        return StringUtils.containsIgnoreCase(OS, "mac os");
    }

    public static boolean isDev() {
        String env = getDeployEnv();
        return StringUtils.equalsIgnoreCase("dev", env) || StringUtils.endsWithIgnoreCase(env, "dev") || StringUtils.equalsIgnoreCase(env, "local") || StringUtils.endsWithIgnoreCase(env, "local");
    }

    public static boolean isQa() {
        String env = getDeployEnv();
        return StringUtils.equalsIgnoreCase("qa", env) || StringUtils.endsWithIgnoreCase(env, "qa");
    }

    public static boolean isProd() {
        String env = getDeployEnv();
        return StringUtils.isBlank(env) || StringUtils.equalsIgnoreCase("prod", env) || StringUtils.endsWithIgnoreCase(env, "prod");
    }

    public static boolean isUSA() {
        String env = getDeployEnv();
        return StringUtils.isBlank(env) || StringUtils.startsWithIgnoreCase("usa", env);
    }

    public static boolean isSGP() {
        String env = getDeployEnv();
        return StringUtils.isBlank(env) || StringUtils.startsWithIgnoreCase("sgp", env);
    }

    public static boolean isJE() {
        String env = getDeployEnv();
        return StringUtils.isBlank(env) || StringUtils.startsWithIgnoreCase("je", env);
    }

    public static boolean isUG() {
        String env = getDeployEnv();
        return StringUtils.isBlank(env) || StringUtils.startsWithIgnoreCase("ug", env);
    }

    private static String getDeployEnv() {
        if (StringUtils.isNotBlank(deployEnv)) {
            return deployEnv;
        } else {
            if (env != null) {
                deployEnv = (String)env.getProperty("spring.profiles.active", String.class);
            }

            if (StringUtils.isBlank(deployEnv)) {
                deployEnv = System.getProperty("env");
            }

            return deployEnv;
        }
    }

    public static String getFlowFlag() {
        String flowFlag = null;
        if (env != null) {
            flowFlag = getProperty("spring.application.flowflag");
            if (flowFlag == null) {
                flowFlag = getProperty("eureka.instance.metadataMap.flowflag");
            }
        }

        return "normal".equalsIgnoreCase(flowFlag) ? null : flowFlag;
    }

    public static String getDeployFlag() {
        return getEnvFlag();
    }

    public static String getEnvFlag() {
        return getFlowFlag();
    }

    public static boolean isGray() {
        String flowFlag = getFlowFlag();
        return flowFlag != null && StringUtils.equalsIgnoreCase(flowFlag, "gray");
    }

    public static boolean isBlue() {
        String flowFlag = getFlowFlag();
        return flowFlag != null && StringUtils.equalsIgnoreCase(flowFlag, "blue");
    }

    public static boolean isGreen() {
        String flowFlag = getFlowFlag();
        if (flowFlag != null && StringUtils.equalsIgnoreCase(flowFlag, "normal")) {
            return true;
        } else {
            return flowFlag == null;
        }
    }

    static {
        ScheduledExecutorService scheduleReport = Executors.newScheduledThreadPool(1);
        scheduleReport.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    if (FlowflagHelper.CACHE_KEYPROPERTYS.size() > 0L && FlowflagHelper.env != null) {
                        FlowflagHelper.CACHE_KEYPROPERTYS.asMap().forEach((key, optional) -> {
                            String value = FlowflagHelper.env.getProperty(key);
                            if (optional.isPresent() && !Objects.equals(optional.get(), value) || !optional.isPresent() && value != null) {
                                FlowflagHelper.CACHE_KEYPROPERTYS.put(key, Optional.ofNullable(value));
                            }

                        });
                    }
                } catch (Throwable var2) {
                }

            }
        }, 0L, 60L, TimeUnit.SECONDS);
    }

    public static boolean needConsumer(ConsumerRecord consumerRecord) {

        final Headers headers = consumerRecord.headers();
        if (isHeadersEmpty(headers) && !hasFlowtag()){
            return true;
        }
        if (isHeadersEmpty(headers) && hasFlowtag()){
            return false;
        }
        if (!isHeadersEmpty(headers)){
            for (Header header : headers.headers(Constants.MQ_FLOWFLAG)){
                if (new String(header.value()).equalsIgnoreCase(getFlowFlag())){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isHeadersEmpty(Headers headers){
        if (null == headers){
            return true;
        }
        return 0 == headers.toArray().length;
    }

}

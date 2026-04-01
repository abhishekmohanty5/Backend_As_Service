package com.jobhunt.saas.shared;

public class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new InheritableThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
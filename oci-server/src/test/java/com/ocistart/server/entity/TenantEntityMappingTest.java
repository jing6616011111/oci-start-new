package com.ocistart.server.entity;

import com.ocistart.dao.entity.Tenant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Lob;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantEntityMappingTest {

    @Test
    void keyFileUsesLargeTextColumnForPemPrivateKey() throws NoSuchFieldException {
        Field keyFile = Tenant.class.getDeclaredField("keyFile");
        Column column = keyFile.getAnnotation(Column.class);

        assertNotNull(keyFile.getAnnotation(JsonIgnore.class));
        assertNotNull(keyFile.getAnnotation(Lob.class));
        assertNotNull(column);
        assertTrue(column.columnDefinition().toUpperCase().contains("CLOB"));
    }
}

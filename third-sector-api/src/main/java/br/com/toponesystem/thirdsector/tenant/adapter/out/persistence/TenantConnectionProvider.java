package br.com.toponesystem.thirdsector.tenant.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private static final String DEFAULT_SCHEMA = "public";

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantId) throws SQLException {
        var connection = dataSource.getConnection();
        connection.setSchema(tenantId);
        return connection;
    }

    @Override
    public void releaseConnection(String tenantId, Connection connection) throws SQLException {
        connection.setSchema(DEFAULT_SCHEMA);
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return unwrapType.isInstance(this);
    }

    @Override
    public <X> X unwrap(Class<X> unwrapType) {
        if (unwrapType.isInstance(this)) {
            return unwrapType.cast(this);
        }
        throw new UnknownUnwrapTypeException(unwrapType);
    }
}

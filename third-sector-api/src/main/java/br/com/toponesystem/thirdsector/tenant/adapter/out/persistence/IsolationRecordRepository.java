package br.com.toponesystem.thirdsector.tenant.adapter.out.persistence;

import br.com.toponesystem.thirdsector.tenant.domain.model.IsolationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IsolationRecordRepository extends JpaRepository<IsolationRecord, UUID> {
}

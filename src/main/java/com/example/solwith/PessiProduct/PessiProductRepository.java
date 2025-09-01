package com.example.solwith.PessiProduct;

import com.example.solwith.Optiproduct.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PessiProductRepository extends JpaRepository<PessiProduct, Long> {

    // 행 배타 락: MySQL -> SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PessiProduct p where p.id = :id")
    Optional<PessiProduct> findByIdForUpdate(@Param("id") Long id);

    // (선택) 읽기 공유 락
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p from PessiProduct p where p.id = :id")
    Optional<PessiProduct> findByIdForShare(@Param("id") Long id);

    // (선택) '바로 실패' 전략: NOWAIT (MySQL 8+ 지원함)
    @Query(value = "select * from products where id = :id for update nowait", nativeQuery = true)
    Optional<PessiProduct> findByIdForUpdateNowait(@Param("id") Long id);

}

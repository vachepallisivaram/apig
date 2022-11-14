package com.otsi.retail.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.otsi.retail.gateway.util.ContentLogging;

@Repository
public interface RequestDataRepository extends JpaRepository<ContentLogging, Long> {

}

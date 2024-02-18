package com.gpt.product.gpcash.corporate.transaction.sipkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.sipkd.model.SipkdModel;

@Repository
public interface SipkdRepository extends JpaRepository<SipkdModel, String>, CashRepository<SipkdModel>{

}

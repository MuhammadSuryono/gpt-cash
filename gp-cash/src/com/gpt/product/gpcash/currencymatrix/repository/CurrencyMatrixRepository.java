package com.gpt.product.gpcash.currencymatrix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.currencymatrix.model.CurrencyMatrixModel;

@Repository
public interface CurrencyMatrixRepository extends JpaRepository<CurrencyMatrixModel, String>, CashRepository<CurrencyMatrixModel> {

}

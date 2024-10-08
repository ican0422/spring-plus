package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.eunm.LogSuccessfulStatus;
import org.example.expert.domain.log.repository.LogRepository;
import org.example.expert.domain.user.entity.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processLog(LogSuccessfulStatus apiStatus, User user) {
        printActiveTransaction("processLog()");

        Log log = new Log(apiStatus, user);
        logRepository.save(log);
    }

    private void printActiveTransaction(String methodName) {
        // 현재 트랜잭션이 활성화되어 있는지를 확인
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        // 현재 트랜잭션이 새로운 트랜잭션인지 확인
        boolean isNewTransaction = TransactionAspectSupport.currentTransactionStatus().isNewTransaction();
        log.info("-> {}: isTxActive: {}, isNew: {}", methodName, actualTransactionActive, isNewTransaction);
    }
}

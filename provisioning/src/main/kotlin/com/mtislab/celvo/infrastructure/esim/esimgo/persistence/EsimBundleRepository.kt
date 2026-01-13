package com.mtislab.celvo.infrastructure.esim.esimgo.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EsimBundleRepository : JpaRepository<EsimBundleEntity, String>
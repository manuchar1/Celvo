package com.mtislab.celvo.domain.esim.ports

import com.mtislab.celvo.domain.esim.models.*

/**
 * Port interface defining the contract for any eSIM provider.
 * This follows the hexagonal architecture pattern, decoupling domain logic from infrastructure.
 */
interface EsimProvider {
    
    /**
     * Fetches the available eSIM bundles from the provider's catalogue.
     * @return List of available bundles
     */
    fun getCatalogue(): List<EsimBundle>
    

}

package com.truebalance.creditiq.enrichment.integration;

import com.truebalance.creditiq.verification.Lead;
import com.truebalance.creditiq.verification.User;

public interface CrmClient {

    void pushLead(User user, Lead lead);
}

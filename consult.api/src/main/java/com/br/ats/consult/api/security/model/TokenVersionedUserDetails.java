package com.br.ats.consult.api.security.model;

import org.springframework.security.core.userdetails.UserDetails;

public interface TokenVersionedUserDetails extends UserDetails {

    long getTokenVersion();
}

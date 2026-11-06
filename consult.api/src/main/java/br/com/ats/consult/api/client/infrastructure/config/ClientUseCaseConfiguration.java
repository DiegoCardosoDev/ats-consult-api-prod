package br.com.ats.consult.api.client.infrastructure.config;

import br.com.ats.consult.api.client.domain.port.out.ClientGateway;
import br.com.ats.consult.api.client.domain.usecase.CreateClientUseCase;
import br.com.ats.consult.api.client.domain.usecase.FindClientUseCase;
import br.com.ats.consult.api.client.domain.usecase.impl.CreateClientUseCaseImpl;
import br.com.ats.consult.api.client.domain.usecase.impl.FindClientUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientUseCaseConfiguration {

    @Bean
    public CreateClientUseCase createClientUseCase(ClientGateway gateway) {
        return new CreateClientUseCaseImpl(gateway);
    }

    @Bean
    public FindClientUseCase findClientUseCase(ClientGateway gateway) {
        return new FindClientUseCaseImpl(gateway);
    }
}

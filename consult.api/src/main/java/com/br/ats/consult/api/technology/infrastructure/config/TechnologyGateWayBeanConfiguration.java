package com.br.ats.consult.api.technology.infrastructure.config;

import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import com.br.ats.consult.api.technology.infrastructure.persistence.adapter.TechnologyRepositoryAdapter;
import com.br.ats.consult.api.technology.infrastructure.persistence.mapper.TechnologyMapper;
import com.br.ats.consult.api.technology.infrastructure.persistence.repository.SpringDataTechnologyRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TechnologyGateWayBeanConfiguration {


    /**
     * Expõe o adaptador de persistência como um bean gerenciado do tipo {@link TechnologyGateway}.
     *
     * @param repository Repositório do Spring Data.
     * @param mapper     Mapper do MapStruct.
     * @return Instância concreta do gateway de tecnologias.
     */
    @Bean
    public TechnologyGateway technologyGateway(
            SpringDataTechnologyRepository repository,
            TechnologyMapper mapper
    ) {
        return new TechnologyRepositoryAdapter(repository, mapper);
    }
}

package com.br.ats.consult.api.selectionprocess.domain.usecase;

import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;

public interface CreateSelectionProcessUseCase {

    SelectionProcess execute(SelectionProcess process);
}

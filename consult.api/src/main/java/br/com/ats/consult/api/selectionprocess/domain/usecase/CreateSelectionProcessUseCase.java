package br.com.ats.consult.api.selectionprocess.domain.usecase;

import br.com.ats.consult.api.selectionprocess.domain.model.SelectionProcess;

public interface CreateSelectionProcessUseCase {

    SelectionProcess execute(SelectionProcess process);
}

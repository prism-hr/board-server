package hr.prism.board.workflow;

import hr.prism.board.domain.Resource;

public interface WorkflowExecution {

    Resource executeWithAction();

}

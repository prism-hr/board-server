package hr.prism.board.authentication;

import hr.prism.board.domain.Scope;
import hr.prism.board.enums.Action;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockApi {
    
    @Restriction(scope = Scope.BOARD)
    public void getBoards() {
    }
    
    @Restriction(scope = Scope.DEPARTMENT)
    public void getDepartments() {
    }
    
    @Restriction(scope = Scope.BOARD, actions = Action.EDIT)
    public void getBoard(@PathVariable("id") Long id) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, actions = Action.EDIT)
    public void getDepartment(@PathVariable("id") Long id) {
    }
    
    @Restriction(scope = Scope.BOARD, actions = Action.EDIT)
    public void getBoard(@PathVariable("handle") String handle) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, actions = Action.EDIT)
    public void getDepartment(@PathVariable("handle") String handle) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, actions = Action.EDIT)
    public void invalidArgument(@PathVariable String handle) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, actions = Action.EDIT)
    public void invalidSignature(String handle) {
    }
    
}
